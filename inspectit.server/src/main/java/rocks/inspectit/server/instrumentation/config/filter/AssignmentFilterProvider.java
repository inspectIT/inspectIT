package rocks.inspectit.server.instrumentation.config.filter;

/**
 * Defaults for the class and method filters.
 *
 * @author Ivan Senic
 *
 */
public class AssignmentFilterProvider {

	/**
	 * Filter to use for the class matching.
	 */
	private static final ClassSensorAssignmentFilter CLASS_SENSOR_ASSIGNMENT_FILTER = new ClassSensorAssignmentFilter();

	/**
	 * Filter to use for the method matching.
	 */
	private static final MethodSensorAssignmentFilter METHOD_SENSOR_ASSIGNMENT_FILTER = new MethodSensorAssignmentFilter();

	/**
	 * Gets {@link #CLASS_SENSOR_ASSIGNMENT_FILTER}.
	 *
	 * @return {@link #CLASS_SENSOR_ASSIGNMENT_FILTER}
	 */
	public ClassSensorAssignmentFilter getClassSensorAssignmentFilter() {
		return CLASS_SENSOR_ASSIGNMENT_FILTER;
	}

	/**
	 * Gets {@link #METHOD_SENSOR_ASSIGNMENT_FILTER}.
	 *
	 * @return {@link #METHOD_SENSOR_ASSIGNMENT_FILTER}
	 */
	public MethodSensorAssignmentFilter getMethodSensorAssignmentFilter() {
		return METHOD_SENSOR_ASSIGNMENT_FILTER;
	}

}
