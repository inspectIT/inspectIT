package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.server.instrumentation.config.filter.AssignmentFilterProvider;
import rocks.inspectit.server.instrumentation.config.filter.ClassSensorAssignmentFilter;
import rocks.inspectit.server.instrumentation.config.filter.JmxSensorAssignmentFilter;
import rocks.inspectit.server.instrumentation.config.filter.MethodSensorAssignmentFilter;
import rocks.inspectit.shared.cs.ci.Environment;

/**
 * Base class for all appliers.
 * 
 * @author Ivan Senic
 */
public class GenericApplier {

	/**
	 * Assignment filter provider for providing class, method and jmx matching filters.
	 */
	protected AssignmentFilterProvider assignmentFilterProvider = new AssignmentFilterProvider();

	/**
	 * Environment belonging to the assignment.
	 */
	protected Environment environment;

	/**
	 * Default constructor.
	 *
	 * @param environment
	 *            Environment belonging to the assignment.
	 */
	protected GenericApplier(Environment environment) {
		if (null == environment) {
			throw new IllegalArgumentException("Environment can not be null in instrumentation applier.");
		}
		this.environment = environment;
	}

	/**
	 * Gets {@link AssignmentFilterProvider#getClassSensorAssignmentFilter()}.
	 *
	 * @return {@link AssignmentFilterProvider#getClassSensorAssignmentFilter()}
	 */
	protected ClassSensorAssignmentFilter getClassSensorAssignmentFilter() {
		return assignmentFilterProvider.getClassSensorAssignmentFilter();
	}

	/**
	 * Gets {@link AssignmentFilterProvider#getMethodsSensorAssignmentFilter()}.
	 *
	 * @return {@link AssignmentFilterProvider#getMethodSensorAssignmentFilter()}
	 */
	protected MethodSensorAssignmentFilter getMethodSensorAssignmentFilter() {
		return assignmentFilterProvider.getMethodSensorAssignmentFilter();
	}

	/**
	 * Gets {@link AssignmentFilterProvider#getJmxSensorAssignmentFilter()}.
	 *
	 * @return {@link AssignmentFilterProvider#getJmxSensorAssignmentFilter()}
	 */
	protected JmxSensorAssignmentFilter getJmxSensorAssignmentFilter() {
		return assignmentFilterProvider.getJmxSensorAssignmentFilter();
	}

}
