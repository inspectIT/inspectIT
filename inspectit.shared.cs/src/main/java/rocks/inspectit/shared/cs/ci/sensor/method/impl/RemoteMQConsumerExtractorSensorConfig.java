package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.method.AbstractRemoteSensorConfig;

/**
 * Remote MQ Consumer Extractor sensor config.
 *
 * @author Thomas Kluge
 *
 */
@XmlRootElement(name = "remote-mq-consumer-extractor-sensor-config")
public class RemoteMQConsumerExtractorSensorConfig extends AbstractRemoteSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Remote MQ Consumer Extractor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.remote.extractor.mq.RemoteMQConsumerExtractorSensor";

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
