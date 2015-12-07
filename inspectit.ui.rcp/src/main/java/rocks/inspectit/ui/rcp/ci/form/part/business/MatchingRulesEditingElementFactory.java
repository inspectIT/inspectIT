package rocks.inspectit.ui.rcp.ci.form.part.business;

import org.springframework.http.HttpMethod;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.AndExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.NotExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.OrExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.PatternMatchingType;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HostValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpRequestMethodValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpUriValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodSignatureValueSource;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.BooleanRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.HttpParameterRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.HttpRequestMethodRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.HttpUriRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.IpRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.MethodParameterRuleEditingElement;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl.MethodSignatureRuleEditingElement;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;

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
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 * @return Returns {@link AbstractExpression} instance, or {@code null} if no
	 *         {@link AbstractExpression} instance can be created for the passed
	 *         {@link AbstractExpression}.
	 *
	 */
	public static AbstractRuleEditingElement<?> createRuleComposite(AbstractExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		if (expression instanceof StringMatchingExpression) {
			return createRuleComposite(getMatchingRuleType(expression), (StringMatchingExpression) expression, editable, upstreamValidationManager);
		} else if (expression instanceof BooleanExpression) {
			return createRuleComposite((BooleanExpression) expression, editable, upstreamValidationManager);
		}
		return null;
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
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	private static AbstractRuleEditingElement<?> createRuleComposite(MatchingRuleType type, StringMatchingExpression expression, boolean editable,
			AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		AbstractRuleEditingElement<?> ruleComposite = null;
		switch (type) {
		case HTTP_PARAMETER:
			ruleComposite = new HttpParameterRuleEditingElement(expression, editable, upstreamValidationManager);
			break;
		case HTTP_URI:
			ruleComposite = new HttpUriRuleEditingElement(expression, editable, upstreamValidationManager);
			break;
		case IP:
			ruleComposite = new IpRuleEditingElement(expression, editable, upstreamValidationManager);
			break;
		case METHOD_SIGNATURE:
			ruleComposite = new MethodSignatureRuleEditingElement(expression, editable, upstreamValidationManager);
			break;
		case METHOD_PARAMETER:
			ruleComposite = new MethodParameterRuleEditingElement(expression, editable, upstreamValidationManager);
			break;
		case HTTP_REQUEST_METHOD:
			ruleComposite = new HttpRequestMethodRuleEditingElement(expression, editable, upstreamValidationManager);
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
	 * @param expression
	 *            {@link StringMatchingExpression} instance used for initialization
	 * @param editable
	 *            indicates whether the created element shell be editable or read-only
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 * @return Returns an {@link AbstractRuleEditingElement} instance.
	 */
	private static AbstractRuleEditingElement<?> createRuleComposite(BooleanExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		if (expression == null) {
			throw new IllegalArgumentException("Expression must not be null!");
		}

		return new BooleanRuleEditingElement(expression, editable, upstreamValidationManager);
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
		case HTTP_REQUEST_METHOD:
			expression.setStringValueSource(new HttpRequestMethodValueSource());
			expression.setSnippet(HttpMethod.GET.toString());
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
		throw new IllegalArgumentException("Unsupported expression type!");
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
		} else if (source instanceof HttpRequestMethodValueSource) {
			return MatchingRuleType.HTTP_REQUEST_METHOD;
		}

		throw new IllegalArgumentException("Unsupported string value source!");
	}

	/**
	 * Retrieves the {@link BooleanExpressionType} of the passed {@link AbstractExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} to retrieve the {@link BooleanExpressionType} from
	 * @return the {@link BooleanExpressionType} of the passed {@link AbstractExpression}.
	 */
	private static BooleanExpressionType getBooleanExpressionType(AbstractExpression expression) {
		if (expression instanceof AndExpression) {
			return BooleanExpressionType.AND;
		} else if (expression instanceof OrExpression) {
			return BooleanExpressionType.OR;
		} else if (expression instanceof NotExpression) {
			return BooleanExpressionType.NOT;
		} else if (expression instanceof BooleanExpression) {
			return BooleanExpressionType.BOOLEAN;
		}
		throw new IllegalArgumentException("Unsupported expression type!");
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
		 * Matching of the HTTP request method.
		 */
		HTTP_REQUEST_METHOD,

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
			case HTTP_REQUEST_METHOD:
				return "HTTP Request Method Matching";
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
				return InspectITImages.IMG_BROWSER;
			case HTTP_REQUEST_METHOD:
				return InspectITImages.IMG_HTTP_METHOD;
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

	/**
	 * This class represents an invalid expression used to avoid handling of null as value for
	 * invalid expressions.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static final class InvalidExpression extends AbstractExpression {
		/**
		 * Singleton instance.
		 */
		private static InvalidExpression singletonInstance;

		/**
		 * Returns the singleton instance.
		 *
		 * @return Returns the singleton instance.
		 */
		public static synchronized InvalidExpression getInstance() {
			if (null == singletonInstance) {
				singletonInstance = new InvalidExpression();
			}
			return singletonInstance;
		}

		/**
		 * Private Constructor for singleton.
		 */
		private InvalidExpression() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean evaluate(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
			return false;
		}

	}
}
