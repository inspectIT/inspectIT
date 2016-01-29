/**
 *
 */
package info.novatec.inspectit.ci.sensor.method.impl;

import info.novatec.inspectit.ci.sensor.method.AbstractMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.MethodSensorPriorityEnum;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "log4j-logging-sensor-config")
public class Log4jLoggingSensorConfig extends AbstractMethodSensorConfig implements IMethodSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Log4j Logging Sensor";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.method.logging.Log4JLoggingSensor";

	/**
	 * Min logging level to capture.
	 * <p>
	 * Default value is {@value #minLevel}.
	 */
	@XmlAttribute(name = "minLevel", required = true)
	private String minLevel = "WARN";

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodSensorPriorityEnum getPriority() {
		return MethodSensorPriorityEnum.MIN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sub-classes can override, by calling super and adding parameters to the returned map.
	 */
	@Override
	public Map<String, Object> getParameters() {
		return Collections.<String, Object> singletonMap("minLevel", minLevel);
	}

	/**
	 * Gets {@link #minLevel}.
	 *
	 * @return {@link #minLevel}
	 */
	public String getMinLevel() {
		return minLevel;
	}

	/**
	 * Sets {@link #minLevel}.
	 *
	 * @param minLevel
	 *            New value for {@link #minLevel}
	 */
	public void setMinLevel(String minLevel) {
		this.minLevel = minLevel;
	}

}
