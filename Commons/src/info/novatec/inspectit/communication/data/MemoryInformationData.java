package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.SystemSensorData;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * This class provide dynamic informations about the memory of the underlying operating system and
 * also heap and non-heap memory information of the virtual machine.
 * <p>
 * This class implements the {@link IAggregatedData} interface but does not provide the IDs of the
 * aggregated instances since they are not related to any data and are useless.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@Entity
public class MemoryInformationData extends SystemSensorData implements IAggregatedData<MemoryInformationData> {

	/**
	 * The serial version uid for this class.
	 */
	private static final long serialVersionUID = -8065956301565979083L;

	/**
	 * The count.
	 */
	private int count = 0;

	/**
	 * The minimum amount of free physical memory.
	 */
	private long minFreePhysMemory = Long.MAX_VALUE;

	/**
	 * The maximum amount of free physical memory.
	 */
	private long maxFreePhysMemory = 0;

	/**
	 * The total amount of free physical memory.
	 */
	private long totalFreePhysMemory = 0;

	/**
	 * The minimum amount of free swap space.
	 */
	private long minFreeSwapSpace = Long.MAX_VALUE;

	/**
	 * The maximum amount of free swap space.
	 */
	private long maxFreeSwapSpace = 0;

	/**
	 * The total amount of free swap space.
	 */
	private long totalFreeSwapSpace = 0;

	/**
	 * The minimum amount of virtual memory that is guaranteed to be available to the running
	 * process.
	 */
	private long minComittedVirtualMemSize = Long.MAX_VALUE;

	/**
	 * The maximum amount of virtual memory that is guaranteed to be available to the running
	 * process.
	 */
	private long maxComittedVirtualMemSize = 0;

	/**
	 * The total amount of virtual memory that is guaranteed to be available to the running process.
	 */
	private long totalComittedVirtualMemSize = 0;

	/**
	 * The minimum of memory usage of the heap that is used for object allocation.
	 */
	private long minUsedHeapMemorySize = Long.MAX_VALUE;

	/**
	 * The maximum of memory usage of the heap that is used for object allocation.
	 */
	private long maxUsedHeapMemorySize = 0;

	/**
	 * The total of memory usage of the heap that is used for object allocation.
	 */
	private long totalUsedHeapMemorySize = 0;

	/**
	 * The minimum amount of memory that is guaranteed to be available for use by the virtual
	 * machine for heap memory usage.
	 */
	private long minComittedHeapMemorySize = Long.MAX_VALUE;

	/**
	 * The maximum amount of memory that is guaranteed to be available for use by the virtual
	 * machine for heap memory usage.
	 */
	private long maxComittedHeapMemorySize = 0;

	/**
	 * The total amount of memory that is guaranteed to be available for use by the virtual machine
	 * for heap memory usage.
	 */
	private long totalComittedHeapMemorySize = 0;

	/**
	 * The minimum amount of memory for non-heap memory usage of the virtual machine.
	 */
	private long minUsedNonHeapMemorySize = Long.MAX_VALUE;

	/**
	 * The maximum amount of memory for non-heap memory usage of the virtual machine.
	 */
	private long maxUsedNonHeapMemorySize = 0;

	/**
	 * The total amount of memory for non-heap memory usage of the virtual machine.
	 */
	private long totalUsedNonHeapMemorySize = 0;

	/**
	 * The minimum amount of memory that is guaranteed to be available for use by the virtual
	 * machine for non-heap memory usage.
	 */
	private long minComittedNonHeapMemorySize = Long.MAX_VALUE;

	/**
	 * The maximum amount of memory that is guaranteed to be available for use by the virtual
	 * machine for non-heap memory usage.
	 */
	private long maxComittedNonHeapMemorySize = 0;

	/**
	 * The total amount of memory that is guaranteed to be available for use by the virtual machine
	 * for non-heap memory usage.
	 */
	private long totalComittedNonHeapMemorySize = 0;

