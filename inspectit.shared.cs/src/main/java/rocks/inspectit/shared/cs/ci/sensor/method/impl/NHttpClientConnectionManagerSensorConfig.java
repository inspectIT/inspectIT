package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * The configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.async.http.NHttpClientConnectionManagerSensor}
 * class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@XmlRootElement(name = "nhttp-client-connection-manager-sensor-config")
public final class NHttpClientConnectionManagerSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "NHttp Client Connection Manager Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.async.http.NHttpClientConnectionManagerSensor";

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
	public boolean isServerSide() {
		return false;
	}
}
