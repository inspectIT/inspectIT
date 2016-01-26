package info.novatec.inspectit.agent.sensor.jmx;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.sensor.ISensor;

/**
 * This interface is implemented by classes which provide information about MBeans registered on a
 * MBeanServer.
 *
 * @author Alfred Krauss
 *
 */
public interface IJmxSensor extends ISensor {

	/**
	 * This method is called whenever the sensor should be updated.
	 *
	 * @param coreService
	 *            The core service which is needed to store the measurements to.
	 */
	void update(ICoreService coreService);
}
