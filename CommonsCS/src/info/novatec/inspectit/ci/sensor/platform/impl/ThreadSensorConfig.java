package info.novatec.inspectit.ci.sensor.platform.impl;

import info.novatec.inspectit.ci.sensor.platform.AbstractPlatformSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sensor configuration for the threads information.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "thread-sensor-config")
public class ThreadSensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.platform.ThreadInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
