package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * This class provide dynamic informations about the class loading system of the virtual machine.
 * <p>
 * This class implements the {@link IAggregatedData} interface but does not provide the IDs of the
 * aggregated instances since they are not related to any data and are useless.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class ClassLoadingInformationData extends SystemSensorData implements IAggregatedData<ClassLoadingInformationData> {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = 3670151927025437963L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum number of loaded classes in the virtual machine.
	 */
	private int minLoadedClassCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of loaded classes in the virtual machine.
	 */
	private int maxLoadedClassCount = 0;

	/**
	 * The total number of loaded classes in the virtual machine.
	 */
	private int totalLoadedClassCount = 0;

	/**
	 * The minimum number of total loaded classes since the virtual machine started.
	 */
	private long minTotalLoadedClassCount = Long.MAX_VALUE;

	/**
	 * The maximum number of total loaded classes since the virtual machine started.
	 */
	private long maxTotalLoadedClassCount = 0;

	/**
	 * The total number of total loaded classes since the virtual machine started.
	 */
	private long totalTotalLoadedClassCount = 0;

	/**
	 * The minimum number of unloaded classes since the virtual machine started.
	 */
	private long minUnloadedClassCount = Long.MAX_VALUE;

	/**
	 * The maximum number of unloaded classes since the virtual machine started.
	 */
	private long maxUnloadedClassCount = 0;

	/**
	 * The total number of unloaded classes since the virtual machine started.
	 */
	private long totalUnloadedClassCount = 0;

	/**
	 * Default no-args constructor.
	 */
	public ClassLoadingInformationData() {
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
	public ClassLoadingInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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
	 * adds the given value to the total number of loaded classes.
	 * 
	 * @param loadedClassCount
	 *            the value to add.
	 */
	public void addLoadedClassCount(int loadedClassCount) {
		this.totalLoadedClassCount += loadedClassCount;
	}

	/**
	 * Gets {@link #minLoadedClassCount}.
	 * 
	 * @return {@link #minLoadedClassCount}
	 */
	public int getMinLoadedClassCount() {
		return minLoadedClassCount;
	}

	/**
	 * Sets {@link #minLoadedClassCount}.
	 * 
	 * @param minLoadedClassCount
	 *            New value for {@link #minLoadedClassCount}
	 */
	public void setMinLoadedClassCount(int minLoadedClassCount) {
		this.minLoadedClassCount = minLoadedClassCount;
	}

	/**
	 * Gets {@link #maxLoadedClassCount}.
	 * 
	 * @return {@link #maxLoadedClassCount}
	 */
	public int getMaxLoadedClassCount() {
		return maxLoadedClassCount;
	}

	/**
	 * Sets {@link #maxLoadedClassCount}.
	 * 
	 * @param maxLoadedClassCount
	 *            New value for {@link #maxLoadedClassCount}
	 */
	public void setMaxLoadedClassCount(int maxLoadedClassCount) {
		this.maxLoadedClassCount = maxLoadedClassCount;
	}

	/**
	 * Gets {@link #totalLoadedClassCount}.
	 * 
	 * @return {@link #totalLoadedClassCount}
	 */
	public int getTotalLoadedClassCount() {
		return totalLoadedClassCount;
	}

	/**
	 * Sets {@link #totalLoadedClassCount}.
	 * 
	 * @param totalLoadedClassCount
	 *            New value for {@link #totalLoadedClassCount}
	 */
	public void setTotalLoadedClassCount(int totalLoadedClassCount) {
		this.totalLoadedClassCount = totalLoadedClassCount;
	}

	/**
	 * adds the given value to the total number of loaded classes.
	 * 
	 * @param totalLoadedClassCount
	 *            the value to add.
	 */
	public void addTotalLoadedClassCount(long totalLoadedClassCount) {
		this.totalTotalLoadedClassCount += totalLoadedClassCount;
	}

	/**
	 * Gets {@link #minTotalLoadedClassCount}.
	 * 
	 * @return {@link #minTotalLoadedClassCount}
	 */
	public long getMinTotalLoadedClassCount() {
		return minTotalLoadedClassCount;
	}

	/**
	 * Sets {@link #minTotalLoadedClassCount}.
	 * 
	 * @param minTotalLoadedClassCount
	 *            New value for {@link #minTotalLoadedClassCount}
	 */
	public void setMinTotalLoadedClassCount(long minTotalLoadedClassCount) {
		this.minTotalLoadedClassCount = minTotalLoadedClassCount;
	}

	/**
	 * Gets {@link #maxTotalLoadedClassCount}.
	 * 
	 * @return {@link #maxTotalLoadedClassCount}
	 */
	public long getMaxTotalLoadedClassCount() {
		return maxTotalLoadedClassCount;
	}

	/**
	 * Sets {@link #maxTotalLoadedClassCount}.
	 * 
	 * @param maxTotalLoadedClassCount
	 *            New value for {@link #maxTotalLoadedClassCount}
	 */
	public void setMaxTotalLoadedClassCount(long maxTotalLoadedClassCount) {
		this.maxTotalLoadedClassCount = maxTotalLoadedClassCount;
	}

	/**
	 * Gets {@link #totalTotalLoadedClassCount}.
	 * 
	 * @return {@link #totalTotalLoadedClassCount}
	 */
	public long getTotalTotalLoadedClassCount() {
		return totalTotalLoadedClassCount;
	}

	/**
	 * Sets {@link #totalTotalLoadedClassCount}.
	 * 
	 * @param totalTotalLoadedClassCount
	 *            New value for {@link #totalTotalLoadedClassCount}
	 */
	public void setTotalTotalLoadedClassCount(long totalTotalLoadedClassCount) {
		this.totalTotalLoadedClassCount = totalTotalLoadedClassCount;
	}

	/**
	 * adds the given value to the number of unloaded classes.
	 * 
	 * @param unloadedClassCount
	 *            the value to add.
	 */
	public void addUnloadedClassCount(long unloadedClassCount) {
		this.totalUnloadedClassCount += unloadedClassCount;
	}

	/**
	 * Gets {@link #minUnloadedClassCount}.
	 * 
	 * @return {@link #minUnloadedClassCount}
	 */
	public long getMinUnloadedClassCount() {
		return minUnloadedClassCount;
	}

	/**
	 * Sets {@link #minUnloadedClassCount}.
	 * 
	 * @param minUnloadedClassCount
	 *            New value for {@link #minUnloadedClassCount}
	 */
	public void setMinUnloadedClassCount(long minUnloadedClassCount) {
		this.minUnloadedClassCount = minUnloadedClassCount;
	}

	/**
	 * Gets {@link #maxUnloadedClassCount}.
	 * 
	 * @return {@link #maxUnloadedClassCount}
	 */
	public long getMaxUnloadedClassCount() {
		return maxUnloadedClassCount;
	}

	/**
	 * Sets {@link #maxUnloadedClassCount}.
	 * 
	 * @param maxUnloadedClassCount
	 *            New value for {@link #maxUnloadedClassCount}
	 */
	public void setMaxUnloadedClassCount(long maxUnloadedClassCount) {
		this.maxUnloadedClassCount = maxUnloadedClassCount;
	}

	/**
	 * Gets {@link #totalUnloadedClassCount}.
	 * 
	 * @return {@link #totalUnloadedClassCount}
	 */
	public long getTotalUnloadedClassCount() {
		return totalUnloadedClassCount;
	}

	/**
	 * Sets {@link #totalUnloadedClassCount}.
	 * 
	 * @param totalUnloadedClassCount
	 *            New value for {@link #totalUnloadedClassCount}
	 */
	public void setTotalUnloadedClassCount(long totalUnloadedClassCount) {
		this.totalUnloadedClassCount = totalUnloadedClassCount;
	}

	/**
	 * Aggregates other class loading object info this object.
	 * 
	 * @param other
	 *            Object to aggregate data from.
	 */
	public void aggregate(ClassLoadingInformationData other) {
		count += other.count;

		minLoadedClassCount = Math.min(minLoadedClassCount, other.minLoadedClassCount);
		maxLoadedClassCount = Math.max(maxLoadedClassCount, other.maxLoadedClassCount);
		totalLoadedClassCount += other.totalLoadedClassCount;

		minTotalLoadedClassCount = Math.min(minTotalLoadedClassCount, other.minTotalLoadedClassCount);
		maxTotalLoadedClassCount = Math.max(maxTotalLoadedClassCount, other.maxTotalLoadedClassCount);
		totalTotalLoadedClassCount += other.totalTotalLoadedClassCount;

		minUnloadedClassCount = Math.min(minUnloadedClassCount, other.minUnloadedClassCount);
		maxUnloadedClassCount = Math.max(maxUnloadedClassCount, other.maxUnloadedClassCount);
		totalUnloadedClassCount += other.maxUnloadedClassCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassLoadingInformationData getData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + maxLoadedClassCount;
		result = prime * result + (int) (maxTotalLoadedClassCount ^ (maxTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (maxUnloadedClassCount ^ (maxUnloadedClassCount >>> 32));
		result = prime * result + minLoadedClassCount;
		result = prime * result + (int) (minTotalLoadedClassCount ^ (minTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (minUnloadedClassCount ^ (minUnloadedClassCount >>> 32));
		result = prime * result + totalLoadedClassCount;
		result = prime * result + (int) (totalTotalLoadedClassCount ^ (totalTotalLoadedClassCount >>> 32));
		result = prime * result + (int) (totalUnloadedClassCount ^ (totalUnloadedClassCount >>> 32));
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
		ClassLoadingInformationData other = (ClassLoadingInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxLoadedClassCount != other.maxLoadedClassCount) {
			return false;
		}
		if (maxTotalLoadedClassCount != other.maxTotalLoadedClassCount) {
			return false;
		}
		if (maxUnloadedClassCount != other.maxUnloadedClassCount) {
			return false;
		}
		if (minLoadedClassCount != other.minLoadedClassCount) {
			return false;
		}
		if (minTotalLoadedClassCount != other.minTotalLoadedClassCount) {
			return false;
		}
		if (minUnloadedClassCount != other.minUnloadedClassCount) {
			return false;
		}
		if (totalLoadedClassCount != other.totalLoadedClassCount) {
			return false;
		}
		if (totalTotalLoadedClassCount != other.totalTotalLoadedClassCount) {
			return false;
		}
		if (totalUnloadedClassCount != other.totalUnloadedClassCount) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 4, 0, 6, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
