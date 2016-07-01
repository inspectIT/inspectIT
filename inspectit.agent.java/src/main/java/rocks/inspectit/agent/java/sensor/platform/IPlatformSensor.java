package rocks.inspectit.agent.java.sensor.platform;

import rocks.inspectit.agent.java.sensor.ISensor;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;

/**
 * This interface is implemented by classes which provide information about the system, like CPU,
 * Memory etc.
 *
 * @author Patrice Bouillet
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public interface IPlatformSensor extends ISensor {

	/**
	 * Returns the {@link PlatformSensorTypeConfig} for this sensor.
	 *
	 * @return Returns the {@link PlatformSensorTypeConfig} for this sensor.
	 */
	PlatformSensorTypeConfig getSensorTypeConfig();

	/** Reset any saved state in the sensor. Used to reset the sensor data collector class. */
	void reset();

	/** This method is called whenever the sensor should be updated. */
	void gather();

	/**
	 * Get the corresponding collector class of type {@link PlatformSensorData}.
	 *
	 * @return the collector class.
	 */
	PlatformSensorData get();

}
