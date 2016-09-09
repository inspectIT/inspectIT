package rocks.inspectit.server.influx.constants;

/**
 * @author Ivan Senic
 *
 */
public interface Series {

	/**
	 * Series for the business transactions.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface BusinessTransaction extends Series {

		/**
		 * Series name.
		 */
		String NAME = "businessTransactions";

		/**
		 * Duration field.
		 */
		String FIELD_DURATION = "duration";

	}

	/**
	 * Series for the methods represented by
	 * {@link rocks.inspectit.shared.all.communication.data.TimerData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface Methods extends Series {

		/**
		 * Series name.
		 */
		String NAME = "methods";

		/**
		 * Minimum duration field.
		 */
		String FIELD_MIN_DURATION = "minDuration";

		/**
		 * Average duration field.
		 */
		String FIELD_DURATION = "avgDuration";

		/**
		 * Maximum duration field.
		 */
		String FIELD_MAX_DURATION = "maxDuration";

		/**
		 * Minimum CPU time field.
		 */
		String FIELD_MIN_CPU_TIME = "minCpuTime";

		/**
		 * Average CPU time field.
		 */
		String FIELD_CPU_TIME = "avgCpuTime";

		/**
		 * Maximum CPU time field.
		 */
		String FIELD_MAX_CPU_TIME = "maxCpuTime";

	}

	/**
	 * Series for the http calls represented by
	 * {@link rocks.inspectit.shared.all.communication.data.HttpTimerData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface Http extends Series {

		/**
		 * Series name.
		 */
		String NAME = "http";

		/**
		 * Duration field.
		 */
		String FIELD_DURATION = "duration";
	}

	/**
	 * Series for the
	 * {@link rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface ClassLoadingInfomation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "classLoading";

		/**
		 * Loaded classes count field.
		 */
		String FIELD_LOADED_CLASSES = "loadedClassCount";

		/**
		 * Total loaded classes count field.
		 */
		String FIELD_TOTAL_LOADED_CLASSES = "totalLoadedClassCount";

		/**
		 * Unloaded classes count field.
		 */
		String FIELD_UNLOADED_CLASSES = "unloadedClassCount";

	}

	/**
	 * Series for the {@link rocks.inspectit.shared.all.communication.data.CpuInformationData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface CpuInformation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "cpu";

		/**
		 * Average CPU utilization field.
		 */
		String FIELD_AVG_CPU_UTIL = "avgUtilization";

		/**
		 * Minimum CPU utilization field.
		 */
		String FIELD_MIN_CPU_UTIL = "minUtilization";

		/**
		 * Maximum CPU utilization field.
		 */
		String FIELD_MAX_CPU_UTIL = "maxUtilization";

	}

	/**
	 * Series for the {@link rocks.inspectit.shared.all.communication.data.JmxSensorValueData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface Jmx extends Series {

		/**
		 * Series name.
		 */
		String NAME = "jmx";

		/**
		 * Average value field.
		 */
		String FIELD_VALUE = "value";

	}

	/**
	 * Series for the {@link rocks.inspectit.shared.all.communication.data.MemoryInformationData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface MemoryInformation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "memory";

		/**
		 * Free physical memory field.
		 */
		String FIELD_AVG_FREE_PHYS_MEMORY = "freePhysicalMem";

		/**
		 * Free swap space field.
		 */
		String FIELD_AVG_FREE_SWAP_SPACE = "freeSwapSpace";

		/**
		 * Committed heap size field.
		 */
		String FIELD_AVG_COMMITTED_HEAP_MEMORY = "committedHeapMemorySize";

		/**
		 * Committed non-heap size field.
		 */
		String FIELD_AVG_COMMITTED_NON_HEAP_MEMORY = "committedNonHeapMemorySize";

		/**
		 * Average used heap size field.
		 */
		String FIELD_AVG_USED_HEAP_MEMORY = "avgUsedHeapMemorySize";

		/**
		 * Minimum used heap size field.
		 */
		String FIELD_MIN_USED_HEAP_MEMORY = "minUsedHeapMemorySize";

		/**
		 * Maximum used heap size field.
		 */
		String FIELD_MAX_USED_HEAP_MEMORY = "maxUsedHeapMemorySize";

		/**
		 * Average used non-heap size field.
		 */
		String FIELD_AVG_USED_NON_HEAP_MEMORY = "avgUsedNonHeapMemorySize";

		/**
		 * Minimum used non-heap size field.
		 */
		String FIELD_MIN_USED_NON_HEAP_MEMORY = "minUsedNonHeapMemorySize";

		/**
		 * Maximum used non-heap size field.
		 */
		String FIELD_MAX_USED_NON_HEAP_MEMORY = "maxUsedNonHeapMemorySize";

	}

	/**
	 * Series for the {@link rocks.inspectit.shared.all.communication.data.SystemInformationData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface SystemInformation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "jvmData";

		/**
		 * Architecture field.
		 */
		String FIELD_ARCHITECTURE = "architecture";

		/**
		 * Number of available processors field.
		 */
		String FIELD_NUM_AVAILABLE_PROCESSORS = "numAvailableProcessors";

		/**
		 * Boot class path field.
		 */
		String FIELD_BOOT_CLASS_PATH = "bootClassPath";

		/**
		 * Class path field.
		 */
		String FIELD_CLASS_PATH = "classPath";

		/**
		 * Initial heap memory size field.
		 */
		String FIELD_INIT_HEAP_MEMORY_SIZE = "initHeapMemprySize";

		/**
		 * Initial non-heap memory size field.
		 */
		String FIELD_INIT_NON_HEAP_MEMORY_SIZE = "initNonHeapMemprySize";

		/**
		 * JIT compiler name field.
		 */
		String FIELD_JIT_COMPILER_NAME = "jitCompilerName";

		/**
		 * Library path field.
		 */
		String FIELD_LIBRARY_PATH = "libraryPath";

		/**
		 * Maximum heap memory size field.
		 */
		String FIELD_MAX_HEAP_SIZE = "maxHeapMemorySize";

		/**
		 * Maximum non-heap memory size field.
		 */
		String FIELD_MAX_NON_HEAP_SIZE = "maxNonHeapMemorySize";

		/**
		 * OS name field.
		 */
		String FIELD_OS_NAME = "osName";

		/**
		 * OS version field.
		 */
		String FIELD_OS_VERSION = "osVersion";

		/**
		 * Total physical memory field.
		 */
		String FIELD_TOTAL_PHYS_MEMORY = "totalPhysicalMemory";

		/**
		 * Total swap space field.
		 */
		String FIELD_TOTAL_SWAP_SPACE = "totalSwapSpace";

		/**
		 * VM name field.
		 */
		String FIELD_VM_NAME = "vmName";

		/**
		 * CM SPEC name field.
		 */
		String FIELD_VM_SPEC_NAME = "vmSpecName";

		/**
		 * VM vendor field.
		 */
		String FIELD_VM_VENDOR = "vmVendor";

		/**
		 * VM version field.
		 */
		String FIELD_VM_VERSION = "vmVersion";

		/**
		 * VM attributes field.
		 */
		String FIELD_VM_ATTRIBUTES = "vmAttributes";

	}

	/**
	 * Series for the {@link rocks.inspectit.shared.all.communication.data.ThreadInformationData}.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface ThreadInformation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "threads";

		/**
		 * Live thread count field.
		 */
		String FIELD_LIVE_THREAD_COUNT = "liveThreadCount";

		/**
		 * Deamon thread count field.
		 */
		String FIELD_DEAMON_THREAD_COUNT = "daemonThreadCount";

		/**
		 * Total started thread count.
		 */
		String FIELD_TOTAL_STARTED_THREAD_COUNT = "totalStartedThreadCount";

		/**
		 * Peak thread count.
		 */
		String FIELD_PEAK_THREAD_COUNT = "peakThreadCount";

	}
}
