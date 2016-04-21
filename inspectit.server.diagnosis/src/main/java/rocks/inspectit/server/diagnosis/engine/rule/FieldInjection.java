/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;

/**
 * Base class for classes which need to inject a value to a field.
 *
 * @author Claudio Waldvogel
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
	 * @see ExecutionContext Throws: RuleExecutionException if an exception soccurs
	 */
	public void execute(ExecutionContext context) {
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
	 */
	protected abstract Object determineValueToInject(ExecutionContext context);

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FieldInjection that = (FieldInjection) o;

		return new EqualsBuilder().append(getInjectee(), that.getInjectee()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getInjectee()).toHashCode();
	}
}
