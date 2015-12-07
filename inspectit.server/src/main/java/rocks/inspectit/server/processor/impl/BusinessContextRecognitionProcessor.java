package rocks.inspectit.server.processor.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import rocks.inspectit.server.ci.event.BusinessContextDefinitionUpdateEvent;
import rocks.inspectit.server.dao.InvocationDataDao;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.IMatchingRuleProvider;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextRegistryService;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

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
	 * {@link IBusinessContextRegistryService} instance.
	 */
	@Autowired
	private IBusinessContextRegistryService businessContextRegistryService;

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
		if (null == appDefinition) {
			appDefinition = BusinessContextDefinition.DEFAULT_APPLICATION_DEFINITION;
		}
		ApplicationData application = businessContextRegistryService.registerApplication(appDefinition);
		invocSequence.setApplicationId(application.getId());

		BusinessTransactionDefinition businessTxDefinition = identify(invocSequence, appDefinition.getBusinessTransactionDefinitions());
		String businessTxName = businessTxDefinition.determineBusinessTransactionName(invocSequence, cachedDataService);
		BusinessTransactionData businessTransaction = businessContextRegistryService.registerBusinessTransaction(application, businessTxDefinition, businessTxName);
		invocSequence.setBusinessTransactionId(businessTransaction.getId());
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
				List<InvocationSequenceData> invocations = invocationDataDao.getInvocationSequenceDetail(0, 0, -1, null, null, null);
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
