package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote Spring REST Template sensor config.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "remote-spring-resttemplate-client-sensor-config")
public class RemoteSpringRestTemplateClientSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote Spring RestTemplate Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.client.http.SpringRestTemplateClientSensor";

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
