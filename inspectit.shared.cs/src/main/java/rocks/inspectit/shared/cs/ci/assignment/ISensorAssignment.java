package rocks.inspectit.shared.cs.ci.assignment;

import java.util.Map;

import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;

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

	/**
	 * Return settings for the sensor assignment.
	 *
	 * @return Return settings for the sensor assignment.
	 */
	Map<String, Object> getSettings();

}
