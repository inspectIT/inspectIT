package info.novatec.inspectit.ci.sensor.platform.impl;

import info.novatec.inspectit.ci.sensor.platform.AbstractPlatformSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sensor configuration for the memory information.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "memory-sensor-config")
public class MemorySensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.platform.MemoryInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
