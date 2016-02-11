package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * This class provide dynamic informations about the compilation system of the virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class CompilationInformationData extends SystemSensorData {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -7529958619378902534L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum approximate accumulated elapsed time (milliseconds) spent in compilation.
	 */
	private long minTotalCompilationTime = Long.MAX_VALUE;

	/**
	 * The maximum approximate accumulated elapsed time (milliseconds) spent in compilation.
	 */
	private long maxTotalCompilationTime = 0;

	/**
	 * The total approximate accumulated elapsed time (milliseconds) spent in compilation.
	 */
	private long totalTotalCompilationTime = 0;

	/**
	 * Default no-args constructor.
	 */
	public CompilationInformationData() {
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
	public CompilationInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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
	 * adds the value to the total compilation time.
	 * 
	 * @param totalCompilationTime
	 *            the value to add.
	 */
	public void addTotalCompilationTime(long totalCompilationTime) {
		this.totalTotalCompilationTime += totalCompilationTime;
	}

	/**
	 * Gets {@link #minTotalCompilationTime}.
	 * 
	 * @return {@link #minTotalCompilationTime}
	 */
	public long getMinTotalCompilationTime() {
		return minTotalCompilationTime;
	}

	/**
	 * Sets {@link #minTotalCompilationTime}.
	 * 
	 * @param minTotalCompilationTime
	 *            New value for {@link #minTotalCompilationTime}
	 */
	public void setMinTotalCompilationTime(long minTotalCompilationTime) {
		this.minTotalCompilationTime = minTotalCompilationTime;
	}

	/**
	 * Gets {@link #maxTotalCompilationTime}.
	 * 
	 * @return {@link #maxTotalCompilationTime}
	 */
	public long getMaxTotalCompilationTime() {
		return maxTotalCompilationTime;
	}

	/**
	 * Sets {@link #maxTotalCompilationTime}.
	 * 
	 * @param maxTotalCompilationTime
	 *            New value for {@link #maxTotalCompilationTime}
	 */
	public void setMaxTotalCompilationTime(long maxTotalCompilationTime) {
		this.maxTotalCompilationTime = maxTotalCompilationTime;
	}

	/**
	 * Gets {@link #totalTotalCompilationTime}.
	 * 
	 * @return {@link #totalTotalCompilationTime}
	 */
	public long getTotalTotalCompilationTime() {
		return totalTotalCompilationTime;
	}

	/**
	 * Sets {@link #totalTotalCompilationTime}.
	 * 
	 * @param totalTotalCompilationTime
	 *            New value for {@link #totalTotalCompilationTime}
	 */
	public void setTotalTotalCompilationTime(long totalTotalCompilationTime) {
		this.totalTotalCompilationTime = totalTotalCompilationTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + (int) (maxTotalCompilationTime ^ (maxTotalCompilationTime >>> 32));
		result = prime * result + (int) (minTotalCompilationTime ^ (minTotalCompilationTime >>> 32));
		result = prime * result + (int) (totalTotalCompilationTime ^ (totalTotalCompilationTime >>> 32));
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
		CompilationInformationData other = (CompilationInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxTotalCompilationTime != other.maxTotalCompilationTime) {
			return false;
		}
		if (minTotalCompilationTime != other.minTotalCompilationTime) {
			return false;
		}
		if (totalTotalCompilationTime != other.totalTotalCompilationTime) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 3, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
