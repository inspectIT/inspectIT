package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.spring.logger.Log;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Set of properties for one buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class BufferProperties {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Name of the memory pool for old generation.
	 */
	private static final String OLD_GEN_POOL_NAME = "Old Gen";

	/**
	 * Name of the memory pool for tenured generation. Some JVM name like this the old generation
	 * space.
	 */
	private static final String TENURED_GEN_POOL_NAME = "Tenured";

	/**
	 * Buffer eviction occupancy percentage.
	 */
	@Value(value = "${buffer.evictionOccupancyPercentage}")
	float evictionOccupancyPercentage;

	/**
	 * Maximum security object expansion rate in percentages.
	 */
	@Value(value = "${buffer.maxObjectExpansionRate}")
	float maxObjectExpansionRate;

	/**
	 * Minimum security object expansion rate in percentages.
	 */
	@Value(value = "${buffer.minObjectExpansionRate}")
	float minObjectExpansionRate;

	/**
	 * Maximum security object expansion rate active till this buffer size.
	 */
	@Value(value = "${buffer.maxObjectExpansionRateActiveTillBufferSize}")
	long maxObjectExpansionRateActiveTillBufferSize;

	/**
	 * Minimum security object expansion rate active from this buffer size.
	 */
	@Value(value = "${buffer.minObjectExpansionRateActiveFromBufferSize}")
	long minObjectExpansionRateActiveFromBufferSize;

	/**
	 * Buffer occupancy of old gen till which min expansion rate is active.
	 */
	@Value(value = "${buffer.minObjectExpansionRateActiveTillOccupancy}")
	float minObjectExpansionRateActiveTillOccupancy;

	/**
	 * Buffer occupancy of old gen from which max expansion rate is active.
	 */
	@Value(value = "${buffer.maxObjectExpansionRateActiveFromOccupancy}")
	float maxObjectExpansionRateActiveFromOccupancy;

	/**
	 * Size of the eviction fragment in percentages, in relation to the max buffer size.
	 */
	@Value(value = "${buffer.evictionFragmentSizePercentage}")
	float evictionFragmentSizePercentage;

	/**
	 * Number of bytes in % relative to the buffer size that need to be added or removed for the
	 * buffer so that update and clean of the indexing tree is performed - 5%.
	 */
	@Value(value = "${buffer.bytesMaintenancePercentage}")
	float bytesMaintenancePercentage;

	/**
	 * Number of threads that are cleaning the indexing tree.
	 */
	@Value(value = "${buffer.indexingTreeCleaningThreads}")
	int indexingTreeCleaningThreads;

	/**
	 * Time in milliseconds that the indexing thread will wait for the object to be analyzed first.
	 */
	@Value(value = "${buffer.indexingWaitTime}")
	long indexingWaitTime;

	/**
	 * Size of old space occupancy till which min occupancy will be active.
	 */
	@Value(value = "${buffer.minOldSpaceOccupancyActiveTillOldGenSize}")
	long minOldSpaceOccupancyActiveTillOldGenSize;

	/**
	 * Size of old space occupancy from which max occupancy will be active.
	 */
	@Value(value = "${buffer.maxOldSpaceOccupancyActiveFromOldGenSize}")
	long maxOldSpaceOccupancyActiveFromOldGenSize;

	/**
	 * Percentage of the min old generation heap space buffer can occupy.
	 */
	@Value(value = "${buffer.minOldSpaceOccupancy}")
	float minOldSpaceOccupancy;

	/**
	 * Percentage of the max old generation heap space buffer can occupy.
	 */
	@Value(value = "${buffer.maxOldSpaceOccupancy}")
	float maxOldSpaceOccupancy;

	/**
	 * Returns buffer eviction occupancy percentage.
	 * 
	 * @return Buffer eviction occupancy percentage as float.
	 */
	public float getEvictionOccupancyPercentage() {
		return evictionOccupancyPercentage;
	}

	/**
	 * Returns maximum security object expansion rate in percentages.
	 * 
	 * @return Maximum security object expansion rate in percentages as float.
	 */
	public float getMaxObjectExpansionRate() {
		return maxObjectExpansionRate;
	}

	/**
	 * Returns minimum security object expansion rate in percentages.
	 * 
	 * @return Minimum security object expansion rate in percentages as float.
	 */
	public float getMinObjectExpansionRate() {
		return minObjectExpansionRate;
	}

	/**
	 * Returns buffer size till which maximum object expansion rate is active.
	 * 
	 * @return Buffer size in bytes.
	 */
	public long getMaxObjectExpansionRateActiveTillBufferSize() {
		return maxObjectExpansionRateActiveTillBufferSize;
	}

	/**
	 * Returns buffer size from which minimum object expansion rate is active.
	 * 
	 * @return Buffer size in bytes.
	 */
	public long getMinObjectExpansionRateActiveFromBufferSize() {
		return minObjectExpansionRateActiveFromBufferSize;
	}

	/**
	 * Gets {@link #minObjectExpansionRateActiveTillOccupancy}.
	 * 
	 * @return {@link #minObjectExpansionRateActiveTillOccupancy}
	 */
	public float getMinObjectExpansionRateActiveTillOccupancy() {
		return minObjectExpansionRateActiveTillOccupancy;
	}

	/**
	 * Gets {@link #maxObjectExpansionRateActiveFromOccupancy}.
	 * 
	 * @return {@link #maxObjectExpansionRateActiveFromOccupancy}
	 */
	public float getMaxObjectExpansionRateActiveFromOccupancy() {
		return maxObjectExpansionRateActiveFromOccupancy;
	}

	/**
	 * Returns size of the eviction fragment in percentages, in relation to the max buffer size.
	 * 
	 * @return Eviction fragment in percentages as float.
	 */
	public float getEvictionFragmentSizePercentage() {
		return evictionFragmentSizePercentage;
	}

	/**
	 * Number of bytes that need to be added or removed for the buffer so that update and clean of
	 * the indexing tree is performed.
	 * 
	 * @param bufferSize
	 *            Size of the buffer.
	 * @return Number of bytes that need to be added or removed for the buffer so that update and
	 *         clean of the indexing tree is performed.
	 */
	public long getFlagsSetOnBytes(long bufferSize) {
		return (long) (bytesMaintenancePercentage * bufferSize);
	}

	/**
	 * @return the bytesMaintenancePercentage
	 */
	public float getBytesMaintenancePercentage() {
		return bytesMaintenancePercentage;
	}

	/**
	 * @return Number of indexing tree cleaning threads.
	 */
	public int getIndexingTreeCleaningThreads() {
		return indexingTreeCleaningThreads;
	}

	/**
	 * @return the indexingWaitTime
	 */
	public long getIndexingWaitTime() {
		return indexingWaitTime;
	}

	/**
	 * @return the minOldSpaceOccupancyActiveTillOldGenSize
	 */
	public long getMinOldSpaceOccupancyActiveTillOldGenSize() {
		return minOldSpaceOccupancyActiveTillOldGenSize;
	}

	/**
	 * @return the maxOldSpaceOccupancyActiveFromOldGenSize
	 */
	public long getMaxOldSpaceOccupancyActiveFromOldGenSize() {
		return maxOldSpaceOccupancyActiveFromOldGenSize;
	}

	/**
	 * @return the minOldSpaceOccupancy
	 */
	public float getMinOldSpaceOccupancy() {
		return minOldSpaceOccupancy;
	}

	/**
	 * @return the oldSpaceOccupancy
	 */
	public float getMaxOldSpaceOccupancy() {
		return maxOldSpaceOccupancy;
	}

	/**
	 * Returns the initial buffer size based on the property set.
	 * 
	 * @return Size in bytes.
	 */
	public long getInitialBufferSize() {
		long bufferSize = 0;
		long oldGenMax = getOldGenMax();

		// If we did not get the value, throw exception
		if (oldGenMax == 0) {
			throw new RuntimeException("Could not calculate the old generation heap space. Please make sure CMR is running on the provided JVM.");
		}

		// Otherwise calculate now
		if (oldGenMax > maxOldSpaceOccupancyActiveFromOldGenSize) {
			bufferSize = (long) (oldGenMax * maxOldSpaceOccupancy);
		} else if (oldGenMax < minOldSpaceOccupancyActiveTillOldGenSize) {
			bufferSize = (long) (oldGenMax * minOldSpaceOccupancy);
		} else {
			// delta is the value that defines how much we can extend the minimum heap
			// occupancy
			// percentage by analyzing the max memory size
			// delta is actually representing additional percentage of heap we can take
			// it is always thru that: minHeapSizeOccupancy + delta <
			// maxHeapSizeOccupancy
			float delta = (maxOldSpaceOccupancy - minOldSpaceOccupancy)
					* ((float) (oldGenMax - minOldSpaceOccupancyActiveTillOldGenSize) / (maxOldSpaceOccupancyActiveFromOldGenSize - minOldSpaceOccupancyActiveTillOldGenSize));
			bufferSize = (long) (oldGenMax * (minOldSpaceOccupancy + delta));
		}
		return bufferSize;
	}

	/**
	 * Returns memory in bytes for the given argument.
	 * 
	 * @param argument
	 *            Complete argument value.
	 * @param memoryToken
	 *            Memory token that is contained in argument. For example 'Xmx' or similar.
	 * @return Memory value in bytes.
	 */
	private long getMemorySizeFromArgument(String argument, String memoryToken) {
		try {
			int index = argument.indexOf(memoryToken) + memoryToken.length();

			String number = argument.substring(index, argument.length() - 1);
			String typeOfMemory = argument.substring(index + number.length());

			double value = Double.parseDouble(number);
			if ("K".equalsIgnoreCase(typeOfMemory)) {
				value *= 1024;
			} else if ("M".equalsIgnoreCase(typeOfMemory)) {
				value *= 1024 * 1024;
			} else if ("G".equalsIgnoreCase(typeOfMemory)) {
				value *= 1024 * 1024 * 1024;
			} else {
				value *= 1;
			}

			return (long) value;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Returns object security expansion rate based on the property set and given buffer size.
	 * 
	 * @param bufferSize
	 *            Buffer's size that expansion rate has to be calculated for.
	 * @return Expansion rate in percentages.
	 */
	public float getObjectSecurityExpansionRate(long bufferSize) {
		return (getObjectSecurityExpansionRateBufferSize(bufferSize) + getObjectSecurityExpansionRateBufferOccupancy(bufferSize, getOldGenMax())) / 2;

	}

	/**
	 * Returns object security expansion rate based on the property set and given buffer size.
	 * 
	 * @param bufferSize
	 *            Buffer's size that expansion rate has to be calculated for.
	 * @return Expansion rate in percentages.
	 */
	public float getObjectSecurityExpansionRateBufferSize(long bufferSize) {
		if (bufferSize > minObjectExpansionRateActiveFromBufferSize) {
			return minObjectExpansionRate;
		} else if (bufferSize < maxObjectExpansionRateActiveTillBufferSize) {
			return maxObjectExpansionRate;
		} else {
			// delta is the value that defines how much we can lower the maximum object security
			// rate by analyzing the given buffer size
			// it is always true that: maxObjectExpansionRate - delta > minObjectExpansionRate
			float delta = (maxObjectExpansionRate - minObjectExpansionRate)
					* ((float) (bufferSize - maxObjectExpansionRateActiveTillBufferSize) / (minObjectExpansionRateActiveFromBufferSize - maxObjectExpansionRateActiveTillBufferSize));
			return maxObjectExpansionRate - delta;
		}
	}

	/**
	 * Returns object security expansion rate based on the property set and given buffer size / old
	 * generation size.
	 * 
	 * @param bufferSize
	 *            Buffer's size that expansion rate has to be calculated for.
	 * @param oldGenMax
	 *            Old generation space.
	 * @return Expansion rate in percentages.
	 */
	float getObjectSecurityExpansionRateBufferOccupancy(long bufferSize, long oldGenMax) {
		double occupancy = (double) bufferSize / oldGenMax;

		if (occupancy < minObjectExpansionRateActiveTillOccupancy) {
			return minObjectExpansionRate;
		} else if (occupancy > maxObjectExpansionRateActiveFromOccupancy) {
			return maxObjectExpansionRate;
		} else {
			// delta is the value that defines how much we can lower the maximum object security
			// rate by analyzing the given buffer size
			// it is always true that: maxObjectExpansionRate - delta > minObjectExpansionRate
			float delta = (maxObjectExpansionRate - minObjectExpansionRate)
					* ((float) (occupancy - maxObjectExpansionRateActiveFromOccupancy) / (minObjectExpansionRateActiveTillOccupancy - maxObjectExpansionRateActiveFromOccupancy));
			return maxObjectExpansionRate - delta;
		}
	}

	/**
	 * Tries to find out the old space generation size.
	 * 
	 * @return Returns the size of the old generation space in bytes. This method will return
	 *         <code>0</code> if the calculation fails.
	 */
	long getOldGenMax() {
		long oldGenMax = 0;

		// try with Memory pool beans
		try {
			List<MemoryPoolMXBean> memBeans = ManagementFactory.getMemoryPoolMXBeans();
			for (MemoryPoolMXBean memBean : memBeans) {
				if (memBean.getName().indexOf(OLD_GEN_POOL_NAME) != -1 || memBean.getName().indexOf(TENURED_GEN_POOL_NAME) != -1) {
					MemoryUsage memUsage = memBean.getUsage();
					oldGenMax = memUsage.getMax();
					break;
				}
			}
		} catch (Exception e) {
			oldGenMax = 0;
		}

		// fall back to the Runtime bean for arguments
		try {
			if (oldGenMax == 0) {
				long maxHeap = 0;
				long newGen = 0;
				RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
				List<String> arguments = runtimeMXBean.getInputArguments();
				for (String arg : arguments) {
					if (arg.length() > 4) {
						String subedArg = arg.substring(0, 4);
						if ("-Xmx".equalsIgnoreCase(subedArg)) {
							maxHeap = getMemorySizeFromArgument(arg, subedArg);
						}
						if ("-Xmn".equalsIgnoreCase(subedArg)) {
							newGen = getMemorySizeFromArgument(arg, subedArg);
						}
					}
				}
				if (maxHeap != 0 && newGen != 0 && maxHeap > newGen) {
					oldGenMax = maxHeap - newGen;
				}
			}
		} catch (Exception e) {
			oldGenMax = 0;
		}

		return oldGenMax;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		// log first
		if (log.isInfoEnabled()) {
			log.info("|-Buffer properties initialized with following values:");
			log.info("||-Eviction occupancy percentage: " + NumberFormat.getInstance().format(evictionOccupancyPercentage * 100) + "%");
			log.info("||-Eviction fragment size percentage: " + NumberFormat.getInstance().format(evictionFragmentSizePercentage * 100) + "%");
			log.info("||-Indexing tree cleaning threads: " + NumberFormat.getInstance().format(indexingTreeCleaningThreads));
			log.info("||-Indexing waiting time: " + NumberFormat.getInstance().format(indexingWaitTime) + " ms");
			log.info("||-Min old generation occupancy percentage active till: " + NumberFormat.getInstance().format(minOldSpaceOccupancyActiveTillOldGenSize) + " bytes");
			log.info("||-Max old generation occupancy percentage active from: " + NumberFormat.getInstance().format(maxOldSpaceOccupancyActiveFromOldGenSize) + " bytes");
			log.info("||-Min old generation occupancy percentage: " + NumberFormat.getInstance().format(minOldSpaceOccupancy * 100) + "%");
			log.info("||-Max old generation occupancy percentage: " + NumberFormat.getInstance().format(maxOldSpaceOccupancy * 100) + "%");
			log.info("||-Max object size expansion: " + NumberFormat.getInstance().format(maxObjectExpansionRate * 100) + "%");
			log.info("||-Min object size expansion: " + NumberFormat.getInstance().format(minObjectExpansionRate * 100) + "%");
			log.info("||-Max object size expansion active from buffer occupancy: " + NumberFormat.getInstance().format(maxObjectExpansionRateActiveFromOccupancy * 100) + "%");
			log.info("||-Min object size expansion active till buffer occupancy: " + NumberFormat.getInstance().format(minObjectExpansionRateActiveTillOccupancy * 100) + " %");
			log.info("||-Max object size expansion active till buffer size: " + NumberFormat.getInstance().format(maxObjectExpansionRateActiveTillBufferSize) + " bytes");
			log.info("||-Min object size expansion active from buffer size: " + NumberFormat.getInstance().format(minObjectExpansionRateActiveFromBufferSize) + " bytes");
		}

		// eviction
		if (this.evictionOccupancyPercentage < 0 || this.evictionOccupancyPercentage > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Eviction occupancy must be a percentage value between 0 and 1. Initialization value is: " + evictionOccupancyPercentage);
		}
		if (this.evictionFragmentSizePercentage < 0.01 || this.evictionFragmentSizePercentage > 0.5) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Eviction fragment size must be a percentage value between 0.01 and 0.5. Initialization value is: " + evictionFragmentSizePercentage);
		}

		// expansion rate
		if (this.minObjectExpansionRateActiveFromBufferSize < this.maxObjectExpansionRateActiveTillBufferSize) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Buffer size from which minimum object expansion rate is active can not be lower than buffer size till which maximum object expansion rate is active. Initialization values are: "
							+ minObjectExpansionRateActiveFromBufferSize + " (buffer size for min object expansion rate) and " + maxObjectExpansionRateActiveTillBufferSize
							+ " (buffer size for max object expansion rate)");
		}
		if (this.minObjectExpansionRateActiveTillOccupancy > this.maxObjectExpansionRateActiveFromOccupancy) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: Buffer occupancy till which minimum object expansion rate is active can not be higher than buffer occupancy from which maximum object expansion rate is active. Initialization values are: "
							+ minObjectExpansionRateActiveTillOccupancy + " (buffer occupancy for min object expansion rate) and " + maxObjectExpansionRateActiveFromOccupancy
							+ " (buffer occupnacy for max object expansion rate)");
		}
		if (this.minObjectExpansionRateActiveTillOccupancy <= 0 || this.minObjectExpansionRateActiveTillOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min object expansion rate till buffer old space gen occupancy can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.minObjectExpansionRateActiveTillOccupancy);
		}
		if (this.maxObjectExpansionRateActiveFromOccupancy <= 0 || this.maxObjectExpansionRateActiveFromOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The max object expansion rate from buffer old space gen occupancy can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.maxObjectExpansionRateActiveFromOccupancy);
		}

		// indexing tree
		if (this.getBytesMaintenancePercentage() <= 0 && this.getBytesMaintenancePercentage() > this.getEvictionOccupancyPercentage()) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The buffer bytes maintenance percentage that activate the clean and update of the indexing tree can not be less or equal than zero nor bigger that eviction occupancy percentage. Initialization value is: "
							+ this.getBytesMaintenancePercentage());
		}
		if (this.getIndexingTreeCleaningThreads() <= 0) {
			throw new BeanInitializationException("Buffer properties initialization error: The number of indexing tree cleaning threads can not be less or equal than zero. Initialization value is: "
					+ this.getIndexingTreeCleaningThreads());
		}
		if (this.indexingWaitTime <= 0) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The indexing wait time can not be less or equal than zero. Initialization value is: " + this.indexingWaitTime);
		}

		// old space settings
		if (this.minOldSpaceOccupancyActiveTillOldGenSize <= 0) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space active till old generation size value can not be less or equal than zero. Initialization value is: "
							+ this.minOldSpaceOccupancyActiveTillOldGenSize);
		}
		if (this.maxOldSpaceOccupancyActiveFromOldGenSize <= 0) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The max buffer occupancy percentage of the old generation heap space active till old generation size value can not be less or equal than zero. Initialization value is: "
							+ this.maxOldSpaceOccupancyActiveFromOldGenSize);
		}
		if (this.minOldSpaceOccupancy > this.maxOldSpaceOccupancy) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space can not be higer than max buffer occupancy percentage of the old generation. Initialization values are: "
							+ this.minOldSpaceOccupancy + "(min), " + this.maxOldSpaceOccupancy + "(max)");
		}
		if (this.minOldSpaceOccupancy <= 0 || this.minOldSpaceOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The min buffer occupancy percentage of the old generation heap space can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.minOldSpaceOccupancy);
		}
		if (this.maxOldSpaceOccupancy <= 0 || this.maxOldSpaceOccupancy > 1) {
			throw new BeanInitializationException(
					"Buffer properties initialization error: The max buffer occupancy percentage of the old generation heap space can not be less or equal than zero, nor greater that one. Initialization value is: "
							+ this.maxOldSpaceOccupancy);
		}
	}

}
