package info.novatec.inspectit.ci.sensor.method.impl;

import info.novatec.inspectit.ci.sensor.StringConstraintSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.MethodSensorPriorityEnum;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Prepared statement sensor configuration.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "prepared-statement-sensor-config")
public class PreparedStatementSensorConfig extends StringConstraintSensorConfig implements IMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "JDBC Prepared Statement Sensor";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementSensor";


	/**
	 * No-args constructor.
	 */
	public PreparedStatementSensorConfig() {
		super(1000);
	}

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
