package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote Jetty HttpClient client sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-jetty-httpclientV61-client-sensor-config")
public class RemoteJettyHttpClientV61ClientSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote Jetty HTTP Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.client.http.JettyHttpClientV61Sensor";

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
