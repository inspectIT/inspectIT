package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.sensor.ISensor;

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
	 * @param sensorTypeIdent
	 *            The ID of the sensor type so that old data can be found. (for aggregating etc.)
	 */
	void update(ICoreService coreService, long sensorTypeIdent);

}
