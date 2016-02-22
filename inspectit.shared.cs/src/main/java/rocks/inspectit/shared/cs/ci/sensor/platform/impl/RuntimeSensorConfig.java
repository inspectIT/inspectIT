package rocks.inspectit.shared.cs.ci.sensor.platform.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.platform.AbstractPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;

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
	private static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.platform.RuntimeInformation";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
