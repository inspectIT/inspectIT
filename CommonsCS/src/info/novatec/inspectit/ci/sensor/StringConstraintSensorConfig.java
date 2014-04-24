package info.novatec.inspectit.ci.sensor;

import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class for all sensor configs that can define string length.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ ExceptionSensorConfig.class })
public abstract class StringConstraintSensorConfig implements ISensorConfig {

	/**
	 * String length of captured context.
	 * <p>
	 * Negative or zero values means unlimited.
	 */
	@XmlAttribute(name = "string-length", required = true)
	private int stringLength;

	/**
	 * No-arg constructor. Need for serialization.
	 */
	public StringConstraintSensorConfig() {
	}

	/**
	 * Only constructor.
	 * 
	 * @param stringLength
	 *            Default value of the string length of captured context.
	 */
	public StringConstraintSensorConfig(int stringLength) {
		this.stringLength = stringLength;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sub-classes can override, by calling super and adding parameters to the returned map.
	 */
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> params = new HashMap<>();
		if (stringLength > 0) {
			params.put("stringLength", String.valueOf(stringLength));
		}
		return params;
	}

	/**
	 * Gets {@link #stringLength}.
	 * 
	 * @return {@link #stringLength}
	 */
	public int getStringLength() {
		return stringLength;
	}

	/**
	 * Sets {@link #stringLength}.
	 * 
	 * @param stringLength
	 *            New value for {@link #stringLength}
	 */
	public void setStringLength(int stringLength) {
		this.stringLength = stringLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stringLength;
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
		StringConstraintSensorConfig other = (StringConstraintSensorConfig) obj;
		if (stringLength != other.stringLength) {
			return false;
		}
		return true;
	}

}
