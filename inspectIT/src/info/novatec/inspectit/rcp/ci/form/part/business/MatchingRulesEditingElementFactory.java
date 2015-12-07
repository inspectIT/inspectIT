package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.AndExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.expression.impl.NotExpression;
import info.novatec.inspectit.ci.business.expression.impl.OrExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.PatternMatchingType;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.HostValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.HttpUriValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.MethodParameterValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.MethodSignatureValueSource;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.BooleanRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.HttpParameterRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.HttpUriMatchingRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.IpRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.MethodParameterRuleEditingElement;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.impl.MethodSignatureRuleEditingElement;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

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
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 * @return Returns {@link AbstractExpression} instance, or {@code null} if no
	 *         {@link AbstractExpression} instance can be created for the passed
	 *         {@link AbstractExpression}.
	 *
	 */
	public static AbstractRuleEditingElement<?> createRuleComposite(AbstractExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		if (expression instanceof StringMatchingExpression) {
			return createRuleComposite(getMatchingRuleType(expression), (StringMatchingExpression) expression, editable, validatorRegistry);
		} else if (expression instanceof BooleanExpression) {
			return createRuleComposite(BooleanExpressionType.BOOLEAN, (BooleanExpression) expression, editable, validatorRegistry);
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
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	public static AbstractRuleEditingElement<?> createRuleComposite(MatchingRuleType type, boolean editable, IValidatorRegistry validatorRegistry) {
		StringMatchingExpression expression = new StringMatchingExpression();
		expression.setMatchingType(PatternMatchingType.CONTAINS);
		expression.setSnippet("");
		return createRuleComposite(type, expression, editable, validatorRegistry);
	}

	/**
	 * Creates an {@link AbstractRuleEditingElement} instance for the passed
	 * {@link MatchingRuleType}.
	 *
	 * @param type
	 *            MatchingRuleType determining the {@link AbstractRuleEditingElement} instance.
	 * @param expression
	 *            {@link StringMatchingExpression} instance used for initialization
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	public static AbstractRuleEditingElement<?> createRuleComposite(MatchingRuleType type, StringMatchingExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		AbstractRuleEditingElement<?> ruleComposite = null;
		switch (type) {
		case HTTP_PARAMETER:
			ruleComposite = new HttpParameterRuleEditingElement(expression, editable, validatorRegistry);
			break;
		case HTTP_URI:
			ruleComposite = new HttpUriMatchingRuleEditingElement(expression, editable, validatorRegistry);
			break;
		case IP:
			ruleComposite = new IpRuleEditingElement(expression, editable, validatorRegistry);
			break;
		case METHOD_SIGNATURE:
			ruleComposite = new MethodSignatureRuleEditingElement(expression, editable, validatorRegistry);
			break;
		case METHOD_PARAMETER:
			ruleComposite = new MethodParameterRuleEditingElement(expression, editable, validatorRegistry);
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
	 * @param expression
	 *            {@link StringMatchingExpression} instance used for initialization
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	public static AbstractRuleEditingElement<?> createRuleComposite(BooleanExpressionType type, BooleanExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		if (expression == null) {
			expression = new BooleanExpression();
		}
		if (type.equals(BooleanExpressionType.BOOLEAN)) {
			return new BooleanRuleEditingElement(expression, editable, validatorRegistry);
		} else {
			throw new RuntimeException("Unsupported type!");
		}
	}

	/**
	 * Creates an {@link AbstractExpression} for the given {@link IRulesExpressionType}.
	 *
	 * @param type
	 *            {@link IRulesExpressionType} determining the specific {@link AbstractExpression}.
	 * @return an {@link AbstractExpression}.
	 */
	public static AbstractExpression createExpression(IRulesExpressionType type) {
		if (type instanceof MatchingRuleType) {
			return createMatchingRuleExpression((MatchingRuleType) type);
		} else if (type instanceof BooleanExpressionType) {
			return createBooleanExpression((BooleanExpressionType) type);
		}
		throw new RuntimeException("Unsupported type!");
	}

	/**
	 * Creates an {@link AbstractExpression} for the given {@link MatchingRuleType}.
	 *
	 * @param type
	 *            {@link MatchingRuleType} determining the specific {@link AbstractExpression}.
	 * @return an {@link AbstractExpression}.
	 */
	private static AbstractExpression createMatchingRuleExpression(MatchingRuleType type) {
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

		return expression;
	}

	/**
	 * Creates an {@link AbstractExpression} for the given {@link BooleanExpressionType}.
	 *
	 * @param type
	 *            {@link BooleanExpressionType} determining the specific {@link AbstractExpression}.
	 * @return an {@link AbstractExpression}.
	 */
	private static AbstractExpression createBooleanExpression(BooleanExpressionType type) {
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
		if (expression instanceof StringMatchingExpression) {
			StringValueSource source = ((StringMatchingExpression) expression).getStringValueSource();
			return getMatchingRuleType(source);
		}
		throw new RuntimeException("Unsupported expression type!");
	}

	/**
	 * Retrieves the {@link MatchingRuleType} of the passed {@link StringValueSource}.
	 *
	 * @param source
	 *            {@link StringValueSource} to retrieve the {@link MatchingRuleType} from
	 * @return the {@link MatchingRuleType} of the passed {@link StringValueSource}.
	 */
	public static MatchingRuleType getMatchingRuleType(StringValueSource source) {
		if (source instanceof HttpUriValueSource) {
			return MatchingRuleType.HTTP_URI;
		} else if (source instanceof HttpParameterValueSource) {
			return MatchingRuleType.HTTP_PARAMETER;
		} else if (source instanceof MethodSignatureValueSource) {
			return MatchingRuleType.METHOD_SIGNATURE;
		} else if (source instanceof HostValueSource) {
			return MatchingRuleType.IP;
		} else if (source instanceof MethodParameterValueSource) {
			return MatchingRuleType.METHOD_PARAMETER;
		}

		throw new RuntimeException("Unsupported string value source!");
	}

	/**
	 * Retrieves the {@link BooleanExpressionType} of the passed {@link AbstractExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to retrieve the {@link BooleanExpressionType} from
	 * @return the {@link BooleanExpressionType} of the passed {@link AbstractExpression}.
	 */
	public static BooleanExpressionType getBooleanExpressionType(AbstractExpression expression) {
		if (expression instanceof AndExpression) {
			return BooleanExpressionType.AND;
		} else if (expression instanceof OrExpression) {
			return BooleanExpressionType.OR;
		} else if (expression instanceof NotExpression) {
			return BooleanExpressionType.NOT;
		} else if (expression instanceof BooleanExpression) {
			return BooleanExpressionType.BOOLEAN;
		}
		return null;
	}

	/**
	 * Retrieves the {@link IRulesExpressionType} of the passed {@link AbstractExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to retrieve the {@link IRulesExpressionType} from
	 * @return the {@link IRulesExpressionType} of the passed {@link AbstractExpression}.
	 */
	public static IRulesExpressionType getRulesExpressionType(AbstractExpression expression) {
		if (expression instanceof StringMatchingExpression) {
			return getMatchingRuleType(expression);
		} else {
			return getBooleanExpressionType(expression);
		}
	}

	/**
	 * Common Wrapper for {@link MatchingRuleType} and {@link BooleanExpressionType}.
	 *
	 * @author Alexander Wert
	 *
	 */
	public interface IRulesExpressionType {
		/**
		 * Returns the icon image key for the given type.
		 *
		 * @return Returns the icon image key for the given type.
		 */
		String getImageKey();
	}

	/**
	 * Enum for matching rules types.
	 *
	 * @author Alexander Wert
	 *
	 */
	public enum MatchingRuleType implements IRulesExpressionType {
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getImageKey() {
			switch (this) {
			case HTTP_PARAMETER:
				return InspectITImages.IMG_HTTP_PARAMETER;
			case HTTP_URI:
				return InspectITImages.IMG_GLOBE;
			case IP:
				return InspectITImages.IMG_SERVER;
			case METHOD_SIGNATURE:
				return InspectITImages.IMG_METHOD;
			case METHOD_PARAMETER:
				return InspectITImages.IMG_METHOD_PARAMETER;
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
	public enum BooleanExpressionType implements IRulesExpressionType {
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getImageKey() {
			switch (this) {
			case AND:
				return InspectITImages.IMG_CONJUNCTION;
			case OR:
				return InspectITImages.IMG_DISJUNCTION;
			case NOT:
				return InspectITImages.IMG_NOT;
			case BOOLEAN:
				return InspectITImages.IMG_YES_NO;
			default:
				throw new RuntimeException("Unsupported type!");
			}
		}
	}
}
