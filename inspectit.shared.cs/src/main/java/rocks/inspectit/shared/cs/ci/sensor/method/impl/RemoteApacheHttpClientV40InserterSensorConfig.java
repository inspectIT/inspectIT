package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote Apache HttpClient Inserter sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-apache-httpclientV40-inserter-sensor-config")
public class RemoteApacheHttpClientV40InserterSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Remote Apache HTTP Inserter";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache.RemoteApacheHttpClientV40InserterSensor";

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
