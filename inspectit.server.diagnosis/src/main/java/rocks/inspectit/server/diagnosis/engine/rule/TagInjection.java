package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue.InjectionStrategy;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * FieldInjection implementation to inject {@link Tag}s, respectively the value of a {@link Tag}.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class TagInjection extends FieldInjection {

	/**
	 * The type of tag to be injected.
	 */
	private final String type;

	/**
	 * The strategy how the {@link Tag} is injected.
	 *
	 * @see InjectionStrategy
	 */
	private final InjectionStrategy injectionStrategy;

	/**
	 * Default Constructor.
	 *
	 * @param type
	 *            The type of {@link Tag} to be injected
	 * @param injectee
	 *            The injectee field
	 */
	public TagInjection(String type, Field injectee) {
		this(type, injectee, InjectionStrategy.BY_VALUE);
	}

	/**
	 * Constructor to define a InjectionStrategy.
	 *
	 * @param type
	 *            The type of {@link Tag} to be injected
	 * @param injectee
	 *            The injectee field
	 * @param injectionStrategy
	 *            The {@link InjectionStrategy}
	 * @see InjectionStrategy
	 */
	public TagInjection(String type, Field injectee, InjectionStrategy injectionStrategy) {
		super(injectee);
		checkArgument(StringUtils.isNotEmpty(type), "Tag type must not be null or empty");
		this.type = type;
		this.injectionStrategy = checkNotNull(injectionStrategy, "Injection strategy must not be null");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws RuleExecutionException
	 *             If tag injection fails.
	 */
	@Override
	public Object determineValueToInject(ExecutionContext context) throws RuleExecutionException {
		// extract the required type form the unraveled list of input Tags
		for (Tag tag : context.getRuleInput().getUnraveled()) {
			if (tag.getType().equals(getType())) {
				return InjectionStrategy.BY_VALUE.equals(getInjectionStrategy()) ? tag.getValue() : tag;
			}
		}
		throw new RuleExecutionException("Unable to find Tag: " + getType() + " in RuleInput.", context);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #type}.
	 *
	 * @return {@link #type}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets {@link #injectionStrategy}.
	 *
	 * @return {@link #injectionStrategy}
	 */
	public InjectionStrategy getInjectionStrategy() {
		return injectionStrategy;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "TagInjection [type=" + type + ", injectionStrategy=" + injectionStrategy + ", getInjectee()=" + getInjectee() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.injectionStrategy == null) ? 0 : this.injectionStrategy.hashCode());
		result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
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
		TagInjection other = (TagInjection) obj;
		if (this.injectionStrategy != other.injectionStrategy) {
			return false;
		}
		if (this.type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!this.type.equals(other.type)) {
			return false;
		}
		return true;
	}
}
