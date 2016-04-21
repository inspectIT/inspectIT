package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Defines the condition, if a rule can be executed or not. The <code>FireCondition</code> basically
 * checks if a predefined set of tag types is already available in the engine.
 *
 * @author Claudio Waldvogel
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
		checkNotNull(offer, "The offer must not be null!");
		return offer.containsAll(tagTypes);
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

	@Override
	public String toString() {
		return "FireCondition{" + "tagTypes=" + tagTypes + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FireCondition that = (FireCondition) o;

		return getTagTypes() != null ? getTagTypes().equals(that.getTagTypes()) : that.getTagTypes() == null;

	}

	@Override
	public int hashCode() {
		return getTagTypes() != null ? getTagTypes().hashCode() : 0;
	}

}
