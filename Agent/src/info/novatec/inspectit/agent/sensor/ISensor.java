package info.novatec.inspectit.agent.sensor;

import info.novatec.inspectit.instrumentation.config.impl.AbstractSensorTypeConfig;

/**
 * This interface is used by all sensor which are collecting any kind of data.
 *
 * @author Patrice Bouillet
 * @author Ivan Senic
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
