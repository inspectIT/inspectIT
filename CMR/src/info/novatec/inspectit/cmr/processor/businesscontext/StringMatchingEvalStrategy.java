package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.ci.business.HostValueSource;
import info.novatec.inspectit.ci.business.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.HttpUriValueSource;
import info.novatec.inspectit.ci.business.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.ci.business.StringValueSource;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;

/**
 * Evaluation strategy for the {@link StringMatchingExpression}.
 * 
 * @author Alexander Wert
 *
 */
public class StringMatchingEvalStrategy extends AbstractExpressionEvaluationStrategy<StringMatchingExpression> {

	/**
	 * Constructor.
	 * 
	 * @param expressionEvaluation
	 *            expression evaluation component used for recursive evaluation
	 */
	public StringMatchingEvalStrategy(ExpressionEvaluation expressionEvaluation) {
		super(expressionEvaluation);
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(StringMatchingExpression expression, InvocationSequenceData invocSequence) {
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
		boolean matches = false;

		Object stringObject = retrieveStringValue(expression.getStringValueSource(), invocSequence);

		if (null != stringObject) {
			if (stringObject instanceof String) {
				String stringValue = (String) stringObject;
				if (null != stringValue && evaluateString(expression, stringValue)) {
					return true;
				}
			} else if (stringObject instanceof String[]) {
				String[] strArray = (String[]) stringObject;
				for (int i = 0; i < strArray.length; i++) {
					if (null != strArray[i] && evaluateString(expression, strArray[i])) {
						return true;
					}
				}
			} else {
				throw new IllegalStateException("Unsupported type of stringObject!");
			}
		}

		if (!matches && expression.isSearchNodeInTrace() && (expression.getMaxSearchDepth() < 0 || depth < expression.getMaxSearchDepth())) {
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
		default:
			return false;
		}
	}

	/**
	 * Retrieves the string to be checked according to the given {@link StringValueSource}.
	 * 
	 * @param stringValueSource
	 *            {@link StringValueSource} instance defining where to get the string from
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance defining the evaluation context
	 * @return Returns the string to be checked. If the corresponding
	 *         {@link StringValueSource} provides multiple string values, this method
	 *         returns an array of string.
	 */
	private Object retrieveStringValue(StringValueSource stringValueSource, InvocationSequenceData invocSequence) {
		if (stringValueSource instanceof HttpUriValueSource && InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
			return httpData.getUri();
		} else if (stringValueSource instanceof HttpParameterValueSource && InvocationSequenceDataHelper.hasHttpTimerData(invocSequence)) {
			HttpTimerData httpData = (HttpTimerData) invocSequence.getTimerData();
			HttpParameterValueSource httpStringValueSource = (HttpParameterValueSource) stringValueSource;
			if (null != httpData.getParameters()) {
				return httpData.getParameters().get(httpStringValueSource.getParameterName());
			}
		} else if (stringValueSource instanceof MethodSignatureValueSource) {
			MethodIdent mIdent = cachedDataService.getMethodIdentForId(invocSequence.getMethodIdent());
			if (null != mIdent) {
				return mIdent.getFullyQualifiedMethodSignature();
			}
		} else if (stringValueSource instanceof HostValueSource) {
			PlatformIdent pIdent = cachedDataService.getPlatformIdentForId(invocSequence.getPlatformIdent());
			return pIdent.getDefinedIPs().toArray(new String[0]);
		}

		return null;
	}
}
