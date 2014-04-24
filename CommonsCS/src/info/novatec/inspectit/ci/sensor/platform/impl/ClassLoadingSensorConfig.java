package info.novatec.inspectit.ci.sensor.platform.impl;

import info.novatec.inspectit.ci.sensor.platform.AbstractPlatformSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Sensor configuration for the class loading information.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "class-loading-sensor-config")
public class ClassLoadingSensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
