package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote MQ Inserter sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-mq-inserter-sensor-config")
public class RemoteMQInserterSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Remote MQ Inserter";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.inserter.mq.RemoteMQInserterSensor";

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
