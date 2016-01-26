package rocks.inspectit.server.ci.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.context.ApplicationEvent;

import rocks.inspectit.server.util.CollectionSubtractUtils;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * Event that signals that an {@link Profile} has been updated via CI.
 *
 * @author Ivan Senic
 *
 */
public class ProfileUpdateEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 1040042583468927682L;

	/**
	 * Profile before updating.
	 */
	private final Profile before;

	/**
	 * Profile after updating.
	 */
	private final Profile after;

	/**
	 * Default constructor.
	 *
	 * @param source
	 *            the component that published the event (never {@code null})
	 * @param before
	 *            Profile before updating.
	 * @param after
	 *            Profile after updated.
	 */
	public ProfileUpdateEvent(Object source, Profile before, Profile after) {
		super(source);

		// check null
		if (null == before || null == after) {
			throw new IllegalArgumentException("Profile references must not be null.");
		}

		// check same id
		if (!Objects.equals(before.getId(), after.getId())) {
			throw new IllegalArgumentException("Before and after profile references must have same profile id.");
		}

		this.before = before;
		this.after = after;
	}

	/**
	 * Returns id of the profile being updated.
	 *
	 * @return Returns id of the profile being updated.
	 */
	public String getProfileId() {
		return after.getId();
	}

	/**
	 * Returns if the profile is active.
	 *
	 * @return Returns if the profile is active.
	 */
	public boolean isProfileActive() {
		return after.isActive();
	}

	/**
	 * If profile was deactivated as the result of the update action.
	 *
	 * @return If profile was deactivated as the result of the update action.
	 */
	public boolean isProfileDeactivated() {
		return before.isActive() && !after.isActive();
	}

	/**
	 * If profile was activated as the result of the update action.
	 *
	 * @return If profile was activated as the result of the update action.
	 */
	public boolean isProfileActivated() {
		return !before.isActive() && after.isActive();
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment} that are "removed" as result of this
	 * update. If profile was deactivated then it means that all assignments before the update are
	 * considered for removal.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "removed".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getRemovedSensorAssignments() {
		if (isProfileDeactivated()) {
			// if deactivated then we consider all old assignment to be for removal
			return getAllSensorAssignments(before);
		} else if (isProfileActivated()) {
			// if it was activated then nothing is for removal
			return Collections.emptyList();
		} else {
			// otherwise find the difference
			return getAssignmentsDifference(before, after);
		}
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment} that are "added" as result of this update.
	 * If profile was activated then it means that all assignments after the update are considered
	 * for adding.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "removed".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getAddedSensorAssignments() {
		if (isProfileDeactivated()) {
			// if it was deactivated then nothing is for adding
			return Collections.emptyList();
		} else if (isProfileActivated()) {
			// if activated then we consider all new assignment to be for adding
			return getAllSensorAssignments(after);
		} else {
			// otherwise find the difference
			return getAssignmentsDifference(after, before);
		}
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment}s from the profile.
	 *
	 * @param profile
	 *            {@link Profile}
	 * @return Returns all {@link AbstractClassSensorAssignment}s from the profile.
	 */
	private Collection<AbstractClassSensorAssignment<?>> getAllSensorAssignments(Profile profile) {
		Collection<AbstractClassSensorAssignment<?>> results = new ArrayList<>();
		results.addAll(profile.getMethodSensorAssignments());
		results.addAll(profile.getExceptionSensorAssignments());
		return results;
	}

	/**
	 * Finds {@link AbstractClassSensorAssignment}s that exists in first profile and not in the
	 * second one.
	 *
	 * @param p1
	 *            First profile
	 * @param p2
	 *            Second profile
	 * @return {@link AbstractClassSensorAssignment}s that exists in first profile and not in the
	 *         second one.
	 */
	private Collection<AbstractClassSensorAssignment<?>> getAssignmentsDifference(Profile p1, Profile p2) {
		Collection<AbstractClassSensorAssignment<?>> results1 = getAllSensorAssignments(p1);
		Collection<AbstractClassSensorAssignment<?>> results2 = getAllSensorAssignments(p2);
		return CollectionSubtractUtils.subtractSafe(results1, results2);
	}

}
