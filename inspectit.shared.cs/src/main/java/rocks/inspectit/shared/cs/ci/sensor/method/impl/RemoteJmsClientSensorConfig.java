package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote MQ client sensor.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-jms-client-sensor-config")
public class RemoteJmsClientSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote JMS Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.client.mq.JmsRemoteClientSensor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerSide() {
		return false;
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
	public String getName() {
		return SENSOR_NAME;
	}

}
