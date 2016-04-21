package rocks.inspectit.server.diagnosis.engine.rule;

import java.util.Collection;
import java.util.Collections;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * Value object defining the input for a single rule execution.
 *
 * @author Claudio Waldvogel, Alexander Wert
 * @see Tag
 */
public class RuleInput {

	/**
	 * The root {@link Tag} which is need by a {@link RuleDefinition} to execute.
	 *
	 * @see RuleDefinition
	 * @see Tag
	 */
	private final Tag root;

	/**
	 * A collection of {@link Tag}s which were extracted from the {@link #root} {@link Tag}. The
	 * exact content of the unraveled collection depends on which {@link Tag}s the actual rule
	 * implementation needs to execute.. The {@link #root} Tag itself is present in the unraveled
	 * collection as well.
	 *
	 * @see FireCondition
	 * @see TagInjection
	 */
	private final Collection<Tag> unraveled;

	/**
	 * Default Constructor.
	 *
	 * @param root
	 *            The root {@link Tag}.
	 */
	public RuleInput(Tag root) {
		this(root, Collections.singleton(root));
	}

	/**
	 * Constructor with unraveled collection.
	 *
	 * @param root
	 *            The root {@link Tag}.
	 * @param unraveled
	 *            The unraveled collection of Tags. ({@link #unraveled}).
	 * @see Tag
	 */
	public RuleInput(Tag root, Collection<Tag> unraveled) {
		this.root = root;
		this.unraveled = unraveled;
	}

	/**
	 * Gets {@link #root}.
	 *
	 * @return {@link #root}
	 */
	public Tag getRoot() {
		return root;
	}

	/**
	 * Gets {@link #unraveled}.
	 *
	 * @return {@link #unraveled}
	 */
	public Collection<Tag> getUnraveled() {
		return unraveled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "RuleInput [root=" + this.root + ", unraveled=" + this.unraveled + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.root == null) ? 0 : this.root.hashCode());
		result = (prime * result) + ((this.unraveled == null) ? 0 : this.unraveled.hashCode());
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
		RuleInput other = (RuleInput) obj;
		if (this.root == null) {
			if (other.root != null) {
				return false;
			}
		} else if (!this.root.equals(other.root)) {
			return false;
		}
		if (this.unraveled == null) {
			if (other.unraveled != null) {
				return false;
			}
		} else if (!this.unraveled.equals(other.unraveled)) {
			return false;
		}
		return true;
	}
}
