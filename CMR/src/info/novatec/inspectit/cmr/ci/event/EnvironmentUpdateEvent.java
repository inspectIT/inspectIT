package info.novatec.inspectit.cmr.ci.event;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.ci.factory.FunctionalMethodSensorAssignmentFactory;
import info.novatec.inspectit.cmr.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEvent;

/**
 * Event that signals that an {@link Environment} has been updated via CI.
 *
 * @author Ivan Senic
 *
 */
public class EnvironmentUpdateEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5572925794275714352L;

	/**
	 * Environment before updating.
	 */
	private final Environment before;

	/**
	 * Environment after updating.
	 */
	private final Environment after;

	/**
	 * New profiles bounded to the updated environment.
	 */
	private final Collection<Profile> addedProfiles;

	/**
	 * Removed profiles from the updated environment.
	 */
	private final Collection<Profile> removedProfiles;

	/**
	 * Default constructor.
	 *
	 * @param source
	 *            he component that published the event (never {@code null})
	 * @param before
	 *            Environment before updating.
	 * @param after
	 *            Environment after updated.
	 * @param addedProfiles
	 *            New profiles bounded to the updated environment.
	 * @param removedProfiles
	 *            Removed profiles from the updated environment.
	 */
	public EnvironmentUpdateEvent(Object source, Environment before, Environment after, Collection<Profile> addedProfiles, Collection<Profile> removedProfiles) {
		super(source);

		// check null
		if (null == before || null == after) {
			throw new IllegalArgumentException("Environment references must not be null.");
		}

		// check same id
		if (!Objects.equals(before.getId(), after.getId())) {
			throw new IllegalArgumentException("Before and after environment references must have same environment id.");
		}

		this.before = before;
		this.after = after;
		this.addedProfiles = addedProfiles;
		this.removedProfiles = removedProfiles;
	}

	/**
	 * Returns id of the environment being updated.
	 *
	 * @return Returns id of the environment being updated.
	 */
	public String getEnvironmentId() {
		return after.getId();
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment} that are contained in the removed profiles.
	 * Only active profiles are taken into account. Also includes the functional assignments that
	 * might be removed as the result of changes in the environment.
	 *
	 * @param functionalAssignmentFactory
	 *            FunctionalMethodSensorAssignmentFactory for resolving functional assignment
	 *            updates.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "removed".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getRemovedSensorAssignments(FunctionalMethodSensorAssignmentFactory functionalAssignmentFactory) {
		Collection<AbstractClassSensorAssignment<?>> removedAssignments = new ArrayList<>();

		// first deal with removed profiles
		if (CollectionUtils.isNotEmpty(removedProfiles)) {
			for (Profile profile : removedProfiles) {
				if (profile.isActive()) {
					if (CollectionUtils.isNotEmpty(profile.getMethodSensorAssignments())) {
						removedAssignments.addAll(profile.getMethodSensorAssignments());
					}
					if (CollectionUtils.isNotEmpty(profile.getExceptionSensorAssignments())) {
						removedAssignments.addAll(profile.getExceptionSensorAssignments());
					}
				}
			}
		}

		// then with possible removed functional assignments
		Collection<FunctionalMethodSensorAssignment> functionalAssignmentsBefore = functionalAssignmentFactory.getFunctionalAssignments(before);
		Collection<FunctionalMethodSensorAssignment> functionalAssignmentsAfter = functionalAssignmentFactory.getFunctionalAssignments(after);
		removedAssignments.addAll(Utils.subtractSafe(functionalAssignmentsBefore, functionalAssignmentsAfter));

		return removedAssignments;
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment} that are contained in the added profiles.
	 * Only active profiles are taken into account. Also includes the functional assignments that
	 * might be added as the result of changes in the environment.
	 *
	 * @param functionalAssignmentFactory
	 *            FunctionalMethodSensorAssignmentFactory for resolving functional assignment
	 *            updates.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "added".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getAddedSensorAssignments(FunctionalMethodSensorAssignmentFactory functionalAssignmentFactory) {
		Collection<AbstractClassSensorAssignment<?>> addedAssignments = new ArrayList<>();

		// first deal with added profiles
		if (CollectionUtils.isNotEmpty(addedProfiles)) {
			for (Profile profile : addedProfiles) {
				if (profile.isActive()) {
					if (CollectionUtils.isNotEmpty(profile.getMethodSensorAssignments())) {
						addedAssignments.addAll(profile.getMethodSensorAssignments());
					}
					if (CollectionUtils.isNotEmpty(profile.getExceptionSensorAssignments())) {
						addedAssignments.addAll(profile.getExceptionSensorAssignments());
					}
				}
			}
		}

		// then with possible added functional assignments
		Collection<FunctionalMethodSensorAssignment> functionalAssignmentsBefore = functionalAssignmentFactory.getFunctionalAssignments(before);
		Collection<FunctionalMethodSensorAssignment> functionalAssignmentsAfter = functionalAssignmentFactory.getFunctionalAssignments(after);
		addedAssignments.addAll(Utils.subtractSafe(functionalAssignmentsAfter, functionalAssignmentsBefore));

		return addedAssignments;
	}

	/**
	 * Gets {@link #after}.
	 *
	 * @return {@link #after}
	 */
	public Environment getAfter() {
		return after;
	}

	/**
	 * Gets {@link #addedProfiles}.
	 *
	 * @return {@link #addedProfiles}
	 */
	public Collection<Profile> getAddedProfiles() {
		return addedProfiles;
	}

	/**
	 * Gets {@link #removedProfiles}.
	 *
	 * @return {@link #removedProfiles}
	 */
	public Collection<Profile> getRemovedProfiles() {
		return removedProfiles;
	}

}
