/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import com.google.common.base.Strings;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * FieldInjection implementation to inject session variables. Each SessionVariableInjection reflects
 * a {@link SessionVariable} annotation.
 *
 * @author Claudio Waldvogel
 * @see SessionVariables
 */
public class SessionVariableInjection extends FieldInjection {

	/**
	 * The name of the session variable to be injected.
	 */
	private final String variableName;

	/**
	 * Flag to indicate if the session variable is optional.
	 */
	private final boolean optional;

	/**
	 * Default Constructor.
	 *
	 * @param name
	 *            The name of the variable
	 * @param optional
	 *            Optional flag
	 * @param field
	 *            The target field
	 */
	public SessionVariableInjection(String name, boolean optional, Field field) {
		super(field);
		checkArgument(!Strings.isNullOrEmpty(name));
		this.variableName = name;
		this.optional = optional;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object determineValueToInject(ExecutionContext context) {
		// Fail fast if value is missing completely and not optional.
		if (!context.getSessionParameters().containsKey(getVariableName()) && !isOptional()) {
			throw new RuleExecutionException("Non optional session variable \'" + getVariableName() + "\' not available.", context);
		}
		// This might return null if a null value is available for a certain key.
		// Needs to be discussed how to handle this.
		return context.getSessionParameters().get(getVariableName());
	}

	/**
	 * Gets {@link #variableName}.
	 *
	 * @return {@link #variableName}
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * Gets {@link #optional}.
	 *
	 * @return {@link #optional}
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "SessionVariableInjection [variableName=" + variableName + ", optional=" + optional + ", getInjectee()=" + getInjectee() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (optional ? 1231 : 1237);
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SessionVariableInjection other = (SessionVariableInjection) obj;
		if (optional != other.optional) {
			return false;
		}
		if (variableName == null) {
			if (other.variableName != null) {
				return false;
			}
		} else if (!variableName.equals(other.variableName)) {
			return false;
		}
		return true;
	}

}
