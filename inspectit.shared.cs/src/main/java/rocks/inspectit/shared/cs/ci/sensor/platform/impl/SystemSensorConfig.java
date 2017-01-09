package rocks.inspectit.shared.cs.ci.sensor.platform.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.platform.AbstractPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;

/**
 * Sensor configuration for the system information.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "system-sensor-config")
public class SystemSensorConfig extends AbstractPlatformSensorConfig implements IPlatformSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "System Information";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.platform.SystemInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
