package rocks.inspectit.server.processor.tsdb.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/***

@author Alexander Wert
 *
 */
public class SystemDataToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_JVM_DATA = "jvmData";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Architecture field.
	 */
	private static final String FIELD_ARCHITECTURE = "architecture";

	/**
	 * Number of available processors field.
	 */
	private static final String FIELD_NUM_AVAILABLE_PROCESSORS = "numAvailableProcessors";

	/**
	 * Bott class path field.
	 */
	private static final String FIELD_BOOT_CLASS_PATH = "bootClassPath";

	/**
	 * Class path field.
	 */
	private static final String FIELD_CLASS_PATH = "classPath";

	/**
	 * Initial heap memory size field.
	 */
	private static final String FIELD_INIT_HEAP_MEMORY_SIZE = "initHeapMemprySize";

	/**
	 * Initial non-heap memory size field.
	 */
	private static final String FIELD_INIT_NON_HEAP_MEMORY_SIZE = "initNonHeapMemprySize";

	/**
	 * JIT compiler name field.
	 */
	private static final String FIELD_JIT_COMPILER_NAME = "jitCompilerName";

	/**
	 * Library path field.
	 */
	private static final String FIELD_LIBRARY_PATH = "libraryPath";

	/**
	 * Maximum heap memory size field.
	 */
	private static final String FIELD_MAX_HEAP_SIZE = "maxHeapMemorySize";

	/**
	 * Maximum non-heap memory size field.
	 */
	private static final String FIELD_MAX_NON_HEAP_SIZE = "maxNonHeapMemorySize";

	/**
	 * OS name field.
	 */
	private static final String FIELD_OS_NAME = "osName";

	/**
	 * OS version field.
	 */
	private static final String FIELD_OS_VERSION = "osVersion";

	/**
	 * Total physical memory field.
	 */
	private static final String FIELD_TOTAL_PHYS_MEMORY = "totalPhysicalMemory";

	/**
	 * Total swap space field.
	 */
	private static final String FIELD_TOTAL_SWAP_SPACE = "totalSwapSpace";

	/**
	 * VM name field.
	 */
	private static final String FIELD_VM_NAME = "vmName";

	/**
	 * CM SPEC name field.
	 */
	private static final String FIELD_VM_SPEC_NAME = "vmSpecName";

	/**
	 * VM vendor field.
	 */
	private static final String FIELD_VM_VENDOR = "vmVendor";

	/**
	 * VM version field.
	 */
	private static final String FIELD_VM_VERSION = "vmVersion";

	/**
	 * VM attributes field.
	 */
	private static final String FIELD_VM_ATTRIBUTES = "vmAttributes";


	/**
	 * {@link IInfluxDBService} used to write data to an influx database.
	 */
	@Autowired
	IInfluxDBService influxDbService;

	/**
	 * {@link IGlobalDataAccessService} used to retrieve the agent information.
	 */
	@Autowired
	IGlobalDataAccessService globalDataAccessService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		SystemInformationData data = (SystemInformationData) defaultData;

		String vmAttributes = "";
		boolean first = true;
		for (VmArgumentData vmArgument : data.getVmSet()) {
			if (first) {
				first = false;
			} else {
				vmAttributes += System.getProperty("line.separator");
			}
			vmAttributes += vmArgument.getVmName() + "=" + vmArgument.getVmValue();
		}
		// TODO: VM properties

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}


		// measurement
		Builder builder = Point.measurement(MEASUREMENT_JVM_DATA);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);

		// fields
		builder.addField(FIELD_ARCHITECTURE, data.getArchitecture());
		builder.addField(FIELD_NUM_AVAILABLE_PROCESSORS, data.getAvailableProcessors());
		builder.addField(FIELD_BOOT_CLASS_PATH, data.getBootClassPath());
		builder.addField(FIELD_CLASS_PATH, data.getClassPath());
		builder.addField(FIELD_INIT_HEAP_MEMORY_SIZE, data.getInitHeapMemorySize());
		builder.addField(FIELD_INIT_NON_HEAP_MEMORY_SIZE, data.getInitNonHeapMemorySize());
		builder.addField(FIELD_JIT_COMPILER_NAME, data.getJitCompilerName());
		builder.addField(FIELD_LIBRARY_PATH, data.getLibraryPath());
		builder.addField(FIELD_MAX_HEAP_SIZE, data.getMaxHeapMemorySize());
		builder.addField(FIELD_MAX_NON_HEAP_SIZE, data.getMaxNonHeapMemorySize());
		builder.addField(FIELD_OS_NAME, data.getOsName());
		builder.addField(FIELD_OS_VERSION, data.getOsVersion());
		builder.addField(FIELD_TOTAL_PHYS_MEMORY, data.getTotalPhysMemory());
		builder.addField(FIELD_TOTAL_SWAP_SPACE, data.getTotalSwapSpace());
		builder.addField(FIELD_VM_NAME, data.getVmName());
		builder.addField(FIELD_VM_SPEC_NAME, data.getVmSpecName());
		builder.addField(FIELD_VM_VENDOR, data.getVmVendor());
		builder.addField(FIELD_VM_VERSION, data.getVmVersion());
		builder.addField(FIELD_VM_ATTRIBUTES, vmAttributes);

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof SystemInformationData);
	}

}
