package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.service.AgentStorageService;
import info.novatec.inspectit.cmr.service.ICmrManagementService;
import info.novatec.inspectit.cmr.storage.CmrStorageManager;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;
import info.novatec.inspectit.storage.recording.RecordingState;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This service is used to check the health of the CMR in terms of cpu, memory, some overall
 * statistics etc.
 * 
 * @author Patrice Bouillet
 * 
 */
@Component
public class HealthStatus {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * For the visualization of the memory and load average, a graphical visualization is put into
	 * the log for easier analysis. This char is used as the start and the end of the printed lines.
	 */
	private static final char START_END_CHAR = '+';

	/**
	 * The width of the visualization of the memory and load average.
	 */
	private static final int WIDTH = 30;

	/**
	 * The fixed rate of the refresh rate for gathering the statistics.
	 */
	private static final int FIXED_RATE = 60000;

	/**
	 * Are the beans that are responsible for creating the Health Status available.
	 */
	private boolean beansAvailable = false;

	/**
	 * The memory mx bean used to extract information about the memory of the system.
	 */
	private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

	/**
	 * The operating system mx bean.
	 */
	private OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The thread mx bean.
	 */
	private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	/**
	 * The runtime mx bean.
	 */
	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * Buffer that reports status.
	 */
	@Autowired
	private IBuffer<?> buffer;

	/**
	 * {@link AgentStorageService} for reporting the amount of dropped data on the CMR.
	 */
	@Autowired
	private ICmrManagementService cmrManagementService;

	/**
	 * {@link WritingChannelManager} for status of IO tasks.
	 */
	@Autowired
	private WritingChannelManager writingChannelManager;

	/**
	 * Storage manager for status of writing tasks.
	 */
	@Autowired
	private CmrStorageManager storageManager;

	/**
	 * Byte buffer provider for the buffers pool status.
	 */
	@Autowired
	private ByteBufferProvider byteBufferProvider;

	/**
	 * Log all the statistics.
	 */
	@Scheduled(fixedRate = FIXED_RATE)
	public void logStatistics() {
		if (beansAvailable) {
			if (log.isInfoEnabled()) {
				logOperatingSystemStatistics();
				logRuntimeStatistics();
				logMemoryStatistics();
				logThreadStatistics();
				log.info("\n");
			}
		}
		if (log.isInfoEnabled()) {
			logDroppedData();
			logBufferStatistics();
			logStorageStatistics();
		}
	}

	/**
	 * Log the operating system statistics.
	 */
	private void logOperatingSystemStatistics() {
		String arch = operatingSystemMXBean.getArch();
		String name = operatingSystemMXBean.getName();
		String version = operatingSystemMXBean.getVersion();
		int availCpus = operatingSystemMXBean.getAvailableProcessors();
		double loadAverage = operatingSystemMXBean.getSystemLoadAverage();

		StringBuilder sb = new StringBuilder();
		sb.append("System: ");
		sb.append(name);
		sb.append(' ');
		sb.append(version);
		sb.append(' ');
		sb.append(arch);
		sb.append(" (");
		sb.append(availCpus);
		sb.append(" cpu(s) load average: ");
		sb.append(loadAverage);
		log.info(sb.toString());

		logGraphicalLoadAverage(loadAverage, availCpus);
	}

	/**
	 * Log a graphical version of the load average.
	 * 
	 * @param loadAvg
	 *            The current load average over the last 60 seconds.
	 * @param availCpus
	 *            The available cpus.
	 * 
	 * @see OperatingSystemMXBean#getSystemLoadAverage()
	 */
	private void logGraphicalLoadAverage(double loadAvg, int availCpus) {
		double loadAverage = loadAvg;
		if (loadAverage < 0) {
			loadAverage = 0;
		}
		double value = (double) WIDTH / availCpus;
		long load = Math.round(loadAverage * value);
		if (load > WIDTH) {
			// Necessary so that we don't brake the limit in graphical representation
			load = WIDTH;
		}
		String title = "CPU load";

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		sb.append('-');
		sb.append(title);
		for (int i = title.length() + 1; i < WIDTH; i++) {
			sb.append('-');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());

		// now create the middle line with the status.
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < load; i++) {
			sb.append('/');
		}
		// now fill up the remaining space
		for (long i = load; i < WIDTH; i++) {
			sb.append(' ');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());

