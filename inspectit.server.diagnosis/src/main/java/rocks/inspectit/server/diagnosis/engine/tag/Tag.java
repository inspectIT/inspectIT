package rocks.inspectit.server.diagnosis.engine.tag;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import rocks.inspectit.server.diagnosis.engine.rule.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;

/**
 * A <code>Tag</code> represents the actual result of a rule execution. Everything what is created
 * by a {@link ActionMethod} is wrapped in a <code>Tag</code>. A <code>Tag</code> can thus be
 * considered as a qualified attachment to the currently analyzed object. Depending on the
 * {@link ActionMethod} configuration/implementation, one rule can produce multiple <code>Tag</code>
 * s. <b>Be aware that all produced Tags are of the same type. It is invalid/impossible to create
 * different Tag types</b>. All <code>Tag</code>s are gathered to one {@link RuleOutput} and stored
 * in the {@link IRuleOutputStorage}.
 *
 * @author Claudio Waldvogel
 * @see IRuleOutputStorage
 * @see RuleOutput
 */
public class Tag {

	/**
	 * The type of this <code>Tag</code>. This is a simply, but unique, identifier.
	 */
	private final String type;

	/**
	 * The value of this Tag. No assumptions are made what the value is. Everything is a valid
	 * value.
	 */
	private final Object value;

	/**
	 * The parent Tag.
	 */
	private final Tag parent;

	/**
	 * The state of this Tag.
	 *
	 * <pre>
	 * <ul>
	 *     <li><b>TagState.LEAF</b>, if the value is was not processed any further.</li>
	 *     <li><b>TagState.PARENT</b>, if the value was processed to create further <code>Tag</code>s</li>
	 * </ul>
	 * </pre>
	 */
	private TagState state = TagState.LEAF;

	// -------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------

	/**
	 * Default Constructor.
	 *
	 * @param type
	 *            The type of this <code>Tag</code>. The is an equivalent to a Tag's name.
	 */
	public Tag(String type) {
		this(type, null, null);
	}

	/**
	 * Constructor to create a Tag with an associated value.
	 *
	 * @param type
	 *            The type of the <code>Tag</code>
	 * @param value
	 *            The value of this <code>Tag</code>
	 */
	public Tag(String type, Object value) {
		this(type, value, null);
	}

	/**
	 * Creates a Tag with a value and a parent.
	 *
	 * @param type
	 *            The type of this <code>Tag</code>.
	 * @param value
	 *            The value of this <code>Tag</code>
	 * @param parent
	 *            The <code>Tag</code> which is considered as the predecessor of this
	 *            <code>Tag</code>.
	 */
	public Tag(String type, Object value, Tag parent) {
		this.value = value;
		this.type = type;
		this.parent = parent;
		if (this.parent != null) {
			this.parent.markParent();
		}
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
	 * Gets {@link #value}.
	 *
	 * @return {@link #value}
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Gets {@link #parent}.
	 *
	 * @return {@link #parent}
	 */
	public Tag getParent() {
		return parent;
	}

	/**
	 * Gets {@link #state}.
	 *
	 * @return {@link #state}
	 */
	public TagState getState() {
		return state;
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	/**
	 * Utility method to mark a <code>Tag</code> as parent by setting the state to
	 * {@link TagState#PARENT}.
	 */
	private synchronized void markParent() {
		if (state.equals(TagState.LEAF)) {
			state = TagState.PARENT;
		}
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("type", type).append("value", value).append("parent", parent).append("state", state).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Tag tag = (Tag) o;

		return new EqualsBuilder().append(getType(), tag.getType()).append(getValue(), tag.getValue()).append(getParent(), tag.getParent()).append(getState(), tag.getState()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getType()).append(getValue()).append(getParent()).append(getState()).toHashCode();
	}
}
