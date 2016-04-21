package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ReflectionUtils;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;

/**
 * Defines a condition method of a rule implementation. A <code>ConditionMethod</code> reflects the
 * {@link Condition} annotation.
 *
 * @author Claudio Waldvogel
 * @see Condition
 */
public class ConditionMethod {

	/**
	 * The name of the condition.
	 *
	 * @see Condition#name()
	 */
	private final String name;

	/**
	 * A hint why the condition failed.
	 *
	 * @see Condition#hint()
	 */
	private final String hint;

	/**
	 * The actual condition implementation of a rule. As shown in listing, a valid condition method
	 * implementation has no parameters and returns a boolean.
	 * <p>
	 *
	 * <pre>
	 * {@code
	 *     &#64;literal @Condition(name = "MyCondition", hint = "Some useful information")
	 *     public boolean condition(){
	 *         return true | false;
	 *     }
	 * }
	 * </pre>
	 */
	private final Method method;

	/**
	 * Default Constructor.
	 *
	 * @param name
	 *            The name of the condition
	 * @param hint
	 *            A hint why the condition failed
	 * @param method
	 *            The actual backing implementation of the condition method
	 */
	public ConditionMethod(String name, String hint, Method method) {
		this.method = checkNotNull(method);
		this.name = StringUtils.defaultIfEmpty(name, this.method.getName());
		this.hint = hint;
	}

	/**
	 * Executes this {@link ConditionMethod}. If the #method does not succeed a
	 * {@link ConditionFailure} is returned. Otherwise null is returned.
	 *
	 * @param context
	 *            The current executing {@link ExecutionContext}
	 * @return A {@link ConditionFailure} if condition fails, null otherwise
	 * @see ExecutionContext
	 * @see ConditionFailure
	 */
	public ConditionFailure execute(ExecutionContext context) {
		try {
			boolean valid = (boolean) ReflectionUtils.invokeMethod(getMethod(), context.getInstance());
			if (!valid) {
				// Store information about the failed condition for later usage
				return new ConditionFailure(getName(), getHint());
			}
			return null;
		} catch (Exception e) {
			throw new RuleExecutionException("Invocation of condition method failed.", context, e);
		}
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets {@link #hint}.
	 *
	 * @return {@link #hint}
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public Method getMethod() {
		return method;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	@Override
	public String toString() {
		return "ConditionMethod{" + "name='" + name + '\'' + ", hint='" + hint + '\'' + ", method=" + method + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hint == null) ? 0 : hint.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConditionMethod other = (ConditionMethod) obj;
		if (hint == null) {
			if (other.hint != null) {
				return false;
			}
		} else if (!hint.equals(other.hint)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
