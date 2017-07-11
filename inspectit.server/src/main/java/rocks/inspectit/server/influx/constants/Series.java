package rocks.inspectit.server.influx.constants;

import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;

/**
 * Constants for all the series that we save to the influxDB. These include series name, as well as
 * all field and tag names.
 *
 * @author Ivan Senic
 *
 */
public interface Series {

	/**
	 * Agent name tag.
	 */
	String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	String TAG_AGENT_ID = "agentId";

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

		/**
		 * Trace id field.
		 */
		String FIELD_TRACE_ID = "traceId";

		/**
		 * HTTP response code field.
		 */
		String FIELD_HTTP_RESPONSE_CODE = "httpResponseCode";

		/**
		 * Application name tag.
		 */
		String TAG_APPLICATION_NAME = "applicationName";

		/**
		 * Business transaction name tag.
		 */
		String TAG_BUSINESS_TRANSACTION_NAME = "businessTxName";

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

		/**
		 * The invocation count field.
		 */
		String FIELD_COUNT = "count";

		/**
		 * Simple method name tag.
		 */
		String TAG_METHOD_NAME = "methodName";

		/**
		 * Class FQN tag.
		 */
		String TAG_CLASS_FQN = "classFqn";

		/**
		 * Fully qualified method signature tag.
		 */
		String TAG_METHOD_SIGNATURE = "fqnMethodSignature";

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

		/**
		 * HTTP response code field.
		 */
		String FIELD_HTTP_RESPONSE_CODE = "httpResponseCode";

		/**
		 * URI tag.
		 */
		String TAG_URI = "uri";

		/**
		 * inspectIT tagging header tag.
		 */
		String TAG_INSPECTIT_TAGGING_HEADER = "inspectitTaggingHeader";
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
	 * Series for the
	 * {@link rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence}.
	 *
	 * @author Christian Voegele
	 *
	 */
	interface ProblemOccurrenceInformation extends Series {

		/**
		 * Series name.
		 */
		String NAME = "problemOccurrence";

		/**
		 * Duration of InvocationSequenceRoot.
		 */
		String FIELD_INVOCATION_ROOT_DURATION = "invocationSequenceRootDuration";

		/**
		 * GlobalContext exclusiveTime.
		 */
		String FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME = "globalContextMethodExclusiveTime";

		/**
		 * RootCause exclusiveTime.
		 */
		String FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME = "rootCauseMethodExclusiveTime";

		/**
		 * ApplicationName of ProblemOccurrence.
		 */
		String TAG_APPLICATION_NAME = "applicationName";

		/**
		 * BusinessContext of ProblemOccurrence.
		 */
		String TAG_BUSINESS_TRANSACTION_NAME = "businessTxName";

		/**
		 * Name of ProblemContext method.
		 */
		String TAG_PROBLEM_CONTEXT_METHOD_NAME = "ProblemContext";

		/**
		 * Name of RootCause method.
		 */
		String TAG_ROOTCAUSE_METHOD_NAME = "RootCause";

		/**
		 * CauseType of ProblemOccurrence.
		 */
		String TAG_CAUSESTRUCTURE_CAUSE_TYPE = "CauseType";

		/**
		 * SourceType of ProblemOccurrence.
		 */
		String TAG_CAUSESTRUCTURE_SOURCE_TYPE = "SourceType";

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

		/**
		 * JMX attribute name tag.
		 */
		String TAG_JMX_ATTRIBUTE_FULL_NAME = "name";

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
		String FIELD_INIT_HEAP_MEMORY_SIZE = "initHeapMemorySize";

		/**
		 * Initial non-heap memory size field.
		 */
		String FIELD_INIT_NON_HEAP_MEMORY_SIZE = "initNonHeapMemorySize";

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

	/**
	 * Common Fields and Tags for all EUM series.
	 *
	 * @author Jonas Kunz
	 *
	 */
	interface EUMBasicRequestSeries {
		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String TAG_BROWSER = "browser";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String TAG_DEVICE = "device";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String TAG_LANGUAGE = "language";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String TAG_URL = "url";
	}

	/**
	 * Series for Page Load Requests.
	 *
	 * @author Jonas Kunz
	 *
	 */
	interface EumPageLoad extends EUMBasicRequestSeries {
		/**
		 * Series name.
		 */
		String NAME = "eum_browser_pageload";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_NAVIGATION_START = "navigationStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_CONNECT_END = "connectEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_SECURE_CONNECT_START = "secureConnectStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_CONNECT_START = "connectStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOM_CONTENT_LOADED_EVENT_START = "domContentLoadedEventStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 *
		 */
		String FIELD_DOM_CONTENT_LOADED_EVENT_END = "domContentLoadedEventEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOM_INTERACTIVE = "domInteractive";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOM_COMPLETE = "domComplete";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOM_LOADING = "domLoading";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOMAIN_LOOKUP_START = "domainLookupStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_DOMAIN_LOOKUP_END = "domainLookupEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_FETCH_START = "fetchStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_LOAD_EVENT_START = "loadEventStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_LOAD_EVENT_END = "loadEventEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_REDIRECT_START = "redirectStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_REDIRECT_END = "redirectEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_REQUEST_START = "requestStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_RESPONSE_START = "responseStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_RESPONSE_END = "responseEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_UNLOAD_EVENT_START = "unloadEventStart";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_UNLOAD_EVENT_END = "unloadEventEnd";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_FIRSTPAINT = "firstpaint";

		/**
		 * See the corresponding field in {@link PageLoadRequest}.
		 */
		String FIELD_SPEEDINDEX = "speedindex";

		/**
		 * The total number of resources.
		 */
		String FIELD_RESOURCE_COUNT = "resourceCount";

	}

	/**
	 * Series for Ajax Requests.
	 *
	 * @author Jonas Kunz
	 *
	 */
	interface EumAjax extends EUMBasicRequestSeries {
		/**
		 * Series name.
		 */
		String NAME = "eum_browser_ajax";

		/**
		 * See the corresponding field in {@link AjaxRequest}.
		 */
		String TAG_BASE_URL = "baseurl";

		/**
		 * See the corresponding field in {@link AjaxRequest}.
		 */
		String FIELD_STATUS = "status";

		/**
		 * See the corresponding field in {@link AjaxRequest}.
		 */
		String FIELD_METHOD = "method";

		/**
		 * The duration the ajax request took.
		 */
		String FIELD_DURATION = "duration";
	}

	/**
	 * Series for Resource Load Requests.
	 *
	 * @author Jonas Kunz
	 **/
	interface EumResourceLoad extends EUMBasicRequestSeries {
		/**
		 * Series name.
		 */
		String NAME = "eum_browser_resourceload";

		/**
		 * See the corresponding field in {@link ResourceLoadRequest}.
		 */
		String TAG_INITIATOR_URL = "initiatorUrl";

		/**
		 * See the corresponding field in {@link ResourceLoadRequest}.
		 */
		String TAG_INITIATOR_TYPE = "initiatorType";

		/**
		 * See the corresponding field in {@link ResourceLoadRequest}.
		 */
		String FIELD_TRANSFER_SIZE = "size";

		/**
		 * The duration the request took.
		 */
		String FIELD_DURATION = "duration";

	}
}
