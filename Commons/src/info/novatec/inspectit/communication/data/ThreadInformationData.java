package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * This class provide dynamic informations about the threads running/started in the virtual machine.
 * <p>
 * This class implements the {@link IAggregatedData} interface but does not provide the IDs of the
 * aggregated instances since they are not related to any data and are useless.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class ThreadInformationData extends SystemSensorData implements IAggregatedData<ThreadInformationData> {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -4782628082344900101L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum number of live daemon threads.
	 */
	private int minDaemonThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of live daemon threads.
	 */
	private int maxDaemonThreadCount = 0;

	/**
	 * The total number of live daemon threads.
	 */
	private int totalDaemonThreadCount = 0;

	/**
	 * The minimum peak live thread count since the virtual machine has started.
	 */
	private int minPeakThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum peak live thread count since the virtual machine has started.
	 */
	private int maxPeakThreadCount = 0;

	/**
	 * The total peak live thread count since the virtual machine has started.
	 */
	private int totalPeakThreadCount = 0;

	/**
	 * The minimum number of live threads including both daemon and non-daemon threads.
	 */
	private int minThreadCount = Integer.MAX_VALUE;

	/**
	 * The maximum number of live threads including both daemon and non-daemon threads.
	 */
	private int maxThreadCount = 0;

	/**
	 * The total number of live threads including both daemon and non-daemon threads.
	 */
	private int totalThreadCount = 0;

	/**
	 * The minimum number of total threads created and also started since the virtual machine
	 * started.
	 */
	private long minTotalStartedThreadCount = Long.MAX_VALUE;

	/**
	 * The maximum number of total threads created and also started since the virtual machine
	 * started.
	 */
	private long maxTotalStartedThreadCount = 0;

	/**
	 * The total number of total threads created and also started since the virtual machine started.
	 */
	private long totalTotalStartedThreadCount = 0;

	/**
	 * Default no-args constructor.
	 */
	public ThreadInformationData() {
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
	public ThreadInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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
	 * increases the peak thread count by the given number.
	 * 
	 * @param peakThreadCount
	 *            the number to increase the peak thread count by.
	 */
	public void addPeakThreadCount(int peakThreadCount) {
		this.totalPeakThreadCount += peakThreadCount;
	}

	/**
	 * increases the daemon thread count by the given number.
	 * 
	 * @param daemonThreadCount
	 *            the number to increase the daemon thread count.
	 */
	public void addDaemonThreadCount(int daemonThreadCount) {
		this.totalDaemonThreadCount += daemonThreadCount;
	}

	/**
	 * adds the given number to the thread count.
	 * 
	 * @param threadCount
	 *            the number to increase the thread count.
	 */
	public void addThreadCount(int threadCount) {
		this.totalThreadCount += threadCount;
	}

	/**
	 * Gets {@link #minDaemonThreadCount}.
	 * 
	 * @return {@link #minDaemonThreadCount}
	 */
	public int getMinDaemonThreadCount() {
		return minDaemonThreadCount;
	}

	/**
	 * Sets {@link #minDaemonThreadCount}.
	 * 
	 * @param minDaemonThreadCount
	 *            New value for {@link #minDaemonThreadCount}
	 */
	public void setMinDaemonThreadCount(int minDaemonThreadCount) {
		this.minDaemonThreadCount = minDaemonThreadCount;
	}

	/**
	 * Gets {@link #maxDaemonThreadCount}.
	 * 
	 * @return {@link #maxDaemonThreadCount}
	 */
	public int getMaxDaemonThreadCount() {
		return maxDaemonThreadCount;
	}

	/**
	 * Sets {@link #maxDaemonThreadCount}.
	 * 
	 * @param maxDaemonThreadCount
	 *            New value for {@link #maxDaemonThreadCount}
	 */
	public void setMaxDaemonThreadCount(int maxDaemonThreadCount) {
		this.maxDaemonThreadCount = maxDaemonThreadCount;
	}

	/**
	 * Gets {@link #totalDaemonThreadCount}.
	 * 
	 * @return {@link #totalDaemonThreadCount}
	 */
	public int getTotalDaemonThreadCount() {
		return totalDaemonThreadCount;
	}

	/**
	 * Sets {@link #totalDaemonThreadCount}.
	 * 
	 * @param totalDaemonThreadCount
	 *            New value for {@link #totalDaemonThreadCount}
	 */
	public void setTotalDaemonThreadCount(int totalDaemonThreadCount) {
		this.totalDaemonThreadCount = totalDaemonThreadCount;
	}

	/**
	 * Gets {@link #minPeakThreadCount}.
	 * 
	 * @return {@link #minPeakThreadCount}
	 */
	public int getMinPeakThreadCount() {
		return minPeakThreadCount;
	}

	/**
	 * Sets {@link #minPeakThreadCount}.
	 * 
	 * @param minPeakThreadCount
	 *            New value for {@link #minPeakThreadCount}
	 */
	public void setMinPeakThreadCount(int minPeakThreadCount) {
		this.minPeakThreadCount = minPeakThreadCount;
	}

	/**
	 * Gets {@link #maxPeakThreadCount}.
	 * 
	 * @return {@link #maxPeakThreadCount}
	 */
	public int getMaxPeakThreadCount() {
		return maxPeakThreadCount;
	}

	/**
	 * Sets {@link #maxPeakThreadCount}.
	 * 
	 * @param maxPeakThreadCount
	 *            New value for {@link #maxPeakThreadCount}
	 */
	public void setMaxPeakThreadCount(int maxPeakThreadCount) {
		this.maxPeakThreadCount = maxPeakThreadCount;
	}

	/**
	 * Gets {@link #totalPeakThreadCount}.
	 * 
	 * @return {@link #totalPeakThreadCount}
	 */
	public int getTotalPeakThreadCount() {
		return totalPeakThreadCount;
	}

	/**
	 * Sets {@link #totalPeakThreadCount}.
	 * 
	 * @param totalPeakThreadCount
	 *            New value for {@link #totalPeakThreadCount}
	 */
	public void setTotalPeakThreadCount(int totalPeakThreadCount) {
		this.totalPeakThreadCount = totalPeakThreadCount;
	}

	/**
	 * Gets {@link #minThreadCount}.
	 * 
	 * @return {@link #minThreadCount}
	 */
	public int getMinThreadCount() {
		return minThreadCount;
	}

	/**
	 * Sets {@link #minThreadCount}.
	 * 
	 * @param minThreadCount
	 *            New value for {@link #minThreadCount}
	 */
	public void setMinThreadCount(int minThreadCount) {
		this.minThreadCount = minThreadCount;
	}

	/**
	 * Gets {@link #maxThreadCount}.
	 * 
	 * @return {@link #maxThreadCount}
	 */
	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	/**
	 * Sets {@link #maxThreadCount}.
	 * 
	 * @param maxThreadCount
	 *            New value for {@link #maxThreadCount}
	 */
	public void setMaxThreadCount(int maxThreadCount) {
		this.maxThreadCount = maxThreadCount;
	}

	/**
	 * Gets {@link #totalThreadCount}.
	 * 
	 * @return {@link #totalThreadCount}
	 */
	public int getTotalThreadCount() {
		return totalThreadCount;
	}

	/**
	 * Sets {@link #totalThreadCount}.
	 * 
	 * @param totalThreadCount
	 *            New value for {@link #totalThreadCount}
	 */
	public void setTotalThreadCount(int totalThreadCount) {
		this.totalThreadCount = totalThreadCount;
	}

	/**
	 * increase the total number of started threads by the given value.
	 * 
	 * @param totalStartedThreadCount
	 *            the value to increase the total number of started threads.
	 */
	public void addTotalStartedThreadCount(long totalStartedThreadCount) {
		this.totalTotalStartedThreadCount += totalStartedThreadCount;
	}

	/**
	 * Gets {@link #minTotalStartedThreadCount}.
	 * 
	 * @return {@link #minTotalStartedThreadCount}
	 */
	public long getMinTotalStartedThreadCount() {
		return minTotalStartedThreadCount;
	}

	/**
	 * Sets {@link #minTotalStartedThreadCount}.
	 * 
	 * @param minTotalStartedThreadCount
	 *            New value for {@link #minTotalStartedThreadCount}
	 */
	public void setMinTotalStartedThreadCount(long minTotalStartedThreadCount) {
		this.minTotalStartedThreadCount = minTotalStartedThreadCount;
	}

	/**
	 * Gets {@link #maxTotalStartedThreadCount}.
	 * 
	 * @return {@link #maxTotalStartedThreadCount}
	 */
	public long getMaxTotalStartedThreadCount() {
		return maxTotalStartedThreadCount;
	}

	/**
	 * Sets {@link #maxTotalStartedThreadCount}.
	 * 
	 * @param maxTotalStartedThreadCount
	 *            New value for {@link #maxTotalStartedThreadCount}
	 */
	public void setMaxTotalStartedThreadCount(long maxTotalStartedThreadCount) {
		this.maxTotalStartedThreadCount = maxTotalStartedThreadCount;
	}

	/**
	 * Gets {@link #totalTotalStartedThreadCount}.
	 * 
	 * @return {@link #totalTotalStartedThreadCount}
	 */
	public long getTotalTotalStartedThreadCount() {
		return totalTotalStartedThreadCount;
	}

	/**
	 * Sets {@link #totalTotalStartedThreadCount}.
	 * 
	 * @param totalTotalStartedThreadCount
	 *            New value for {@link #totalTotalStartedThreadCount}
	 */
	public void setTotalTotalStartedThreadCount(long totalTotalStartedThreadCount) {
		this.totalTotalStartedThreadCount = totalTotalStartedThreadCount;
	}

	/**
	 * Aggregates other class loading object info this object.
	 * 
	 * @param other
	 *            Object to aggregate data from.
	 */
	public void aggregate(ThreadInformationData other) {
		count += other.count;

		minDaemonThreadCount = Math.min(other.getMinDaemonThreadCount(), minDaemonThreadCount);
		minPeakThreadCount = Math.min(other.getMinPeakThreadCount(), minPeakThreadCount);
		minThreadCount = Math.min(other.getMinThreadCount(), minThreadCount);
		minTotalStartedThreadCount = Math.min(other.getMinTotalStartedThreadCount(), minTotalStartedThreadCount);

		maxDaemonThreadCount = Math.max(other.getMaxDaemonThreadCount(), maxDaemonThreadCount);
		maxPeakThreadCount = Math.max(other.getMaxPeakThreadCount(), maxPeakThreadCount);
		maxThreadCount = Math.max(other.getMaxThreadCount(), maxThreadCount);
		maxTotalStartedThreadCount = Math.max(other.getMaxTotalStartedThreadCount(), maxTotalStartedThreadCount);

		totalDaemonThreadCount += other.getTotalDaemonThreadCount();
		totalPeakThreadCount += other.getTotalPeakThreadCount();
		totalThreadCount += other.getTotalThreadCount();
		totalTotalStartedThreadCount += other.getTotalTotalStartedThreadCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public ThreadInformationData getData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + maxDaemonThreadCount;
		result = prime * result + maxPeakThreadCount;
		result = prime * result + maxThreadCount;
		result = prime * result + (int) (maxTotalStartedThreadCount ^ (maxTotalStartedThreadCount >>> 32));
		result = prime * result + minDaemonThreadCount;
		result = prime * result + minPeakThreadCount;
		result = prime * result + minThreadCount;
		result = prime * result + (int) (minTotalStartedThreadCount ^ (minTotalStartedThreadCount >>> 32));
		result = prime * result + totalDaemonThreadCount;
		result = prime * result + totalPeakThreadCount;
		result = prime * result + totalThreadCount;
		result = prime * result + (int) (totalTotalStartedThreadCount ^ (totalTotalStartedThreadCount >>> 32));
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
		ThreadInformationData other = (ThreadInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxDaemonThreadCount != other.maxDaemonThreadCount) {
			return false;
		}
		if (maxPeakThreadCount != other.maxPeakThreadCount) {
			return false;
		}
		if (maxThreadCount != other.maxThreadCount) {
			return false;
		}
		if (maxTotalStartedThreadCount != other.maxTotalStartedThreadCount) {
			return false;
		}
		if (minDaemonThreadCount != other.minDaemonThreadCount) {
			return false;
		}
		if (minPeakThreadCount != other.minPeakThreadCount) {
			return false;
		}
		if (minThreadCount != other.minThreadCount) {
			return false;
		}
		if (minTotalStartedThreadCount != other.minTotalStartedThreadCount) {
			return false;
		}
		if (totalDaemonThreadCount != other.totalDaemonThreadCount) {
			return false;
		}
		if (totalPeakThreadCount != other.totalPeakThreadCount) {
			return false;
		}
		if (totalThreadCount != other.totalThreadCount) {
			return false;
		}
		if (totalTotalStartedThreadCount != other.totalTotalStartedThreadCount) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 10, 0, 3, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
