package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;

/**
 * This class provides static information about heap memory/operating system/runtime through
 * MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class SystemInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private SystemInformationData systemInformationData = new SystemInformationData();

	/**
	 * The {@link OperatingSystemInfoProvider} used to retrieve information from the operating
	 * system.
	 */
	private OperatingSystemInfoProvider osBean;

	/**
	 * The {@link MemoryInfoProvider} used to retrieve heap memory information.
	 */
	private MemoryInfoProvider memoryBean;

	/**
	 * The {@link RuntimeInfoProvider} used to retrieve information from the runtime VM.
	 */
	private RuntimeInfoProvider runtimeBean;

	/** The switch for gathering the system information data once. */
	private boolean isGathered = false;

	/** The switch for returning the system information data once. */
	private boolean isDelivered = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void gather() {
		if (!isGathered) {
			Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
			this.systemInformationData.setTimeStamp(timestamp);

			this.systemInformationData.setTotalPhysMemory(this.getOsBean().getTotalPhysicalMemorySize());
			this.systemInformationData.setTotalSwapSpace(this.getOsBean().getTotalSwapSpaceSize());

			this.systemInformationData.setAvailableProcessors(this.getOsBean().getAvailableProcessors());

			this.systemInformationData.setArchitecture(this.getOsBean().getArch());

			this.systemInformationData.setOsName(this.getOsBean().getName());
			this.systemInformationData.setOsVersion(this.getOsBean().getVersion());

			this.systemInformationData.setJitCompilerName(this.getRuntimeBean().getJitCompilerName());
			this.systemInformationData.setClassPath(this.getRuntimeBean().getClassPath());
			this.systemInformationData.setBootClassPath(this.getRuntimeBean().getBootClassPath());
			this.systemInformationData.setLibraryPath(this.getRuntimeBean().getLibraryPath());

			this.systemInformationData.setVmVendor(this.getRuntimeBean().getVmVendor());
			this.systemInformationData.setVmVersion(this.getRuntimeBean().getVmVersion());
			this.systemInformationData.setVmName(this.getRuntimeBean().getVmName());
			this.systemInformationData.setVmSpecName(this.getRuntimeBean().getSpecName());

			this.systemInformationData.setInitHeapMemorySize(this.getMemoryBean().getHeapMemoryUsage().getInit());
			this.systemInformationData.setMaxHeapMemorySize(this.getMemoryBean().getHeapMemoryUsage().getMax());

			this.systemInformationData.setInitNonHeapMemorySize(this.getMemoryBean().getNonHeapMemoryUsage().getInit());
			this.systemInformationData.setMaxNonHeapMemorySize(this.getMemoryBean().getNonHeapMemoryUsage().getMax());

			Properties properties = System.getProperties();
			for (Map.Entry<Object, Object> property : properties.entrySet()) {
				this.systemInformationData.addVMArguments((String) property.getKey(), (String) property.getValue());
			}

			this.isGathered = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SystemSensorData get() {
		if (!isDelivered) {
			isDelivered = true;
			return systemInformationData;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		// no need to reset data. It is delivered only once.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
		return this.systemInformationData;
	}

	/**
	 * Gets the {@link OperatingSystemInfoProvider}. The getter method is provided for better
	 * testability.
	 *
	 * @return {@link OperatingSystemInfoProvider}.
	 */
	private OperatingSystemInfoProvider getOsBean() {
		if (this.osBean == null) {
			this.osBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getOperatingSystemInfoProvider();
		}
		return this.osBean;
	}

	/**
	 * Gets the {@link MemoryInfoProvider}. The getter method is provided for better testability.
	 *
	 * @return {@link MemoryInfoProvider}.
	 */
	private MemoryInfoProvider getMemoryBean() {
		if (this.memoryBean == null) {
			this.memoryBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getMemoryInfoProvider();
		}
		return this.memoryBean;
	}

	/**
	 * Gets the {@link RuntimeInfoProvider}. The getter method is provided for better testability.
	 *
	 * @return {@link RuntimeInfoProvider}.
	 */
	private RuntimeInfoProvider getRuntimeBean() {
		if (this.runtimeBean == null) {
			this.runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();
		}
		return this.runtimeBean;
	}
}
