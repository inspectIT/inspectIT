package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.MethodSensorPriorityEnum;

/**
 * JDBC connection sensor config.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "connection-sensor-config")
public class ConnectionSensorConfig extends AbstractMethodSensorConfig implements IMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "JDBC Connection Sensor";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.jdbc.ConnectionSensor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return SENSOR_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodSensorPriorityEnum getPriority() {
		return MethodSensorPriorityEnum.MIN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return true;
	}

}
