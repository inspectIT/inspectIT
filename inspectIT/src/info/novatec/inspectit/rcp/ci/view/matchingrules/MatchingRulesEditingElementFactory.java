package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HostValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HttpParameterValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HttpUriValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.MethodSignatureValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.StringMatchingExpression;

/**
 * Factory for {@link AbstractRuleEditingElement} instances.
 * 
 * @author Alexander Wert
 *
 */
public final class MatchingRulesEditingElementFactory {

	/**
	 * Private constructor because of utility class.
	 */
	private MatchingRulesEditingElementFactory() {
	}

	/**
	 * Creates an {@link AbstractRuleEditingElement} instance depending on the passed
	 * {@link Expression}.
	 * 
	 * @param expression
	 *            {@link Expression} determining the {@link AbstractRuleEditingElement}
	 *            instance.
	 * @return Returns {@link Expression} instance, or {@code null} if no
	 *         {@link Expression} instance can be created for the passed
	 *         {@link Expression}.
	 * 
	 */
	public static AbstractRuleEditingElement createRuleComposite(Expression expression) {
		if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpUriValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_URI);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpParameterValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_PARAMETER);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodSignatureValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_PARAMETER);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HostValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_PARAMETER);
		}
		return null;
	}

	/**
	 * Creates an {@link AbstractRuleEditingElement} instance for the passed
	 * {@link MatchingRuleType}.
	 * 
	 * @param type
	 *            MatchingRuleType determining the {@link AbstractRuleEditingElement} instance.
	 * @return Returns an {@link AbstractRuleEditingElement} instnace.
	 */
	public static AbstractRuleEditingElement createRuleComposite(MatchingRuleType type) {
		AbstractRuleEditingElement ruleComposite = null;
		switch (type) {
		case HTTP_PARAMETER:
			ruleComposite = new HttpParameterRuleEditingElement();
			break;
		case HTTP_URI:
			ruleComposite = new HttpUriMatchingRuleEditingElement();
			break;
		case IP:
			ruleComposite = new IPRuleEditingElement();
			break;
		case METHOD_SIGNATURE:
			ruleComposite = new MethodSignatureRuleEditingElement();
			break;
		default:
			throw new RuntimeException("Unsupported type!");
		}
		return ruleComposite;
	}

	/**
	 * Enum for matching rules types.
	 * 
	 * @author Alexander Wert
	 *
	 */
	public enum MatchingRuleType {
		/**
		 * Matching of the HTTP URI.
		 */
		HTTP_URI,

		/**
		 * Matching of an HTTP parameter value.
		 */
		HTTP_PARAMETER,

		/**
		 * Matching of a method signature.
		 */
		METHOD_SIGNATURE,

		/**
		 * Matching of an IP address.
		 */
		IP;

		@Override
		public String toString() {
			switch (this) {
			case HTTP_PARAMETER:
				return "HTTP Parameter Matching";
			case HTTP_URI:
				return "HTTP URI Matching";
			case IP:
				return "IP Matching";
			case METHOD_SIGNATURE:
				return "Method Signature Matching";

			default:
				throw new RuntimeException("Unsupported type!");
			}
		}
	}
}
