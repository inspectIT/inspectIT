package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * This class provide dynamic informations about the underlying operating system such as cpu usage
 * and cpu time.
 * <p>
 * This class implements the {@link IAggregatedData} interface but does not provide the IDs of the
 * aggregated instances since they are not related to any data and are useless.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class CpuInformationData extends SystemSensorData implements IAggregatedData<CpuInformationData> {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 3575761562499283807L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The cpu time used by the process on which the virtual machine is running.
	 */
	private long processCpuTime = 0;

	/**
	 * The minimum cpu usage in percent.
	 */
	private float minCpuUsage = Float.MAX_VALUE;

	/**
	 * The maximum cpu usage in percent.
	 */
	private float maxCpuUsage = 0;

	/**
	 * The total cpu usage in percent.
	 */
	private float totalCpuUsage = 0;

	/**
	 * Default no-args constructor.
	 */
	public CpuInformationData() {
	}

	/**
	 * The constructor which needs three parameters.
	 * 
	 * @param timeStamp
	 *            The Timestamp.
	 * @param platformIdent
	 *            The PlatformIdent.
	 * @param sensorTypeIdent
	 *            The SensorTypeIdent.
	 */
	public CpuInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent);
	}

	/**
	 * Gets {@link #count}.
	 * 
	 * @return {@link #count}
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets {@link #count}.
	 * 
	 * @param count
	 *            New value for {@link #count}
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * increases the count by 1.
	 */
	public void incrementCount() {
		this.count++;
	}

	/**
	 * sets the process cpu time if it is higher than the current cpu time.
	 * 
	 * @param actualProcessCpuTime
	 *            the data to set.
	 */
	public void updateProcessCpuTime(long actualProcessCpuTime) {
		if (actualProcessCpuTime > processCpuTime) {
			processCpuTime = actualProcessCpuTime;
		}
	}

	/**
	 * Gets {@link #processCpuTime}.
	 * 
	 * @return {@link #processCpuTime}
	 */
	public long getProcessCpuTime() {
		return processCpuTime;
	}

	/**
	 * Sets {@link #processCpuTime}.
	 * 
	 * @param processCpuTime
	 *            New value for {@link #processCpuTime}
	 */
	public void setProcessCpuTime(long processCpuTime) {
		this.processCpuTime = processCpuTime;
	}

	/**
	 * adds the given value to the cpu usage.
	 * 
	 * @param cpuUsage
	 *            the value to add.
	 */
	public void addCpuUsage(float cpuUsage) {
		this.totalCpuUsage += cpuUsage;
	}

	/**
	 * Gets {@link #minCpuUsage}.
	 * 
	 * @return {@link #minCpuUsage}
	 */
	public float getMinCpuUsage() {
		return minCpuUsage;
	}

	/**
	 * Sets {@link #minCpuUsage}.
	 * 
	 * @param minCpuUsage
	 *            New value for {@link #minCpuUsage}
	 */
	public void setMinCpuUsage(float minCpuUsage) {
		this.minCpuUsage = minCpuUsage;
	}

	/**
	 * Gets {@link #maxCpuUsage}.
	 * 
	 * @return {@link #maxCpuUsage}
	 */
	public float getMaxCpuUsage() {
		return maxCpuUsage;
	}

	/**
	 * Sets {@link #maxCpuUsage}.
	 * 
	 * @param maxCpuUsage
	 *            New value for {@link #maxCpuUsage}
	 */
	public void setMaxCpuUsage(float maxCpuUsage) {
		this.maxCpuUsage = maxCpuUsage;
	}

	/**
	 * Gets {@link #totalCpuUsage}.
	 * 
	 * @return {@link #totalCpuUsage}
	 */
	public float getTotalCpuUsage() {
		return totalCpuUsage;
	}

	/**
	 * Sets {@link #totalCpuUsage}.
	 * 
	 * @param totalCpuUsage
	 *            New value for {@link #totalCpuUsage}
	 */
	public void setTotalCpuUsage(float totalCpuUsage) {
		this.totalCpuUsage = totalCpuUsage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(CpuInformationData other) {
		count += other.count;
		processCpuTime += other.processCpuTime;
		minCpuUsage = Math.min(minCpuUsage, other.minCpuUsage);
		maxCpuUsage = Math.max(maxCpuUsage, other.maxCpuUsage);
		totalCpuUsage += other.totalCpuUsage;
	}

	/**
	 * {@inheritDoc}
	 */
	public CpuInformationData getData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + Float.floatToIntBits(maxCpuUsage);
		result = prime * result + Float.floatToIntBits(minCpuUsage);
		result = prime * result + (int) (processCpuTime ^ (processCpuTime >>> 32));
		result = prime * result + Float.floatToIntBits(totalCpuUsage);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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
		CpuInformationData other = (CpuInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (Float.floatToIntBits(maxCpuUsage) != Float.floatToIntBits(other.maxCpuUsage)) {
			return false;
		}
		if (Float.floatToIntBits(minCpuUsage) != Float.floatToIntBits(other.minCpuUsage)) {
			return false;
		}
		if (processCpuTime != other.processCpuTime) {
			return false;
		}
		if (Float.floatToIntBits(totalCpuUsage) != Float.floatToIntBits(other.totalCpuUsage)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 3, 1, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
