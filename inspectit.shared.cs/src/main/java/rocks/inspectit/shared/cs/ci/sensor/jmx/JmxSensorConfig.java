package rocks.inspectit.shared.cs.ci.sensor.jmx;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;

/**
 * Configuration for the JMX sensor.
 *
 * @author Ivan Senic
 *
 */
public class JmxSensorConfig implements ISensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.jmx.JmxSensor";

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
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

}
