package rocks.inspectit.server.ci.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEvent;

import rocks.inspectit.server.util.CollectionSubtractUtils;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;

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
	 *            the component that published the event (never {@code null})
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
	 *            SpecialMethodSensorAssignmentFactory for resolving functional assignment
	 *            updates.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "removed".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getRemovedSensorAssignments(SpecialMethodSensorAssignmentFactory functionalAssignmentFactory) {
		Collection<AbstractClassSensorAssignment<?>> removedAssignments = getSensorAssignments(removedProfiles);
		removedAssignments.addAll(getFunctionalAssignmentsDifference(functionalAssignmentFactory, before, after));
		return removedAssignments;
	}

	/**
	 * Returns all {@link AbstractClassSensorAssignment} that are contained in the added profiles.
	 * Only active profiles are taken into account. Also includes the functional assignments that
	 * might be added as the result of changes in the environment.
	 *
	 * @param functionalAssignmentFactory
	 *            SpecialMethodSensorAssignmentFactory for resolving functional assignment
	 *            updates.
	 *
	 * @return Returns all {@link AbstractClassSensorAssignment} that are "added".
	 */
	public Collection<AbstractClassSensorAssignment<?>> getAddedSensorAssignments(SpecialMethodSensorAssignmentFactory functionalAssignmentFactory) {
		Collection<AbstractClassSensorAssignment<?>> addedAssignments = getSensorAssignments(addedProfiles);
		addedAssignments.addAll(getFunctionalAssignmentsDifference(functionalAssignmentFactory, after, before));
		return addedAssignments;
	}

	/**
	 * Collects all {@link AbstractClassSensorAssignment}s from the given profiles. If profile is
	 * not active assignments from that profile will not be included.
	 *
	 * @param profiles
	 *            Collection of profiles.
	 * @return All {@link AbstractClassSensorAssignment}s from active profiles.
	 */
	private Collection<AbstractClassSensorAssignment<?>> getSensorAssignments(Collection<Profile> profiles) {
		Collection<AbstractClassSensorAssignment<?>> assignments = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(profiles)) {
			for (Profile profile : profiles) {
				if (profile.isActive()) {
					if (CollectionUtils.isNotEmpty(profile.getMethodSensorAssignments())) {
						assignments.addAll(profile.getMethodSensorAssignments());
					}
					if (CollectionUtils.isNotEmpty(profile.getExceptionSensorAssignments())) {
						assignments.addAll(profile.getExceptionSensorAssignments());
					}
				}
			}
		}

		return assignments;
	}

	/**
	 * Returns the difference from the Functional assignments for the two environments. The
	 * resulting collection will include the assignments that exist in first environment and do not
	 * exist in the second environment.
	 *
	 * @param factory
	 *            {@link SpecialMethodSensorAssignmentFactory} for resolving assignments
	 * @param e1
	 *            First environment
	 * @param e2
	 *            Second environment
	 * @return Functional assignments that are contained in the first environment, but not in
	 *         second.
	 */
	private Collection<? extends AbstractClassSensorAssignment<?>> getFunctionalAssignmentsDifference(SpecialMethodSensorAssignmentFactory factory, Environment e1, Environment e2) {
		Collection<SpecialMethodSensorAssignment> functionalAssignments1 = factory.getSpecialAssignments(e1);
		Collection<SpecialMethodSensorAssignment> functionalAssignments2 = factory.getSpecialAssignments(e2);
		return CollectionSubtractUtils.subtractSafe(functionalAssignments1, functionalAssignments2);
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
