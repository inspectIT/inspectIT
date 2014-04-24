package info.novatec.inspectit.ci.sensor.method.impl;

import info.novatec.inspectit.ci.sensor.method.AbstractMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.MethodSensorPriorityEnum;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JDBC prepared statement parameter config.
 * 
 * @author Ivan Senic
 * 
 */
@XmlRootElement(name = "prepared-statement-parameter-sensor-config")
public class PreparedStatementParameterSensorConfig extends AbstractMethodSensorConfig implements IMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "JDBC Prepared Statement Parameter Sensor";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementParameterSensor";

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
