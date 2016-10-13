package rocks.inspectit.shared.cs.ci.sensor.method.special.impl;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * Configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.special.MBeanServerInterceptorSensor}.
 * Configuration can not be changed.
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
public final class MBeanServerInterceptorSensorConfig extends AbstractSpecialMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "MBean Server Interceptor Special Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.special.MBeanServerInterceptorSensor";

	/**
	 * Singleton instance to use.
	 */
	public static final MBeanServerInterceptorSensorConfig INSTANCE = new MBeanServerInterceptorSensorConfig();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private MBeanServerInterceptorSensorConfig() {
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
	public String getClassName() {
		return CLASS_NAME;
	}

}
