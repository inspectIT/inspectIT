package rocks.inspectit.server.diagnosis.engine.rule;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Defines the condition, if a rule can be executed or not. The <code>FireCondition</code> basically
 * checks if a predefined set of tag types is already available in the engine.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class FireCondition {

	/**
	 * The set of tag types which have to be available to execute a rule.
	 */
	private final Set<String> tagTypes;

	/**
	 * Default constructor.
	 *
	 * @param tagTypes
	 *            The types this <code>FireCondition</code> requires.
	 */
	public FireCondition(Set<String> tagTypes) {
		this.tagTypes = ImmutableSet.copyOf(tagTypes);
	}

	/**
	 * Checks if all required {@link #tagTypes} are satisfied by a provided set of tag types.
	 *
	 * @param offer
	 *            The set of tag types to be checked.
	 * @return true if a rule can be executed, false otherwise.
	 */
	public boolean canFire(Set<String> offer) {
		return (null != offer) && offer.containsAll(tagTypes);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #tagTypes}.
	 *
	 * @return {@link #tagTypes}
	 */
	public Set<String> getTagTypes() {
		return tagTypes;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "FireCondition{" + "tagTypes=" + tagTypes + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.tagTypes == null) ? 0 : this.tagTypes.hashCode());
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
		FireCondition other = (FireCondition) obj;
		if (this.tagTypes == null) {
			if (other.tagTypes != null) {
				return false;
			}
		} else if (!this.tagTypes.equals(other.tagTypes)) {
			return false;
		}
		return true;
	}
}
