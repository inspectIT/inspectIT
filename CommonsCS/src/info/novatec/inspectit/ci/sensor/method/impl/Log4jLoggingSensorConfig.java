/**
 *
 */
package info.novatec.inspectit.ci.sensor.method.impl;

import info.novatec.inspectit.ci.sensor.method.AbstractMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.ILoggingSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.MethodSensorPriorityEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "log4j-logging-sensor-config")
public class Log4jLoggingSensorConfig extends AbstractMethodSensorConfig implements IMethodSensorConfig, ILoggingSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Logging Sensor for log4j";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.method.logging.Log4JLoggingSensor";

	/**
	 * Name of the logging technology.
	 */
	private static final String TECHNOLOGY_NAME = "log4j";

	/**
	 * Available levels.
	 */
	private static final String[] LEVELS = new String[] { "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" };

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
	 * {@inheritDoc}
	 */
	@Override
	public String getTechnologyName() {
		return TECHNOLOGY_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getLogLevels() {
		return LEVELS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMinLevel() {
		return minLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMinLevel(String minLevel) throws IllegalArgumentException {
		if (!ArrayUtils.contains(LEVELS, minLevel)) {
			throw new IllegalArgumentException("Level to set must be one of the: " + Arrays.toString(LEVELS));
		}
		this.minLevel = minLevel;
	}

}
