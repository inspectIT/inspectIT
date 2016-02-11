package info.novatec.inspectit.ci.sensor.platform;

import info.novatec.inspectit.ci.sensor.ISensorConfig;

/**
 * Marker for platform sensors.
 * 
 * @author Ivan Senic
 * 
 */
public interface IPlatformSensorConfig extends ISensorConfig {

	/**
	 * Defines if sensor is active.
	 * 
	 * @return Returns <code>true</code> if sensor is active, false otherwise.
	 */
	boolean isActive();

}