		// print last line
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
			sb.append('-');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());
	}

	/**
	 * Log the runtime statistics.
	 */
	private void logRuntimeStatistics() {
		String name = runtimeMXBean.getName();
		// String specName = runtimeMXBean.getSpecName();
		// String specVendor = runtimeMXBean.getSpecVendor();
		// String specVersion = runtimeMXBean.getSpecVersion();
		long uptime = runtimeMXBean.getUptime();
		String vmName = runtimeMXBean.getVmName();
		String vmVendor = runtimeMXBean.getVmVendor();

		StringBuilder sb = new StringBuilder();
		sb.append("VM: ");
		sb.append(vmName);
		sb.append(" (");
		sb.append(vmVendor);
		sb.append(") process: ");
		sb.append(name);
		sb.append(" uptime: ");
		sb.append(uptime);
		sb.append(" ms");
		log.info(sb.toString());
	}

	/**
	 * Log the memory statistics.
	 */
	private void logMemoryStatistics() {
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

		log.info("Heap: " + heapMemoryUsage);
		logGraphicalMemoryUsage(heapMemoryUsage, "Heap");
		log.info("Non Heap: " + nonHeapMemoryUsage);
		logGraphicalMemoryUsage(nonHeapMemoryUsage, "Non-Heap");
		log.info("Pending finalizations: " + memoryMXBean.getObjectPendingFinalizationCount());
	}

	/**
	 * Log a graphical version of the memory usage object..
	 * 
	 * @param memoryUsage
	 *            The memory usage object to log.
	 * @param title
	 *            Title of graphical box.
	 * 
	 * @see MemoryUsage
	 */
	private void logGraphicalMemoryUsage(MemoryUsage memoryUsage, String title) {
		if (areMemoryUsageValuesCorrect(memoryUsage)) {
			double value = (double) WIDTH / memoryUsage.getMax();
			long used = Math.round(memoryUsage.getUsed() * value);
			long committed = Math.round(memoryUsage.getCommitted() * value);

			// print first line
			StringBuilder sb = new StringBuilder();
			sb.append(START_END_CHAR);
			sb.append('-');
			sb.append(title);
			for (int i = title.length() + 1; i < WIDTH; i++) {
				sb.append('-');
			}

			sb.append(START_END_CHAR);
			log.info(sb.toString());

			// now create the middle line with the status.
			sb = new StringBuilder();
			sb.append(START_END_CHAR);
			for (int i = 0; i < used; i++) {
				sb.append('/');
			}
			long pos = used;
			if (pos <= committed) {
				// only print the char if committed is greater or equal than the
				// current position.
				for (long i = pos; i < committed; i++) {
					sb.append(' ');
				}
				sb.append('|');
				pos = committed + 1L;
			}
			// now fill up the remaining space
			for (long i = pos; i < WIDTH; i++) {
				sb.append(' ');
			}

			// only print last char if committed is smaller
			if (committed < WIDTH) {
				sb.append(START_END_CHAR);
			}
			log.info(sb.toString());

			// print last line
			sb = new StringBuilder();
			sb.append(START_END_CHAR);
			for (int i = 0; i < WIDTH; i++) {
				sb.append('-');
			}
			sb.append(START_END_CHAR);
			log.info(sb.toString());
		}
	}

	/**
	 * Checks if the values in {@link MemoryUsage} are OK for the graphical memory logging.
	 * 
	 * @param memoryUsage
	 *            {@link MemoryUsage}
	 * @return True if values are OK.
	 */
	private boolean areMemoryUsageValuesCorrect(MemoryUsage memoryUsage) {
		if (memoryUsage.getCommitted() < 0 || memoryUsage.getUsed() < 0 || memoryUsage.getMax() < 0) {
			return false;
		}
		if (memoryUsage.getUsed() > memoryUsage.getMax()) {
			return false;
		}
		if (memoryUsage.getUsed() > memoryUsage.getCommitted()) {
			return false;
		}
		if (memoryUsage.getCommitted() > memoryUsage.getMax()) {
			return false;
		}
		return true;
	}

	/**
	 * Log the thread statistics.
	 */
	private void logThreadStatistics() {
		int threadCount = threadMXBean.getThreadCount();
		long totalStartedThreads = threadMXBean.getTotalStartedThreadCount();

		StringBuilder sb = new StringBuilder();
		sb.append("Threads: ");
		sb.append(threadCount);
		sb.append(" total started: ");
		sb.append(totalStartedThreads);
		log.info(sb.toString());
	}

	/**
	 * Log buffer statistic.
	 */
	private void logBufferStatistics() {
		String[] lines = buffer.toString().split("\n");
		for (String str : lines) {
			log.info(str);
		}
		logGraphicalBufferOccupancy(buffer.getOccupancyPercentage());
	}

	/**
	 * Log a graphical version of buffer occupancy.
	 * 
	 * @param bufferOccupancy
	 *            Current buffer occupancy in percentages.
	 */
	private void logGraphicalBufferOccupancy(float bufferOccupancy) {
		String title = "Buffer";
		int used = (int) (bufferOccupancy * WIDTH);

		// print first line
		StringBuilder sb = new StringBuilder();
		sb.append(START_END_CHAR);
		sb.append('-');
		sb.append(title);
		for (int i = title.length() + 1; i < WIDTH; i++) {
			sb.append('-');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());

		// now create the middle line with the status.
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < used; i++) {
			sb.append('/');
		}
		for (int j = used; j < WIDTH; j++) {
			sb.append(' ');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());

		// print last line
		sb = new StringBuilder();
		sb.append(START_END_CHAR);
		for (int i = 0; i < WIDTH; i++) {
			sb.append('-');
		}
		sb.append(START_END_CHAR);
		log.info(sb.toString());
	}

	/**
	 * Logs the storage stats.
	 */
	private void logStorageStatistics() {
		log.info("Status of the Write Channel Manager's executor service: " + writingChannelManager.getExecutorServiceStatus());
		log.info("Status of each writable storage and its executor service:");
		Map<StorageData, String> writersStatusMap = storageManager.getWritersStatus();
		if (!writersStatusMap.isEmpty()) {
			for (Map.Entry<StorageData, String> entry : writersStatusMap.entrySet()) {
				log.info("Storage " + entry.getKey() + " - " + entry.getValue());
			}
		} else {
			log.info("No active writable storage available.");
		}

		if (storageManager.getRecordingState() == RecordingState.ON) {
			StorageData recordingStorageData = storageManager.getRecordingStorage();
			if (null != recordingStorageData) {
				log.info("Recording is active on the storage " + recordingStorageData + ".");
			}
		} else {
			log.info("Recording is not active.");
		}

		log.info("Byte buffer provider has " + byteBufferProvider.getBufferPoolSize() + " available buffers in the pool with total capacity of " + byteBufferProvider.getAvailableCapacity()
				+ " bytes. Total created capacity of the pool is " + byteBufferProvider.getCreatedCapacity() + " bytes.");
	}

	/**
	 * Logs the amount of dropped data on CMR.
	 */
	private void logDroppedData() {
		log.info("Dropped elements due to the high load on the CMR (total count): " + cmrManagementService.getDroppedDataCount());
	}

	/**
	 * Checks if the beans are available and sets the {@link #beansAvailable} depending on the
	 * result of check.
	 */
	private void startUpCheck() {
		try {
			operatingSystemMXBean.getArch();
			operatingSystemMXBean.getName();
			operatingSystemMXBean.getVersion();
			operatingSystemMXBean.getAvailableProcessors();
			operatingSystemMXBean.getSystemLoadAverage();

			runtimeMXBean.getName();
			runtimeMXBean.getUptime();
			runtimeMXBean.getVmName();
			runtimeMXBean.getVmVendor();

			memoryMXBean.getHeapMemoryUsage();
			memoryMXBean.getNonHeapMemoryUsage();

			threadMXBean.getThreadCount();
			threadMXBean.getTotalStartedThreadCount();

			beansAvailable = true;
		} catch (Exception e) {
			beansAvailable = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		startUpCheck();
		if (beansAvailable) {
			if (log.isInfoEnabled()) {
				log.info("Health Service active...");
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("Health Service not active...");
			}
		}
	}

}
