package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.AndExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.impl.NameExtractionExpression;
import info.novatec.inspectit.ci.business.impl.NotExpression;
import info.novatec.inspectit.ci.business.impl.OrExpression;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.cmr.ci.event.BusinessContextDefinitionUpdateEvent;
import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * This component is responsible for evaluating {@link MatchingRule} instances.
 *
 * @author Alexander Wert
 *
 */
@Component
public class ExpressionEvaluation implements ApplicationListener<BusinessContextDefinitionUpdateEvent> {

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
	 * Assigns the business context to the passed {@link InvocationSequenceData} instance.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to assign the business context for.
	 */
	public void assignBusinessContext(InvocationSequenceData invocSequence) {
		List<ApplicationDefinition> applicationDefinitions = businessContextMngmtService.getApplicationDefinitions();
		ApplicationDefinition appDefinition = identifyApplicationDefinition(invocSequence, applicationDefinitions);
		if (null != appDefinition) {
			int applicationId = appDefinition.createApplicationId();
			ApplicationData application = cachedDataService.getApplicationForId(applicationId);
			if (null == application) {
				application = new ApplicationData(applicationId, appDefinition.getId(), appDefinition.getApplicationName());
			}

			invocSequence.setApplicationId(applicationId);

			BusinessTransactionDefinition businessTxDefinition = identifyBusinessTransactionDefinition(invocSequence, appDefinition);

			String businessTxName = determineBusinessTransactionName(invocSequence, businessTxDefinition);

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
	 * Identifies the application the corresponding invocation sequence belongs to.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to check
	 * @param applicationDefinitions
	 *            list of defined applications
	 * @return Returns the {@link ApplicationDefinition} that applies to the corresponding
	 *         invocation sequence.
	 */
	private ApplicationDefinition identifyApplicationDefinition(InvocationSequenceData invocSequence, List<ApplicationDefinition> applicationDefinitions) {
		for (ApplicationDefinition appDefinition : applicationDefinitions) {
			boolean ruleMatches = evaluate(appDefinition.getMatchingRuleExpression(), invocSequence);
			if (ruleMatches) {
				return appDefinition;
			}
		}
		return null;
	}

	/**
	 * Identifies the business transaction the corresponding invocation sequence belongs to.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to check
	 * @param applicationDefinition
	 *            the {@link ApplicationDefinition} the corresponding invocation sequence belongs to
	 * @return Returns the {@link BusinessTransactionDefinition} that applies to the corresponding
	 *         invocation sequence.
	 */
	private BusinessTransactionDefinition identifyBusinessTransactionDefinition(InvocationSequenceData invocSequence, ApplicationDefinition applicationDefinition) {
		for (BusinessTransactionDefinition bTxDefinition : applicationDefinition.getBusinessTransactionDefinitions()) {
			boolean ruleMatches = evaluate(bTxDefinition.getMatchingRuleExpression(), invocSequence);
			if (ruleMatches) {
				return bTxDefinition;
			}
		}
		return null;
	}

	/**
	 * Determines the business transaction name for the given {@link InvocationSequenceData} using
	 * the passed {@link BusinessTransactionDefinition}.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} to determine the business transaction name for.
	 * @param businessTxDefinition
	 *            {@link BusinessTransactionDefinition} describing where to retrieve the name from.
	 * @return a business transaction name
	 */
	private String determineBusinessTransactionName(InvocationSequenceData invocSequence, BusinessTransactionDefinition businessTxDefinition) {
		String businessTxName;
		if (businessTxDefinition.dynamicNameExtractionActive()) {
			NameExtractionExpression extractionExpression = businessTxDefinition.getNameExtractionExpression();
			businessTxName = extractNameDynamically(extractionExpression, invocSequence, 0);
			String definitionName = businessTxDefinition.getBusinessTransactionDefinitionName();
			if (null == businessTxName) {
				businessTxName = definitionName + NameExtractionExpression.UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX;
			}
		} else {
			businessTxName = businessTxDefinition.getBusinessTransactionDefinitionName();
		}
		return businessTxName;
	}

	/**
	 * Extracts the business transaction name dynamically from the {@link InvocationSequenceData} by
	 * recursively iterating over the invocation sequence.
	 *
	 * @param extractionExpression
	 *            {@link NameExtractionExpression} instance describing the name extraction.
	 * @param invocSequence
	 *            {@link InvocationSequenceData} to extract the business transaction from.
	 * @param depth
	 *            current recursion depth. THis is used stop the recursion at a specified maximum
	 *            search depth.
	 * @return extracted name
	 */
	private String extractNameDynamically(NameExtractionExpression extractionExpression, InvocationSequenceData invocSequence, int depth) {
		String name = extractionExpression.extractName(invocSequence, cachedDataService);
		if (null == name && null != invocSequence.getNestedSequences() && (extractionExpression.getMaxSearchDepth() < 0 || depth < extractionExpression.getMaxSearchDepth())) {
			for (InvocationSequenceData child : invocSequence.getNestedSequences()) {
				name = extractNameDynamically(extractionExpression, child, depth + 1);
				if (null != name) {
					return name;
				}
			}

		}
		return name;
	}

	/**
	 * Evaluates the given {@link AbstractExpression} against the evaluation context defined by the
	 * {@link InvocationSequenceData} instance.
	 *
	 * @param expression
	 *            {@link AbstractExpression} instance
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance defining the evaluation context.
	 * @return Boolean result of evaluating the {@link AbstractExpression}
	 */
	private boolean evaluate(AbstractExpression expression, InvocationSequenceData invocSequence) {
		switch (expression.getExpressionType()) {
		case AND:
			return evaluateSpecificExpression((AndExpression) expression, invocSequence);
		case BOOLEAN:
			return ((BooleanExpression) expression).isValue();
		case NOT:
			return evaluateSpecificExpression((NotExpression) expression, invocSequence);
		case OR:
			return evaluateSpecificExpression((OrExpression) expression, invocSequence);
		case STRING_MATCHING:
			return evaluateSpecificExpression((StringMatchingExpression) expression, invocSequence);
		default:
			throw new RuntimeException("Expression of type " + expression.getClass().getName() + " not supported!");
		}
	}

	/**
	 * Evaluate an {@link AndExpression} for the passed {@link InvocationSequenceData} instance.
	 *
	 * @param expression
	 *            {@link AndExpression} to evaluate.
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to evaluate the expression against.
	 * @return true, if the given {@link AndExpression} applies to the passed
	 *         {@link InvocationSequenceData} instance.
	 */
	private boolean evaluateSpecificExpression(AndExpression expression, InvocationSequenceData invocSequence) {
		for (AbstractExpression expr : expression.getOperands()) {
			if (!evaluate(expr, invocSequence)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Evaluate an {@link NotExpression} for the passed {@link InvocationSequenceData} instance.
	 *
	 * @param expression
	 *            {@link NotExpression} to evaluate.
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to evaluate the expression against.
	 * @return true, if the given {@link NotExpression} applies to the passed
	 *         {@link InvocationSequenceData} instance.
	 */
	private boolean evaluateSpecificExpression(NotExpression expression, InvocationSequenceData invocSequence) {
		return !evaluate(expression.getOperand(), invocSequence);
	}

	/**
	 * Evaluate an {@link OrExpression} for the passed {@link InvocationSequenceData} instance.
	 *
	 * @param expression
	 *            {@link OrExpression} to evaluate.
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to evaluate the expression against.
	 * @return true, if the given {@link OrExpression} applies to the passed
	 *         {@link InvocationSequenceData} instance.
	 */
	private boolean evaluateSpecificExpression(OrExpression expression, InvocationSequenceData invocSequence) {
		for (AbstractExpression expr : expression.getOperands()) {
			if (evaluate(expr, invocSequence)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaluate an {@link StringMatchingExpression} for the passed {@link InvocationSequenceData}
	 * instance.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} to evaluate.
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance to evaluate the expression against.
	 * @return true, if the given {@link StringMatchingExpression} applies to the passed
	 *         {@link InvocationSequenceData} instance.
	 */
	private boolean evaluateSpecificExpression(StringMatchingExpression expression, InvocationSequenceData invocSequence) {
		return evaluate(expression, invocSequence, 0);
	}

	/**
	 * Recursive evaluation in the invocation sequence structure if search in trace is activated.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} to evaluate
	 * @param invocSequence
	 *            {@link InvocationSequenceData} forming the evaluation context
	 * @param depth
	 *            current search depth in the invocation sequence tree structure
	 * @return Returns evaluation result.
	 */
	private boolean evaluate(StringMatchingExpression expression, InvocationSequenceData invocSequence, int depth) {
		String[] strArray = expression.getStringValueSource().getStringValues(invocSequence, cachedDataService);

		for (String element : strArray) {
			if (null != element && evaluateString(expression, element)) {
				return true;
			}
		}

		if (expression.isSearchNodeInTrace() && (expression.getMaxSearchDepth() < 0 || depth < expression.getMaxSearchDepth())) {
			for (InvocationSequenceData childNode : invocSequence.getNestedSequences()) {
				if (evaluate(expression, childNode, depth + 1)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Evaluates the string array against the snippet in the {@link StringMatchingExpression}
	 * instance.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} instance
	 * @param stringValue
	 *            string to check
	 * @return boolean evaluation result
	 */
	private boolean evaluateString(StringMatchingExpression expression, String stringValue) {
		switch (expression.getMatchingType()) {
		case CONTAINS:
			return stringValue.contains(expression.getSnippet());
		case ENDS_WITH:
			return stringValue.endsWith(expression.getSnippet());
		case STARTS_WITH:
			return stringValue.startsWith(expression.getSnippet());
		case EQUALS:
			return stringValue.equals(expression.getSnippet());
		case REGEX:
			return stringValue.matches(expression.getSnippet());
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(BusinessContextDefinitionUpdateEvent arg0) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				// update business context for invocation sequences that are in the buffer
				List<InvocationSequenceData> invocations = invocationDataDao.getInvocationSequenceOverview(0);
				for (InvocationSequenceData invocation : invocations) {
					assignBusinessContext(invocation);
				}
			}
		});
	}
}
