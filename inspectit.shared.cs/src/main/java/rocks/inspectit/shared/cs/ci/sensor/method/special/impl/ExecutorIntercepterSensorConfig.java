package rocks.inspectit.shared.cs.ci.sensor.method.special.impl;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.all.instrumentation.config.impl.SubstitutionDescriptor;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * Configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.special.ExecutorInterceptorSensor}.
 *
 * @author Marius Oehler
 *
 */
@XmlTransient
public final class ExecutorIntercepterSensorConfig extends AbstractSpecialMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Executor Interceptor Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.special.ExecutorIntercepterSensor";

	/**
	 * Singleton instance to use.
	 */
	public static final ExecutorIntercepterSensorConfig INSTANCE = new ExecutorIntercepterSensorConfig();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private ExecutorIntercepterSensorConfig() {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubstitutionDescriptor getSubstitutionDescriptor() {
		return new SubstitutionDescriptor(false, true);
	}
}
