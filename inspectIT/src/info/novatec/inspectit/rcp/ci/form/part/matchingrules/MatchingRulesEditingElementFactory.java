package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.AndExpression;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.HostValueSource;
import info.novatec.inspectit.ci.business.impl.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.impl.HttpUriValueSource;
import info.novatec.inspectit.ci.business.impl.MethodParameterValueSource;
import info.novatec.inspectit.ci.business.impl.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.impl.NotExpression;
import info.novatec.inspectit.ci.business.impl.OrExpression;
import info.novatec.inspectit.ci.business.impl.PatternMatchingType;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;

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
	 * {@link AbstractExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} determining the {@link AbstractRuleEditingElement}
	 *            instance.
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @return Returns {@link AbstractExpression} instance, or {@code null} if no
	 *         {@link AbstractExpression} instance can be created for the passed
	 *         {@link AbstractExpression}.
	 *
	 */
	public static AbstractRuleEditingElement createRuleComposite(AbstractExpression expression, boolean editable) {
		if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpUriValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_URI, editable);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpParameterValueSource) {
			return createRuleComposite(MatchingRuleType.HTTP_PARAMETER, editable);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodSignatureValueSource) {
			return createRuleComposite(MatchingRuleType.METHOD_SIGNATURE, editable);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HostValueSource) {
			return createRuleComposite(MatchingRuleType.IP, editable);
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodParameterValueSource) {
			return createRuleComposite(MatchingRuleType.METHOD_PARAMETER, editable);
		} else if (expression instanceof BooleanExpression) {
			return createRuleComposite(BooleanExpressionType.BOOLEAN, editable);
		}
		return null;
	}

	/**
	 * Creates an {@link AbstractRuleEditingElement} instance for the passed
	 * {@link MatchingRuleType}.
	 *
	 * @param type
	 *            MatchingRuleType determining the {@link AbstractRuleEditingElement} instance.
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	public static AbstractRuleEditingElement createRuleComposite(MatchingRuleType type, boolean editable) {
		AbstractRuleEditingElement ruleComposite = null;
		switch (type) {
		case HTTP_PARAMETER:
			ruleComposite = new HttpParameterRuleEditingElement(editable);
			break;
		case HTTP_URI:
			ruleComposite = new HttpUriMatchingRuleEditingElement(editable);
			break;
		case IP:
			ruleComposite = new IPRuleEditingElement(editable);
			break;
		case METHOD_SIGNATURE:
			ruleComposite = new MethodSignatureRuleEditingElement(editable);
			break;
		case METHOD_PARAMETER:
			ruleComposite = new MethodParameterRuleEditingElement(editable);
			break;
		default:
			throw new RuntimeException("Unsupported type!");
		}
		return ruleComposite;
	}

	/**
	 * Creates an {@link AbstractRuleEditingElement} instance for the passed
	 * {@link BooleanExpressionType}.
	 *
	 * @param type
	 *            {@link BooleanExpressionType} determining the type of the
	 *            {@link AbstractRuleEditingElement} to be created
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	public static AbstractRuleEditingElement createRuleComposite(BooleanExpressionType type, boolean editable) {
		AbstractRuleEditingElement ruleComposite = null;
		switch (type) {
		case BOOLEAN:
			ruleComposite = new BooleanRuleEditingElement(editable);
			break;
		case AND:
		case NOT:
		case OR:
		default:
			throw new RuntimeException("Unsupported type!");
		}
		return ruleComposite;
	}

	/**
	 * Creates an {@link AbstractExpression} for the given {@link MatchingRuleType}.
	 *
	 * @param type
	 *            {@link MatchingRuleType} determining the specific {@link AbstractExpression}.
	 * @return an {@link AbstractExpression}.
	 */
	public static AbstractExpression createExpression(MatchingRuleType type) {
		StringMatchingExpression expression = new StringMatchingExpression(PatternMatchingType.EQUALS, "");

		switch (type) {
		case HTTP_PARAMETER:
			expression.setStringValueSource(new HttpParameterValueSource(""));
			break;
		case HTTP_URI:
			expression.setStringValueSource(new HttpUriValueSource());
			break;
		case IP:
			expression.setStringValueSource(new HostValueSource());
			break;
		case METHOD_SIGNATURE:
			expression.setStringValueSource(new MethodSignatureValueSource());
			break;
		case METHOD_PARAMETER:
			expression.setStringValueSource(new MethodParameterValueSource(0, ""));
			break;
		default:
			throw new RuntimeException("Unsupported type!");
		}

		expression.setSearchNodeInTrace(false);
		expression.setMaxSearchDepth(-1);

		return expression;
	}

	/**
	 * Creates an {@link AbstractExpression} for the given {@link BooleanExpressionType}.
	 *
	 * @param type
	 *            {@link BooleanExpressionType} determining the specific {@link AbstractExpression}.
	 * @return an {@link AbstractExpression}.
	 */
	public static AbstractExpression createExpression(BooleanExpressionType type) {
		switch (type) {
		case AND:
			return new AndExpression();
		case OR:
			return new OrExpression();
		case NOT:
			return new NotExpression();
		case BOOLEAN:
			return new BooleanExpression(false);
		default:
			throw new RuntimeException("Unsupported type!");
		}

	}

	/**
	 * Retrieves the {@link MatchingRuleType} of the passed {@link AbstractExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to retrieve the {@link MatchingRuleType} from
	 * @return the {@link MatchingRuleType} of the passed {@link AbstractExpression}.
	 */
	public static MatchingRuleType getMatchingRuleType(AbstractExpression expression) {
		if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpUriValueSource) {
			return MatchingRuleType.HTTP_URI;
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpParameterValueSource) {
			return MatchingRuleType.HTTP_PARAMETER;
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodSignatureValueSource) {
			return MatchingRuleType.METHOD_SIGNATURE;
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HostValueSource) {
			return MatchingRuleType.IP;
		} else if (expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodParameterValueSource) {
			return MatchingRuleType.METHOD_PARAMETER;
		}
		return null;
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
		 * Matching of a method parameter value.
		 */
		METHOD_PARAMETER,

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
			case METHOD_PARAMETER:
				return "Method Parameter Matching";
			default:
				throw new RuntimeException("Unsupported type!");
			}
		}
	}

	/**
	 * Enum for Boolean expression types.
	 *
	 * @author Alexander Wert
	 *
	 */
	public enum BooleanExpressionType {
		/**
		 * Conjunction.
		 */
		AND,

		/**
		 * Disjunction.
		 */
		OR,

		/**
		 * Negation.
		 */
		NOT,

		/**
		 * Boolean constant.
		 */
		BOOLEAN;

		@Override
		public String toString() {
			switch (this) {
			case AND:
				return "And";
			case OR:
				return "Or";
			case NOT:
				return "Not";
			case BOOLEAN:
				return "Boolean";
			default:
				throw new RuntimeException("Unsupported type!");
			}
		}
	}
}
