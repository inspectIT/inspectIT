package rocks.inspectit.shared.cs.ci.sensor.platform.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.platform.AbstractPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;

/**
 * Sensor configuration for the class loading information.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "class-loading-sensor-config")
public class ClassLoadingSensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Class Loading Information";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.platform.ClassLoadingInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
