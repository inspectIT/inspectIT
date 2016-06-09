package rocks.inspectit.server.instrumentation.config.filter;

/**
 * Defaults for the class, method and jmx filters.
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
	 * Filter to use for the JMX bean/attribute matching.
	 */
	private static final JmxSensorAssignmentFilter JMX_SENSOR_ASSIGNMENT_FILTER = new JmxSensorAssignmentFilter();

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

	/**
	 * Gets {@link #JMX_SENSOR_ASSIGNMENT_FILTER}.
	 *
	 * @return {@link #JMX_SENSOR_ASSIGNMENT_FILTER}
	 */
	public JmxSensorAssignmentFilter getJmxSensorAssignmentFilter() {
		return JMX_SENSOR_ASSIGNMENT_FILTER;
	}

}
