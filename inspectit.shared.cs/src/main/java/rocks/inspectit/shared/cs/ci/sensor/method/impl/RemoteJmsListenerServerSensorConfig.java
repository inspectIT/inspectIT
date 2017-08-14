package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote MQ Listener server sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-jms-listener-server-sensor-config")
public class RemoteJmsListenerServerSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote JMS Listener Server Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.server.mq.JmsListenerRemoteServerSensor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerSide() {
		return true;
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
