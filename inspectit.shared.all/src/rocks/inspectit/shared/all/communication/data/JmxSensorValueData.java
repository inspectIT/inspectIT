package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * This class is needed to store the values of a single attribute.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
@Entity
public class JmxSensorValueData extends SystemSensorData implements IAggregatedData<JmxSensorValueData> {

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
	@Column(length = 10000)
	private String value;

	/**
	 * The count of aggregated values, represented by this object.
	 */
	private int aggregationCount = 0;

	/**
	 * The minimum value of the aggregated objects.
	 */
	private double minValue = Double.MAX_VALUE;

	/**
	 * The maximum value of the aggregated objects.
	 */
	private double maxValue = Double.MIN_VALUE;

	/**
	 * The sum of all values of the aggregated objects.
	 */
	private double totalValue = 0;

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
	 * Copy constructor. Copies all values (except the aggregation values) of the given
	 * {@link JmxSensorValueData} object into the newly created.
	 * 
	 * @param origin
	 *            object to clone
	 */
	public JmxSensorValueData(JmxSensorValueData origin) {
		setId(origin.getId());
		setPlatformIdent(origin.getPlatformIdent());
		setTimeStamp(new Timestamp(origin.getTimeStamp().getTime()));
		setSensorTypeIdent(origin.getSensorTypeIdent());
		setJmxSensorDefinitionDataIdentId(origin.jmxSensorDefinitionDataIdentId);
		setValue(origin.value);
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

		if (aggregationCount <= 0 && isBooleanOrNumeric()) {
			double currentValue = getValueAsDouble();
			aggregationCount = 1;
			minValue = currentValue;
			maxValue = currentValue;
			totalValue = currentValue;
		}
	}

	/**
	 * Calculates and returns the average value of the aggregated objects.
	 * 
	 * @return the average value
	 */
	public double getAverageValue() {
		if (aggregationCount <= 0) {
			return getValueAsDouble();
		}
		return totalValue / aggregationCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public JmxSensorValueData getData() {
		return this;
	}

	/**
	 * Gets {@link #aggregationCount}.
	 * 
	 * @return {@link #aggregationCount}
	 */
	public int getAggregationCount() {
		return aggregationCount;
	}

	/**
	 * Gets {@link #minValue}.
	 * 
	 * @return {@link #minValue}
	 */
	public double getMinValue() {
		if (aggregationCount <= 0) {
			return getValueAsDouble();
		}
		return minValue;
	}

	/**
	 * Gets {@link #maxValue}.
	 * 
	 * @return {@link #maxValue}
	 */
	public double getMaxValue() {
		if (aggregationCount <= 0) {
			return getValueAsDouble();
		}
		return maxValue;
	}

	/**
	 * Gets {@link #totalValue}.
	 * 
	 * @return {@link #totalValue}
	 */
	public double getTotalValue() {
		return totalValue;
	}

	/**
	 * Returns the value as a {@link Double} value. The returned value will be <code>0</code> or
	 * <code>1</code> if the {@link #value} is a boolean value. If {@link #value} can not be
	 * converted into a number, a {@link NumberFormatException} is thrown.
	 * 
	 * @return {@link #value} as {@link Double}
	 */
	public double getValueAsDouble() {
		if (NumberUtils.isNumber(value)) {
			return NumberUtils.createDouble(value);
		}
		if (isBooleanValue(value)) {
			return BooleanUtils.toBoolean(value) ? 1 : 0;
		}
		throw new NumberFormatException();
	}

	/**
	 * Checks if the value of this object is a boolean or numeric value.
	 * 
	 * @return <code>true</code> if the value is a boolean or number, otherwise <code>false</code>
	 */
	public boolean isBooleanOrNumeric() {
		return isBooleanValue(value) || NumberUtils.isNumber(value);
	}

	/**
	 * Determines whether the given string contains a boolean value (false, true, yes, no).
	 * 
	 * @param value
	 *            the value to examine
	 * @return whether the given value is a boolean value
	 */
	private boolean isBooleanValue(String value) {
		value = value.toLowerCase();
		return "false".equals(value) || "true".equals(value) || "yes".equals(value) || "no".equals(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(JmxSensorValueData data) {
		if (!data.isBooleanOrNumeric()) {
			throw new RuntimeException("The given JMX data can not be aggregated.");
		}
		aggregationCount++;

		double valueToAggregate = data.getValueAsDouble();

		minValue = Math.min(minValue, valueToAggregate);
		maxValue = Math.max(maxValue, valueToAggregate);
		totalValue += valueToAggregate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "JmxSensorValueData [jmxSensorDefinitionDataIdent=" + jmxSensorDefinitionDataIdentId + ", value=" + value + ", getId()=" + getId() + ", getPlatformIdent()=" + getPlatformIdent()
				+ ", getSensorTypeIdent()=" + getSensorTypeIdent() + ", getTimeStamp()=" + getTimeStamp() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + aggregationCount;
		result = prime * result + (int) (jmxSensorDefinitionDataIdentId ^ (jmxSensorDefinitionDataIdentId >>> 32));
		long temp;
		temp = Double.doubleToLongBits(maxValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(totalValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		JmxSensorValueData other = (JmxSensorValueData) obj;
		if (aggregationCount != other.aggregationCount) {
			return false;
		}
		if (jmxSensorDefinitionDataIdentId != other.jmxSensorDefinitionDataIdentId) {
			return false;
		}
		if (Double.doubleToLongBits(maxValue) != Double.doubleToLongBits(other.maxValue)) {
			return false;
		}
		if (Double.doubleToLongBits(minValue) != Double.doubleToLongBits(other.minValue)) {
			return false;
		}
		if (Double.doubleToLongBits(totalValue) != Double.doubleToLongBits(other.totalValue)) {
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

}
