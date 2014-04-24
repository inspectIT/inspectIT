package info.novatec.inspectit.ci.assignment;

import info.novatec.inspectit.ci.sensor.ISensorConfig;

/**
 * Base interface for all sensor assignments.
 * 
 * @author Ivan Senic
 * 
 * @param <T>Type of the sensor config that relates to the assignment.
 */
public interface ISensorAssignment<T extends ISensorConfig> {

	/**
	 * Returns the class of the sensor config.
	 * 
	 * @return Returns the class of the sensor config.
	 */
	Class<? extends T> getSensorConfigClass();
}
