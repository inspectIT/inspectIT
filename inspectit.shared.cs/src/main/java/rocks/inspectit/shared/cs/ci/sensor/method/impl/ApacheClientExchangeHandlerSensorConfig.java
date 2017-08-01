package rocks.inspectit.shared.cs.ci.sensor.method.impl;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.cs.ci.sensor.method.AbstractMethodSensorConfig;

/**
 * The configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.async.http.ApacheClientExchangeHandlerSensor}
 * class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@XmlRootElement(name = "apache-client-exchange-handler-sensor")
public final class ApacheClientExchangeHandlerSensorConfig extends AbstractMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Apache Client Exchange Handler Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.async.http.ApacheClientExchangeHandlerSensor";

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
	public PriorityEnum getPriority() {
		return PriorityEnum.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return true;
	}
}