	/**
	 * Default no-args constructor.
	 */
	public MemoryInformationData() {
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
	public MemoryInformationData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent) {
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
	 * increases the count.
	 */
	public void incrementCount() {
		this.count++;
	}

	/**
	 * adds the given value to the free physical memory.
	 * 
	 * @param freePhysMemory
	 *            the value to add.
	 */
	public void addFreePhysMemory(long freePhysMemory) {
		this.totalFreePhysMemory += freePhysMemory;
	}

	/**
	 * Gets {@link #minFreePhysMemory}.
	 * 
	 * @return {@link #minFreePhysMemory}
	 */
	public long getMinFreePhysMemory() {
		return minFreePhysMemory;
	}

	/**
	 * Sets {@link #minFreePhysMemory}.
	 * 
	 * @param minFreePhysMemory
	 *            New value for {@link #minFreePhysMemory}
	 */
	public void setMinFreePhysMemory(long minFreePhysMemory) {
		this.minFreePhysMemory = minFreePhysMemory;
	}

	/**
	 * Gets {@link #maxFreePhysMemory}.
	 * 
	 * @return {@link #maxFreePhysMemory}
	 */
	public long getMaxFreePhysMemory() {
		return maxFreePhysMemory;
	}

	/**
	 * Sets {@link #maxFreePhysMemory}.
	 * 
	 * @param maxFreePhysMemory
	 *            New value for {@link #maxFreePhysMemory}
	 */
	public void setMaxFreePhysMemory(long maxFreePhysMemory) {
		this.maxFreePhysMemory = maxFreePhysMemory;
	}

	/**
	 * Gets {@link #totalFreePhysMemory}.
	 * 
	 * @return {@link #totalFreePhysMemory}
	 */
	public long getTotalFreePhysMemory() {
		return totalFreePhysMemory;
	}

	/**
	 * Sets {@link #totalFreePhysMemory}.
	 * 
	 * @param totalFreePhysMemory
	 *            New value for {@link #totalFreePhysMemory}
	 */
	public void setTotalFreePhysMemory(long totalFreePhysMemory) {
		this.totalFreePhysMemory = totalFreePhysMemory;
	}

	/**
	 * adds the given value to the free swap space.
	 * 
	 * @param freeSwapSpace
	 *            the value to add.
	 */
	public void addFreeSwapSpace(long freeSwapSpace) {
		this.totalFreeSwapSpace += freeSwapSpace;
	}

	/**
	 * Gets {@link #minFreeSwapSpace}.
	 * 
	 * @return {@link #minFreeSwapSpace}
	 */
	public long getMinFreeSwapSpace() {
		return minFreeSwapSpace;
	}

	/**
	 * Sets {@link #minFreeSwapSpace}.
	 * 
	 * @param minFreeSwapSpace
	 *            New value for {@link #minFreeSwapSpace}
	 */
	public void setMinFreeSwapSpace(long minFreeSwapSpace) {
		this.minFreeSwapSpace = minFreeSwapSpace;
	}

	/**
	 * Gets {@link #maxFreeSwapSpace}.
	 * 
	 * @return {@link #maxFreeSwapSpace}
	 */
	public long getMaxFreeSwapSpace() {
		return maxFreeSwapSpace;
	}

	/**
	 * Sets {@link #maxFreeSwapSpace}.
	 * 
	 * @param maxFreeSwapSpace
	 *            New value for {@link #maxFreeSwapSpace}
	 */
	public void setMaxFreeSwapSpace(long maxFreeSwapSpace) {
		this.maxFreeSwapSpace = maxFreeSwapSpace;
	}

	/**
	 * Gets {@link #totalFreeSwapSpace}.
	 * 
	 * @return {@link #totalFreeSwapSpace}
	 */
	public long getTotalFreeSwapSpace() {
		return totalFreeSwapSpace;
	}

	/**
	 * Sets {@link #totalFreeSwapSpace}.
	 * 
	 * @param totalFreeSwapSpace
	 *            New value for {@link #totalFreeSwapSpace}
	 */
	public void setTotalFreeSwapSpace(long totalFreeSwapSpace) {
		this.totalFreeSwapSpace = totalFreeSwapSpace;
	}

	/**
	 * adds the given value to the comittedVirtualMemSize.
	 * 
	 * @param comittedVirtualMemSize
	 *            the value to add.
	 */
	public void addComittedVirtualMemSize(long comittedVirtualMemSize) {
		this.totalComittedVirtualMemSize += comittedVirtualMemSize;
	}

	/**
	 * Gets {@link #minComittedVirtualMemSize}.
	 * 
	 * @return {@link #minComittedVirtualMemSize}
	 */
	public long getMinComittedVirtualMemSize() {
		return minComittedVirtualMemSize;
	}

	/**
	 * Sets {@link #minComittedVirtualMemSize}.
	 * 
	 * @param minComittedVirtualMemSize
	 *            New value for {@link #minComittedVirtualMemSize}
	 */
	public void setMinComittedVirtualMemSize(long minComittedVirtualMemSize) {
		this.minComittedVirtualMemSize = minComittedVirtualMemSize;
	}

	/**
	 * Gets {@link #maxComittedVirtualMemSize}.
	 * 
	 * @return {@link #maxComittedVirtualMemSize}
	 */
	public long getMaxComittedVirtualMemSize() {
		return maxComittedVirtualMemSize;
	}

	/**
	 * Sets {@link #maxComittedVirtualMemSize}.
	 * 
	 * @param maxComittedVirtualMemSize
	 *            New value for {@link #maxComittedVirtualMemSize}
	 */
	public void setMaxComittedVirtualMemSize(long maxComittedVirtualMemSize) {
		this.maxComittedVirtualMemSize = maxComittedVirtualMemSize;
	}

	/**
	 * Gets {@link #totalComittedVirtualMemSize}.
	 * 
	 * @return {@link #totalComittedVirtualMemSize}
	 */
	public long getTotalComittedVirtualMemSize() {
		return totalComittedVirtualMemSize;
	}

	/**
	 * Sets {@link #totalComittedVirtualMemSize}.
	 * 
	 * @param totalComittedVirtualMemSize
	 *            New value for {@link #totalComittedVirtualMemSize}
	 */
	public void setTotalComittedVirtualMemSize(long totalComittedVirtualMemSize) {
		this.totalComittedVirtualMemSize = totalComittedVirtualMemSize;
	}

	/**
	 * adds the given value to the usedHeapMemorySize.
	 * 
	 * @param usedHeapMemorySize
	 *            the value to add.
	 */
	public void addUsedHeapMemorySize(long usedHeapMemorySize) {
		this.totalUsedHeapMemorySize += usedHeapMemorySize;
	}

	/**
	 * Gets {@link #minUsedHeapMemorySize}.
	 * 
	 * @return {@link #minUsedHeapMemorySize}
	 */
	public long getMinUsedHeapMemorySize() {
		return minUsedHeapMemorySize;
	}

	/**
	 * Sets {@link #minUsedHeapMemorySize}.
	 * 
	 * @param minUsedHeapMemorySize
	 *            New value for {@link #minUsedHeapMemorySize}
	 */
	public void setMinUsedHeapMemorySize(long minUsedHeapMemorySize) {
		this.minUsedHeapMemorySize = minUsedHeapMemorySize;
	}

	/**
	 * Gets {@link #maxUsedHeapMemorySize}.
	 * 
	 * @return {@link #maxUsedHeapMemorySize}
	 */
	public long getMaxUsedHeapMemorySize() {
		return maxUsedHeapMemorySize;
	}

	/**
	 * Sets {@link #maxUsedHeapMemorySize}.
	 * 
	 * @param maxUsedHeapMemorySize
	 *            New value for {@link #maxUsedHeapMemorySize}
	 */
	public void setMaxUsedHeapMemorySize(long maxUsedHeapMemorySize) {
		this.maxUsedHeapMemorySize = maxUsedHeapMemorySize;
	}

	/**
	 * Gets {@link #totalUsedHeapMemorySize}.
	 * 
	 * @return {@link #totalUsedHeapMemorySize}
	 */
	public long getTotalUsedHeapMemorySize() {
		return totalUsedHeapMemorySize;
	}

	/**
	 * Sets {@link #totalUsedHeapMemorySize}.
	 * 
	 * @param totalUsedHeapMemorySize
	 *            New value for {@link #totalUsedHeapMemorySize}
	 */
	public void setTotalUsedHeapMemorySize(long totalUsedHeapMemorySize) {
		this.totalUsedHeapMemorySize = totalUsedHeapMemorySize;
	}

	/**
	 * adds the given value to the comittedHeapMemorySize.
	 * 
	 * @param comittedHeapMemorySize
	 *            the value to add.
	 */
	public void addComittedHeapMemorySize(long comittedHeapMemorySize) {
		this.totalComittedHeapMemorySize += comittedHeapMemorySize;
	}

	/**
	 * Gets {@link #minComittedHeapMemorySize}.
	 * 
	 * @return {@link #minComittedHeapMemorySize}
	 */
	public long getMinComittedHeapMemorySize() {
		return minComittedHeapMemorySize;
	}

	/**
	 * Sets {@link #minComittedHeapMemorySize}.
	 * 
	 * @param minComittedHeapMemorySize
	 *            New value for {@link #minComittedHeapMemorySize}
	 */
	public void setMinComittedHeapMemorySize(long minComittedHeapMemorySize) {
		this.minComittedHeapMemorySize = minComittedHeapMemorySize;
	}

	/**
	 * Gets {@link #maxComittedHeapMemorySize}.
	 * 
	 * @return {@link #maxComittedHeapMemorySize}
	 */
	public long getMaxComittedHeapMemorySize() {
		return maxComittedHeapMemorySize;
	}

	/**
	 * Sets {@link #maxComittedHeapMemorySize}.
	 * 
	 * @param maxComittedHeapMemorySize
	 *            New value for {@link #maxComittedHeapMemorySize}
	 */
	public void setMaxComittedHeapMemorySize(long maxComittedHeapMemorySize) {
		this.maxComittedHeapMemorySize = maxComittedHeapMemorySize;
	}

	/**
	 * Gets {@link #totalComittedHeapMemorySize}.
	 * 
	 * @return {@link #totalComittedHeapMemorySize}
	 */
	public long getTotalComittedHeapMemorySize() {
		return totalComittedHeapMemorySize;
	}

	/**
	 * Sets {@link #totalComittedHeapMemorySize}.
	 * 
	 * @param totalComittedHeapMemorySize
	 *            New value for {@link #totalComittedHeapMemorySize}
	 */
	public void setTotalComittedHeapMemorySize(long totalComittedHeapMemorySize) {
		this.totalComittedHeapMemorySize = totalComittedHeapMemorySize;
	}

	/**
	 * adds the given value to the usedNonHeapMemorySize.
	 * 
	 * @param usedNonHeapMemorySize
	 *            the value to add.
	 */
	public void addUsedNonHeapMemorySize(long usedNonHeapMemorySize) {
		this.totalUsedNonHeapMemorySize += usedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #minUsedNonHeapMemorySize}.
	 * 
	 * @return {@link #minUsedNonHeapMemorySize}
	 */
	public long getMinUsedNonHeapMemorySize() {
		return minUsedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #minUsedNonHeapMemorySize}.
	 * 
	 * @param minUsedNonHeapMemorySize
	 *            New value for {@link #minUsedNonHeapMemorySize}
	 */
	public void setMinUsedNonHeapMemorySize(long minUsedNonHeapMemorySize) {
		this.minUsedNonHeapMemorySize = minUsedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #maxUsedNonHeapMemorySize}.
	 * 
	 * @return {@link #maxUsedNonHeapMemorySize}
	 */
	public long getMaxUsedNonHeapMemorySize() {
		return maxUsedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #maxUsedNonHeapMemorySize}.
	 * 
	 * @param maxUsedNonHeapMemorySize
	 *            New value for {@link #maxUsedNonHeapMemorySize}
	 */
	public void setMaxUsedNonHeapMemorySize(long maxUsedNonHeapMemorySize) {
		this.maxUsedNonHeapMemorySize = maxUsedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #totalUsedNonHeapMemorySize}.
	 * 
	 * @return {@link #totalUsedNonHeapMemorySize}
	 */
	public long getTotalUsedNonHeapMemorySize() {
		return totalUsedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #totalUsedNonHeapMemorySize}.
	 * 
	 * @param totalUsedNonHeapMemorySize
	 *            New value for {@link #totalUsedNonHeapMemorySize}
	 */
	public void setTotalUsedNonHeapMemorySize(long totalUsedNonHeapMemorySize) {
		this.totalUsedNonHeapMemorySize = totalUsedNonHeapMemorySize;
	}

	/**
	 * adds the given value to the comittedNonHeapMemorySize.
	 * 
	 * @param comittedNonHeapMemorySize
	 *            the value to add.
	 */
	public void addComittedNonHeapMemorySize(long comittedNonHeapMemorySize) {
		this.totalComittedNonHeapMemorySize += comittedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #minComittedNonHeapMemorySize}.
	 * 
	 * @return {@link #minComittedNonHeapMemorySize}
	 */
	public long getMinComittedNonHeapMemorySize() {
		return minComittedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #minComittedNonHeapMemorySize}.
	 * 
	 * @param minComittedNonHeapMemorySize
	 *            New value for {@link #minComittedNonHeapMemorySize}
	 */
	public void setMinComittedNonHeapMemorySize(long minComittedNonHeapMemorySize) {
		this.minComittedNonHeapMemorySize = minComittedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #maxComittedNonHeapMemorySize}.
	 * 
	 * @return {@link #maxComittedNonHeapMemorySize}
	 */
	public long getMaxComittedNonHeapMemorySize() {
		return maxComittedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #maxComittedNonHeapMemorySize}.
	 * 
	 * @param maxComittedNonHeapMemorySize
	 *            New value for {@link #maxComittedNonHeapMemorySize}
	 */
	public void setMaxComittedNonHeapMemorySize(long maxComittedNonHeapMemorySize) {
		this.maxComittedNonHeapMemorySize = maxComittedNonHeapMemorySize;
	}

	/**
	 * Gets {@link #totalComittedNonHeapMemorySize}.
	 * 
	 * @return {@link #totalComittedNonHeapMemorySize}
	 */
	public long getTotalComittedNonHeapMemorySize() {
		return totalComittedNonHeapMemorySize;
	}

	/**
	 * Sets {@link #totalComittedNonHeapMemorySize}.
	 * 
	 * @param totalComittedNonHeapMemorySize
	 *            New value for {@link #totalComittedNonHeapMemorySize}
	 */
	public void setTotalComittedNonHeapMemorySize(long totalComittedNonHeapMemorySize) {
		this.totalComittedNonHeapMemorySize = totalComittedNonHeapMemorySize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(MemoryInformationData other) {
		count += other.count;

		minComittedHeapMemorySize = Math.min(minComittedHeapMemorySize, other.minComittedHeapMemorySize);
		maxComittedHeapMemorySize = Math.max(maxComittedHeapMemorySize, other.maxComittedHeapMemorySize);
		totalComittedHeapMemorySize += other.totalComittedHeapMemorySize;

		minComittedNonHeapMemorySize = Math.min(minComittedNonHeapMemorySize, other.minComittedNonHeapMemorySize);
		maxComittedNonHeapMemorySize = Math.max(maxComittedNonHeapMemorySize, other.maxComittedNonHeapMemorySize);
		totalComittedNonHeapMemorySize += other.totalComittedNonHeapMemorySize;

		minComittedVirtualMemSize = Math.min(minComittedVirtualMemSize, other.minComittedVirtualMemSize);
		maxComittedVirtualMemSize = Math.max(maxComittedVirtualMemSize, other.maxComittedVirtualMemSize);
		totalComittedVirtualMemSize += other.totalComittedVirtualMemSize;

		minFreePhysMemory = Math.min(minFreePhysMemory, other.minFreePhysMemory);
		maxFreePhysMemory = Math.max(maxFreePhysMemory, other.maxFreePhysMemory);
		totalFreePhysMemory += other.totalFreePhysMemory;

		minFreeSwapSpace = Math.min(minFreeSwapSpace, other.minFreeSwapSpace);
		maxFreeSwapSpace = Math.max(maxFreeSwapSpace, other.maxFreeSwapSpace);
		totalFreeSwapSpace += other.totalFreeSwapSpace;

		minUsedHeapMemorySize = Math.min(minUsedHeapMemorySize, other.minUsedHeapMemorySize);
		maxUsedHeapMemorySize = Math.max(maxUsedHeapMemorySize, other.maxUsedHeapMemorySize);
		totalUsedHeapMemorySize += other.totalUsedHeapMemorySize;

		minUsedNonHeapMemorySize = Math.min(minUsedNonHeapMemorySize, other.minUsedNonHeapMemorySize);
		maxUsedNonHeapMemorySize = Math.max(maxUsedNonHeapMemorySize, other.maxUsedNonHeapMemorySize);
		totalUsedNonHeapMemorySize += other.totalUsedNonHeapMemorySize;
	}

	/**
	 * {@inheritDoc}
	 */
	public MemoryInformationData getData() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + (int) (maxComittedHeapMemorySize ^ (maxComittedHeapMemorySize >>> 32));
		result = prime * result + (int) (maxComittedNonHeapMemorySize ^ (maxComittedNonHeapMemorySize >>> 32));
		result = prime * result + (int) (maxComittedVirtualMemSize ^ (maxComittedVirtualMemSize >>> 32));
		result = prime * result + (int) (maxFreePhysMemory ^ (maxFreePhysMemory >>> 32));
		result = prime * result + (int) (maxFreeSwapSpace ^ (maxFreeSwapSpace >>> 32));
		result = prime * result + (int) (maxUsedHeapMemorySize ^ (maxUsedHeapMemorySize >>> 32));
		result = prime * result + (int) (maxUsedNonHeapMemorySize ^ (maxUsedNonHeapMemorySize >>> 32));
		result = prime * result + (int) (minComittedHeapMemorySize ^ (minComittedHeapMemorySize >>> 32));
		result = prime * result + (int) (minComittedNonHeapMemorySize ^ (minComittedNonHeapMemorySize >>> 32));
		result = prime * result + (int) (minComittedVirtualMemSize ^ (minComittedVirtualMemSize >>> 32));
		result = prime * result + (int) (minFreePhysMemory ^ (minFreePhysMemory >>> 32));
		result = prime * result + (int) (minFreeSwapSpace ^ (minFreeSwapSpace >>> 32));
		result = prime * result + (int) (minUsedHeapMemorySize ^ (minUsedHeapMemorySize >>> 32));
		result = prime * result + (int) (minUsedNonHeapMemorySize ^ (minUsedNonHeapMemorySize >>> 32));
		result = prime * result + (int) (totalComittedHeapMemorySize ^ (totalComittedHeapMemorySize >>> 32));
		result = prime * result + (int) (totalComittedNonHeapMemorySize ^ (totalComittedNonHeapMemorySize >>> 32));
		result = prime * result + (int) (totalComittedVirtualMemSize ^ (totalComittedVirtualMemSize >>> 32));
		result = prime * result + (int) (totalFreePhysMemory ^ (totalFreePhysMemory >>> 32));
		result = prime * result + (int) (totalFreeSwapSpace ^ (totalFreeSwapSpace >>> 32));
		result = prime * result + (int) (totalUsedHeapMemorySize ^ (totalUsedHeapMemorySize >>> 32));
		result = prime * result + (int) (totalUsedNonHeapMemorySize ^ (totalUsedNonHeapMemorySize >>> 32));
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
		MemoryInformationData other = (MemoryInformationData) obj;
		if (count != other.count) {
			return false;
		}
		if (maxComittedHeapMemorySize != other.maxComittedHeapMemorySize) {
			return false;
		}
		if (maxComittedNonHeapMemorySize != other.maxComittedNonHeapMemorySize) {
			return false;
		}
		if (maxComittedVirtualMemSize != other.maxComittedVirtualMemSize) {
			return false;
		}
		if (maxFreePhysMemory != other.maxFreePhysMemory) {
			return false;
		}
		if (maxFreeSwapSpace != other.maxFreeSwapSpace) {
			return false;
		}
		if (maxUsedHeapMemorySize != other.maxUsedHeapMemorySize) {
			return false;
		}
		if (maxUsedNonHeapMemorySize != other.maxUsedNonHeapMemorySize) {
			return false;
		}
		if (minComittedHeapMemorySize != other.minComittedHeapMemorySize) {
			return false;
		}
		if (minComittedNonHeapMemorySize != other.minComittedNonHeapMemorySize) {
			return false;
		}
		if (minComittedVirtualMemSize != other.minComittedVirtualMemSize) {
			return false;
		}
		if (minFreePhysMemory != other.minFreePhysMemory) {
			return false;
		}
		if (minFreeSwapSpace != other.minFreeSwapSpace) {
			return false;
		}
		if (minUsedHeapMemorySize != other.minUsedHeapMemorySize) {
			return false;
		}
		if (minUsedNonHeapMemorySize != other.minUsedNonHeapMemorySize) {
			return false;
		}
		if (totalComittedHeapMemorySize != other.totalComittedHeapMemorySize) {
			return false;
		}
		if (totalComittedNonHeapMemorySize != other.totalComittedNonHeapMemorySize) {
			return false;
		}
		if (totalComittedVirtualMemSize != other.totalComittedVirtualMemSize) {
			return false;
		}
		if (totalFreePhysMemory != other.totalFreePhysMemory) {
			return false;
		}
		if (totalFreeSwapSpace != other.totalFreeSwapSpace) {
			return false;
		}
		if (totalUsedHeapMemorySize != other.totalUsedHeapMemorySize) {
			return false;
		}
		if (totalUsedNonHeapMemorySize != other.totalUsedNonHeapMemorySize) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 21, 0);
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

}
