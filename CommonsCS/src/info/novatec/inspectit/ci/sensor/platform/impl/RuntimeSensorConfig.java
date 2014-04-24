package info.novatec.inspectit.ci.sensor.platform.impl;

import info.novatec.inspectit.ci.sensor.platform.AbstractPlatformSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sensor configuration for the runtime information.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "runtime-sensor-config")
public class RuntimeSensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.platform.RuntimeInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
