package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.impl.IMatchingRuleProvider;
import info.novatec.inspectit.cmr.ci.event.BusinessContextDefinitionUpdateEvent;
import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IConfigurationInterfaceService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

/**
 * This processor enriches {@link InvocationSequenceData} instances (i.e. roots of invocation
 * sequences) with business context information (i.e. corresponding application and business
 * transaction).
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextRecognitionProcessor extends AbstractCmrDataProcessor implements ApplicationListener<BusinessContextDefinitionUpdateEvent> {

	/**
	 * {@link CachedDataService} instance used to access method information (e.g. method names,
	 * parameters, etc.).
	 */
	@Autowired
	private ICachedDataService cachedDataService;

	/**
	 * {@link IBusinessContextManagementService} instance.
	 */
	@Autowired
	private IBusinessContextManagementService businessContextMngmtService;

	/**
	 * {@link IConfigurationInterfaceService} instance.
	 */
	@Autowired
	private IConfigurationInterfaceService configurationInterfaceService;

	/**
	 * The invocation DAO used for updating {@link InvocationSequenceData} objects in the buffer.
	 */
	@Autowired
	private InvocationDataDao invocationDataDao;

	/**
	 * {@link ExecutorService} for updating business context assignments.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocSequence = (InvocationSequenceData) defaultData;
		assignBusinessContext(invocSequence);
	}

	/**
	 * Assigns the business context to the passed {@link InvocationSequenceData} instance.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to assign the business context for.
	 */
	public void assignBusinessContext(InvocationSequenceData invocSequence) {
		List<ApplicationDefinition> applicationDefinitions = configurationInterfaceService.getApplicationDefinitions();
		ApplicationDefinition appDefinition = identify(invocSequence, applicationDefinitions);
		if (null != appDefinition) {
			int applicationId = appDefinition.createApplicationId();
			ApplicationData application = cachedDataService.getApplicationForId(applicationId);
			if (null == application) {
				application = new ApplicationData(applicationId, appDefinition.getId(), appDefinition.getApplicationName());
				businessContextMngmtService.registerApplication(application);
			}

			invocSequence.setApplicationId(applicationId);

			BusinessTransactionDefinition businessTxDefinition = identify(invocSequence, appDefinition.getBusinessTransactionDefinitions());

			String businessTxName = businessTxDefinition.determineBusinessTransactionName(invocSequence, cachedDataService);

			int businessTxId = businessTxDefinition.createBusinessTransactionId(businessTxName);
			BusinessTransactionData businessTransaction = cachedDataService.getBusinessTransactionForId(application.getId(), businessTxId);
			if (null == businessTransaction) {
				businessTransaction = new BusinessTransactionData(businessTxId, businessTxDefinition.getId(), application, businessTxName);
				businessContextMngmtService.registerBusinessTransaction(businessTransaction);
			}
			invocSequence.setBusinessTransactionId(businessTransaction.getId());
		}
	}

	/**
	 * Identifies a matching rule provider.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance providing the evaluation context.
	 * @param ruleProviders
	 *            list of provider candidates.
	 * @param <E>
	 *            the type of the {@link IMatchingRuleProvider} instance.
	 * @return one selected rule provider if any matches, otherwise null.
	 */
	private <E extends IMatchingRuleProvider> E identify(InvocationSequenceData invocSequence, List<E> ruleProviders) {
		for (E ruleProvider : ruleProviders) {
			boolean ruleMatches = ruleProvider.getMatchingRuleExpression().evaluate(invocSequence, cachedDataService);
			if (ruleMatches) {
				return ruleProvider;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(BusinessContextDefinitionUpdateEvent event) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				// update business context for invocation sequences that are in the buffer
				List<InvocationSequenceData> invocations = invocationDataDao.getBufferInvocationSequenceInstances(0, 0, -1, null, null, null);
				for (InvocationSequenceData invocation : invocations) {
					assignBusinessContext(invocation);
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
