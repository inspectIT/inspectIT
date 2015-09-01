package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

/**
 * This class is needed to store the values of a single attribute.
 * 
 * @author Alfred Krauss
 * 
 */
public class JmxSensorValueData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 1064800467325690317L;

	/**
	 * The ID of the DefinitionData.
	 */
	private long jmxSensorDefinitionDataIdentId;

	/**
	 * Values of the attribute to a given time.
	 */
	private String value;

	/**
	 * Empty constructor needed for Hibernate.
	 */
	public JmxSensorValueData() {
	}

	/**
	 * Constructor.
	 * 
	 * @param jmxDefinitionDataIdentId
	 *            the id of the related {@link JmxDefinitionDataIdent} of this
	 *            {@link JmxSensorValueData}
	 * @param value
	 *            the value
	 * @param timestamp
	 *            the timestamp when this value was captured
	 * @param platformIdent
	 *            the id of the related {@link PlatformIdent}
	 * @param sensorTypeIdent
	 *            the id of the related {@link SensorTypeIdent}
	 */
	public JmxSensorValueData(long jmxDefinitionDataIdentId, String value, Timestamp timestamp, long platformIdent, long sensorTypeIdent) {
		setJmxSensorDefinitionDataIdentId(jmxDefinitionDataIdentId);
		setValue(value);
		super.setTimeStamp(timestamp);
		super.setPlatformIdent(platformIdent);
		super.setSensorTypeIdent(sensorTypeIdent);
		super.setId(jmxDefinitionDataIdentId);
	}

	/**
	 * @return The jmxSensorDefinitionDataIdentifier ID
	 */
	public long getJmxSensorDefinitionDataIdentId() {
		return jmxSensorDefinitionDataIdentId;
	}

	/**
	 * @param jmxSensorDefinitionDataIdentId
	 *            The jmxSensorDefinitionDataIdent ID to set.
	 */
	public void setJmxSensorDefinitionDataIdentId(long jmxSensorDefinitionDataIdentId) {
		this.jmxSensorDefinitionDataIdentId = jmxSensorDefinitionDataIdentId;
	}

	/**
	 * Gets the {@link #value}.
	 * 
	 * @return {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the {@link #value}.
	 * 
	 * @param value
	 *            New value for {@link #value}.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (jmxSensorDefinitionDataIdentId ^ (jmxSensorDefinitionDataIdentId >>> 32));
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

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
		JmxSensorValueData other = (JmxSensorValueData) obj;
		if (jmxSensorDefinitionDataIdentId != other.jmxSensorDefinitionDataIdentId) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "JmxSensorValueData [jmxSensorDefinitionDataIdent=" + jmxSensorDefinitionDataIdentId + ", value=" + value + ", getId()=" + getId() + ", getPlatformIdent()=" + getPlatformIdent()
				+ ", getSensorTypeIdent()=" + getSensorTypeIdent() + ", getTimeStamp()=" + getTimeStamp() + "]";
	}

}
