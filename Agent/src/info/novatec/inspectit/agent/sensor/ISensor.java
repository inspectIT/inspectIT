package info.novatec.inspectit.agent.sensor;

import info.novatec.inspectit.instrumentation.config.impl.AbstractSensorTypeConfig;

import java.util.Map;

/**
 * This interface is used by all sensor which are collecting any kind of data. The
 * {@link #init(Map)} method is used to initialize the sensor with some additional preferences if
 * available.
 *
 * @author Patrice Bouillet
 *
 */
public interface ISensor {

	/**
	 * Returns the configuration for this sensor.
	 *
	 * @return Returns the configuration for this sensor.
	 */
	AbstractSensorTypeConfig getSensorTypeConfig();

}
