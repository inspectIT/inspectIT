package info.novatec.inspectit.ci.sensor.method;

import info.novatec.inspectit.ci.sensor.ISensorConfig;

/**
 * Interface for the method sensor configurations.
 * 
 * @author Ivan Senic
 * 
 */
public interface IMethodSensorConfig extends ISensorConfig {

	/**
	 * Returns sensor name.
	 * 
	 * @return Returns sensor name.
	 */
	String getName();

	/**
	 * Returns priority of the sensor.
	 * 
	 * @return Returns priority of the sensor.
	 */
	MethodSensorPriorityEnum getPriority();

	/**
	 * Defines if sensor is "advanced", meaning that users mostly won't use this sensor in
	 * assignment.
	 * 
	 * @return Returns <code>true</code> if sensor is "advanced", false otherwise.
	 */
	boolean isAdvanced();

}
