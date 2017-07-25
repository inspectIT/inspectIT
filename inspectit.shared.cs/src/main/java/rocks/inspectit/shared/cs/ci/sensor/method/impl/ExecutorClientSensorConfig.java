package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Configuration for the executor client sensor.
 *
 * @author Marius Oehler
 *
 */
@XmlRootElement(name = "executor-client-sensor-config")
public class ExecutorClientSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Executor Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.async.executor.ExecutorClientSensor";

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
	public String getName() {
		return SENSOR_NAME;
	}
}
