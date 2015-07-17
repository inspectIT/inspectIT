package info.novatec.inspectit.rcp.formatter;

/**
 * This enumeration contains strings/tooltips for the availability of different sensor-types.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum SensorTypeAvailabilityEnum {

	/**
	 * Tooltip for SystemInformation sensor-type.
	 */
	SYS_INF_NA("This item is not available. You have to activate the 'SystemInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for CpuInformation sensor-type.
	 */
	CPU_INF_NA("This item is not available. You have to activate the 'CpuInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for ClassLoadingInformation sensor-type.
	 */
	CLASS_INF_NA("This item is not available. You have to activate the 'ClassLoadingInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for ThreadInformation sensor-type.
	 */
	THREAD_INF_NA("This item is not available. You have to activate the 'ThreadInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for RuntimeInformation sensor-type.
	 */
	RUNTIME_INF_NA("This item is not available. You have to activate the 'RuntimeInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for MemoryInformation sensor-type.
	 */
	MEMORY_INF_NA("This item is not available. You have to activate the 'MemoryInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip for CompilationInformation sensor-type.
	 */
	COMPILATION_INF_NA("This item is not available. You have to activate the 'CompilationInformation' platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip when no sensor-type is available.
	 */
	SENSOR_NA("This item is not available. You have to activate at least one platform sensor type in 'inspectit-agent.cfg' for viewing this information."),

	/**
	 * Tooltip when no exception sensor is available.
	 */
	EXCEPTION_SENSOR_NA("This item is not available. You have to activate the exception sensor in 'inspectit-agent.cfg' for viewing this information.");

	/**
	 * The message string.
	 */
	private String message;

	/**
	 * 
	 * @param message
	 *            The error message.
	 */
	private SensorTypeAvailabilityEnum(String message) {
		this.message = message;
	}

	/**
	 * Returns the message string.
	 * 
	 * @return The message string.
	 */
	public String getMessage() {
		return message;
	}
}
