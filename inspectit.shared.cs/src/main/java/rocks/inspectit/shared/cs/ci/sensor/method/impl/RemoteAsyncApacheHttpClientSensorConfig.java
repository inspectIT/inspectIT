package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote Async Apache HttpClient sensor config.
 *
 * @author Isabel Vico Peinado
 *
 */
@XmlRootElement(name = "remote-async-apache-httpclient-sensor-config")
public class RemoteAsyncApacheHttpClientSensorConfig extends AbstractRemoteSensorConfig {
	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote Async Apache HTTP Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.client.http.ApacheAsyncHttpClientSensor";

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isServerSide() {
		return false;
	}
}
