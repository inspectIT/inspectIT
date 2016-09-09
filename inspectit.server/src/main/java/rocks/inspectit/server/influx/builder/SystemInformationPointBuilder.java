package rocks.inspectit.server.influx.builder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;

/**
 * Point builder for the {@link SystemInformationData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class SystemInformationPointBuilder extends DefaultDataPointBuilder<SystemInformationData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SystemInformationData> getDataClass() {
		return SystemInformationData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.SystemInformation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(SystemInformationData data, Builder builder) {
		StringBuilder vmAttributes = new StringBuilder();
		boolean first = true;
		if (CollectionUtils.isNotEmpty(data.getVmSet())) {
			for (VmArgumentData vmArgument : data.getVmSet()) {
				if (first) {
					first = false;
				} else {
					vmAttributes.append(System.getProperty("line.separator"));
				}
				vmAttributes.append(vmArgument.getVmName());
				vmAttributes.append('=');
				vmAttributes.append(vmArgument.getVmValue());
			}
		}

		// fields
		builder.addField(Series.SystemInformation.FIELD_ARCHITECTURE, StringUtils.defaultString(data.getArchitecture()));
		builder.addField(Series.SystemInformation.FIELD_NUM_AVAILABLE_PROCESSORS, data.getAvailableProcessors());
		builder.addField(Series.SystemInformation.FIELD_BOOT_CLASS_PATH, StringUtils.defaultString(data.getBootClassPath()));
		builder.addField(Series.SystemInformation.FIELD_CLASS_PATH, StringUtils.defaultString(data.getClassPath()));
		builder.addField(Series.SystemInformation.FIELD_INIT_HEAP_MEMORY_SIZE, data.getInitHeapMemorySize());
		builder.addField(Series.SystemInformation.FIELD_INIT_NON_HEAP_MEMORY_SIZE, data.getInitNonHeapMemorySize());
		builder.addField(Series.SystemInformation.FIELD_JIT_COMPILER_NAME, StringUtils.defaultString(data.getJitCompilerName()));
		builder.addField(Series.SystemInformation.FIELD_LIBRARY_PATH, StringUtils.defaultString(data.getLibraryPath()));
		builder.addField(Series.SystemInformation.FIELD_MAX_HEAP_SIZE, data.getMaxHeapMemorySize());
		builder.addField(Series.SystemInformation.FIELD_MAX_NON_HEAP_SIZE, data.getMaxNonHeapMemorySize());
		builder.addField(Series.SystemInformation.FIELD_OS_NAME, StringUtils.defaultString(data.getOsName()));
		builder.addField(Series.SystemInformation.FIELD_OS_VERSION, StringUtils.defaultString(data.getOsVersion()));
		builder.addField(Series.SystemInformation.FIELD_TOTAL_PHYS_MEMORY, data.getTotalPhysMemory());
		builder.addField(Series.SystemInformation.FIELD_TOTAL_SWAP_SPACE, data.getTotalSwapSpace());
		builder.addField(Series.SystemInformation.FIELD_VM_NAME, StringUtils.defaultString(data.getVmName()));
		builder.addField(Series.SystemInformation.FIELD_VM_SPEC_NAME, StringUtils.defaultString(data.getVmSpecName()));
		builder.addField(Series.SystemInformation.FIELD_VM_VENDOR, StringUtils.defaultString(data.getVmVendor()));
		builder.addField(Series.SystemInformation.FIELD_VM_VERSION, StringUtils.defaultString(data.getVmVersion()));
		builder.addField(Series.SystemInformation.FIELD_VM_ATTRIBUTES, vmAttributes.toString());
	}

}
