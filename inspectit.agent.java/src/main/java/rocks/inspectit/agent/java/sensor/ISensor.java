package rocks.inspectit.agent.java.sensor;

import rocks.inspectit.shared.all.instrumentation.config.impl.AbstractSensorTypeConfig;

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
