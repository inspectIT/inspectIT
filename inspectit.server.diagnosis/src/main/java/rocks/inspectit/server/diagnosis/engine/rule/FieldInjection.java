package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;

import org.apache.commons.lang.builder.ToStringBuilder;

import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;

/**
 * Base class for classes which need to inject a value to a field.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public abstract class FieldInjection {

	/**
	 * The target field where to inject a value.
	 */
	private final Field injectee;

	/**
	 * Default constructor.
	 *
	 * @param injectee
	 *            The target field
	 */
	public FieldInjection(Field injectee) {
		this.injectee = checkNotNull(injectee, "The injectee must not be null.");
		// Ensure that field is accessible
		this.injectee.setAccessible(true);
	}

	/**
	 * Performs the injection.
	 *
	 * @param context
	 *            The current valid {@link ExecutionContext}
	 * @throws RuleExecutionException
	 *             If injection fails.
	 * @see ExecutionContext Throws: RuleExecutionException if an exception soccurs
	 */
	public void execute(ExecutionContext context) throws RuleExecutionException {
		Object toInject = determineValueToInject(context);
		try {
			getInjectee().set(context.getInstance(), toInject);
		} catch (IllegalAccessException e) {
			throw new RuleExecutionException("Failed to injected \'" + toInject + "\' to \'" + getInjectee().getName() + "\'", context, e);
		}
	}

	/**
	 * Method determines the value to be injected to {@link #injectee}.
	 *
	 * @param context
	 *            The {@link ExecutionContext}
	 * @return Any object, or null, to be injected to {@link #injectee}
	 * @throws RuleExecutionException
	 *             If injection fails.
	 */
	protected abstract Object determineValueToInject(ExecutionContext context) throws RuleExecutionException;

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #injectee}.
	 *
	 * @return {@link #injectee}
	 */
	public Field getInjectee() {
		return injectee;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("target", injectee).toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.injectee == null) ? 0 : this.injectee.hashCode());
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
		FieldInjection other = (FieldInjection) obj;
		if (this.injectee == null) {
			if (other.injectee != null) {
				return false;
			}
		} else if (!this.injectee.equals(other.injectee)) {
			return false;
		}
		return true;
	}
}
