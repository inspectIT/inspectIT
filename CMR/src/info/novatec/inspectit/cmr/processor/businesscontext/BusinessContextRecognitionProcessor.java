package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.ci.business.ApplicationDefinition;
import info.novatec.inspectit.ci.business.BusinessTransactionDefinition;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This processor enriches {@link InvocationSequenceData} instances (i.e. roots of invocation
 * sequences) with business context information (i.e. corresponding application and business
 * transaction).
 * 
 * @author Alexander Wert
 *
 */
public class BusinessContextRecognitionProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link IBusinessContextManagementService} instance.
	 */
	@Autowired
	private IBusinessContextManagementService businessContextMngmtService;

	/**
	 * {@link ExpressionEvaluation} instance.
	 */
	@Autowired
	private ExpressionEvaluation evaluation;

	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocSequence = (InvocationSequenceData) defaultData;
		List<IApplicationDefinition> applicationDefinitions = businessContextMngmtService.getApplicationDefinitions();
		IApplicationDefinition application = identifyApplication(invocSequence, applicationDefinitions);
		if (null != application) {
			identifyBusinessTransaction(invocSequence, application);
		}
	}

	/**
	 * Identifies the application the corresponding invocation sequence belongs to.
	 * 
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to check
	 * @param applicationDefinitions
	 *            list of defined applications
	 * @return Returns the {@link IApplicationDefinition} that applies to the corresponding
	 *         invocation sequence.
	 */
	private IApplicationDefinition identifyApplication(InvocationSequenceData invocSequence, List<IApplicationDefinition> applicationDefinitions) {
		for (IApplicationDefinition appDefinition : applicationDefinitions) {
			if (appDefinition instanceof ApplicationDefinition) {
				boolean evaluationResult = evaluation.evaluate(appDefinition.getMatchingRule(), invocSequence);
				if (evaluationResult) {
					invocSequence.setApplicationId(appDefinition.getId());
					return appDefinition;
				}
			}

		}
		return null;
	}

	/**
	 * Identifies the business transaction the corresponding invocation sequence belongs to.
	 * 
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to check
	 * @param application
	 *            the {@link IApplicationDefinition} the corresponding invocation sequence belongs
	 *            to
	 * @return Returns the {@link IBusinessTransactionDefinition} that applies to the corresponding
	 *         invocation sequence.
	 */
	private IBusinessTransactionDefinition identifyBusinessTransaction(InvocationSequenceData invocSequence, IApplicationDefinition application) {
		for (IBusinessTransactionDefinition bTxDefinition : application.getBusinessTransactionDefinitions()) {
			if (bTxDefinition instanceof BusinessTransactionDefinition) {
				boolean evaluationResult = evaluation.evaluate(bTxDefinition.getMatchingRule(), invocSequence);
				if (evaluationResult) {
					invocSequence.setBusinessTransactionId(bTxDefinition.getId());
					return bTxDefinition;
				}
			}

		}
		return null;
	}

	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
