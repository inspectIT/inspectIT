package rocks.inspectit.shared.cs.ci.sensor.method.special.impl;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * Configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.special.ClassLoadingDelegationSensor}.
 * Configuration can not be changed.
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
public final class ClassLoadingDelegationSensorConfig extends AbstractSpecialMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Class Loading Delegation Special Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.special.ClassLoadingDelegationSensor";

	/**
	 * Singleton instance to use.
	 */
	public static final ClassLoadingDelegationSensorConfig INSTANCE = new ClassLoadingDelegationSensorConfig();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private ClassLoadingDelegationSensorConfig() {
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
