package rocks.inspectit.shared.cs.ci.sensor.jmx;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;

/**
 * Configuration for the JMX sensor.
 *
 * @author Ivan Senic
 *
 */
@XmlRootElement(name = "jmx-loading-sensor-config")
public class JmxSensorConfig implements ISensorConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "rocks.inspectit.agent.java.sensor.jmx.JmxSensor";

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.getClass().hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

}
