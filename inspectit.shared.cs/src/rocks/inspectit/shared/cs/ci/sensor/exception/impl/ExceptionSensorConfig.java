package info.novatec.inspectit.ci.sensor.exception.impl;

import info.novatec.inspectit.ci.sensor.StringConstraintSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.MethodSensorPriorityEnum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of the exception sensor.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exception-sensor-config")
public class ExceptionSensorConfig extends StringConstraintSensorConfig implements IExceptionSensorConfig {

	/**
	 * Sensor name.
	 */
	private static final String SENSOR_NAME = "Exception Sensor";

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";

	/**
	 * If sensor mode is enhanced.
	 * <p>
	 * Default value is {@value #enhanced}.
	 */
	@XmlAttribute(name = "enhanced", required = true)
	private boolean enhanced = false;

	/**
	 * No-args constructor.
	 */
	public ExceptionSensorConfig() {
		super(500);
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
	public boolean isEnhanced() {
		return enhanced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodSensorPriorityEnum getPriority() {
		return MethodSensorPriorityEnum.NORMAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdvanced() {
		return false;
	}

	/**
	 * Sets {@link #enhanced}.
	 * 
	 * @param enhanced
	 *            New value for {@link #enhanced}
	 */
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (enhanced ? 1231 : 1237);
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExceptionSensorConfig other = (ExceptionSensorConfig) obj;
		if (enhanced != other.enhanced) {
			return false;
		}
		return true;
	}

}
