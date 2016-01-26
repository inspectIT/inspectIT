package rocks.inspectit.agent.java.sensor.platform;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.sensor.ISensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;

/**
 * This interface is implemented by classes which provide information about the system, like CPU,
 * Memory etc.
 *
 * @author Patrice Bouillet
 *
 */
public interface IPlatformSensor extends ISensor {

	/**
	 * Defines if the sensor should be updated automatically. For static information, this method
	 * shall return <code>false</code> as its data will nearly never change. Thus a decrease in
	 * network traffic and processing usage can be accomplished.
	 *
	 * @return If this platform sensor should be updated automatically.
	 */
	boolean automaticUpdate();

	/**
	 * This method is called whenever the sensor should be updated.
	 *
	 * @param coreService
	 *            The core service which is needed to store the measurements to.
	 */
	void update(ICoreService coreService);

	/**
	 * Returns the {@link PlatformSensorTypeConfig} for this sensor.
	 *
	 * @return Returns the {@link PlatformSensorTypeConfig} for this sensor.
	 */
	PlatformSensorTypeConfig getSensorTypeConfig();

}
