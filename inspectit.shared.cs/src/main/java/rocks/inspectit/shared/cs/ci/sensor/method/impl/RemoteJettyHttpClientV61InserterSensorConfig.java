package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote Jetty HttpClient Inserter sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-jetty-httpclientV61-inserter-sensor-config")
public class RemoteJettyHttpClientV61InserterSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Remote Jetty HTTP Inserter";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty.RemoteJettyHttpClientV61InserterSensor";

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
