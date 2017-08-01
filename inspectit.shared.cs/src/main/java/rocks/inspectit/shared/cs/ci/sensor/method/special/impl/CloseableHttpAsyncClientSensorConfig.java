package rocks.inspectit.shared.cs.ci.sensor.method.special.impl;

import rocks.inspectit.shared.all.instrumentation.config.impl.SubstitutionDescriptor;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * @author Isabel Vico Peinado
 *
 */
public final class CloseableHttpAsyncClientSensorConfig extends AbstractSpecialMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Closeable Http Async Client Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.special.CloseableHttpAsyncClientSensor";

	/**
	 * Singleton instance to use.
	 */
	public static final CloseableHttpAsyncClientSensorConfig INSTANCE = new CloseableHttpAsyncClientSensorConfig();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private CloseableHttpAsyncClientSensorConfig() {
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
		return new SubstitutionDescriptor(true, true);
	}
}
