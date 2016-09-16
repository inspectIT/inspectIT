package rocks.inspectit.shared.cs.ci.sensor.method.special.impl;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.all.instrumentation.config.impl.SubstitutionDescriptor;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * Configuration for the
 * {@link rocks.inspectit.agent.java.sensor.method.special.EUMInstrumentationSensor}.
 *
 * @author Jonas Kunz
 *
 */
@XmlTransient
public final class EUMInstrumentationSensorConfig extends AbstractSpecialMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "End User Monitoring Instrumentation Sensor";

	/**
	 * Implementing class name.
	 */
	public static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.method.special.EUMInstrumentationSensor";

	/**
	 * Singleton instance to use.
	 */
	public static final EUMInstrumentationSensorConfig INSTANCE = new EUMInstrumentationSensorConfig();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private EUMInstrumentationSensorConfig() {
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
