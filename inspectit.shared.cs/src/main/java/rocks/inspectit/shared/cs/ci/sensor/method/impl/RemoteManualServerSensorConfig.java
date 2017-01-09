package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote manual serner sensor config.
 * 
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "remote-manual-server-sensor-config")
public class RemoteManualServerSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	public static final String SENSOR_NAME = "Remote Manual Server Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.server.manual.ManualRemoteServerSensor";

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
	public boolean isAdvanced() {
		return false;
	}

}
