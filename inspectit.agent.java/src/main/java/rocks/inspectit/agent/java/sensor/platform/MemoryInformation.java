package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.Calendar;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;

/**
 * This class provides dynamic information about the memory system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class MemoryInformation extends AbstractPlatformSensor {

	/** Collector class. */
	private MemoryInformationData memoryInformationData = new MemoryInformationData();

	/** The {@link MemoryInfoProvider} used to retrieve heap memory information. */
	private MemoryInfoProvider memoryBean;

	/** The {@link OperatingSystemInfoProvider} used to retrieve physical memory information. */
	private OperatingSystemInfoProvider osBean;

	/**
	 * {@inheritDoc}
	 */
	public void gather() {

		// The timestamp is set in the {@link MemoryInformation#reset()} to avoid multiple renewal.
		// It will not be set on the first execution of {@link MemoryInformation#gather()}, but
		// shortly before.
		long freePhysMemory = this.getOsBean().getFreePhysicalMemorySize();
		long freeSwapSpace = this.getOsBean().getFreeSwapSpaceSize();
		long comittedVirtualMemSize = this.getOsBean().getCommittedVirtualMemorySize();
		long usedHeapMemorySize = this.getMemoryBean().getHeapMemoryUsage().getUsed();
		long comittedHeapMemorySize = this.getMemoryBean().getHeapMemoryUsage().getCommitted();
		long usedNonHeapMemorySize = this.getMemoryBean().getNonHeapMemoryUsage().getUsed();
		long comittedNonHeapMemorySize = this.getMemoryBean().getNonHeapMemoryUsage().getCommitted();

		this.memoryInformationData.incrementCount();
		this.memoryInformationData.addFreePhysMemory(freePhysMemory);
		this.memoryInformationData.addFreeSwapSpace(freeSwapSpace);
		this.memoryInformationData.addComittedVirtualMemSize(comittedVirtualMemSize);
		this.memoryInformationData.addUsedHeapMemorySize(usedHeapMemorySize);
		this.memoryInformationData.addComittedHeapMemorySize(comittedHeapMemorySize);
		this.memoryInformationData.addUsedNonHeapMemorySize(usedNonHeapMemorySize);
		this.memoryInformationData.addComittedNonHeapMemorySize(comittedNonHeapMemorySize);

		if (freePhysMemory < this.memoryInformationData.getMinFreePhysMemory()) {
			this.memoryInformationData.setMinFreePhysMemory(freePhysMemory);
		} else if (freePhysMemory > this.memoryInformationData.getMaxFreePhysMemory()) {
			this.memoryInformationData.setMaxFreePhysMemory(freePhysMemory);
		}

		if (freeSwapSpace < this.memoryInformationData.getMinFreeSwapSpace()) {
			this.memoryInformationData.setMinFreeSwapSpace(freeSwapSpace);
		} else if (freeSwapSpace > this.memoryInformationData.getMaxFreeSwapSpace()) {
			this.memoryInformationData.setMaxFreeSwapSpace(freeSwapSpace);
		}

		if (comittedVirtualMemSize < this.memoryInformationData.getMinComittedVirtualMemSize()) {
			this.memoryInformationData.setMinComittedVirtualMemSize(comittedVirtualMemSize);
		} else if (comittedVirtualMemSize > this.memoryInformationData.getMaxComittedVirtualMemSize()) {
			this.memoryInformationData.setMaxComittedVirtualMemSize(comittedVirtualMemSize);
		}

		if (usedHeapMemorySize < this.memoryInformationData.getMinUsedHeapMemorySize()) {
			this.memoryInformationData.setMinUsedHeapMemorySize(usedHeapMemorySize);
		} else if (usedHeapMemorySize > this.memoryInformationData.getMaxUsedHeapMemorySize()) {
			this.memoryInformationData.setMaxUsedHeapMemorySize(usedHeapMemorySize);
		}

		if (comittedHeapMemorySize < this.memoryInformationData.getMinComittedHeapMemorySize()) {
			this.memoryInformationData.setMinComittedHeapMemorySize(comittedHeapMemorySize);
		} else if (comittedHeapMemorySize > this.memoryInformationData.getMaxComittedHeapMemorySize()) {
			this.memoryInformationData.setMaxComittedHeapMemorySize(comittedHeapMemorySize);
		}

		if (usedNonHeapMemorySize < this.memoryInformationData.getMinUsedNonHeapMemorySize()) {
			this.memoryInformationData.setMinUsedNonHeapMemorySize(usedNonHeapMemorySize);
		} else if (usedNonHeapMemorySize > this.memoryInformationData.getMaxUsedNonHeapMemorySize()) {
			this.memoryInformationData.setMaxUsedNonHeapMemorySize(usedNonHeapMemorySize);
		}

		if (comittedNonHeapMemorySize < this.memoryInformationData.getMinComittedNonHeapMemorySize()) {
			this.memoryInformationData.setMinComittedNonHeapMemorySize(comittedNonHeapMemorySize);
		} else if (comittedNonHeapMemorySize > this.memoryInformationData.getMaxComittedNonHeapMemorySize()) {
			this.memoryInformationData.setMaxComittedNonHeapMemorySize(comittedNonHeapMemorySize);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData get() {
		MemoryInformationData newMemoryInformationData = new MemoryInformationData();

		newMemoryInformationData.setPlatformIdent(this.memoryInformationData.getPlatformIdent());
		newMemoryInformationData.setSensorTypeIdent(this.memoryInformationData.getSensorTypeIdent());
		newMemoryInformationData.setCount(this.memoryInformationData.getCount());
		newMemoryInformationData.setTotalFreePhysMemory(this.memoryInformationData.getTotalFreePhysMemory());
		newMemoryInformationData.setMinFreePhysMemory(this.memoryInformationData.getMinFreePhysMemory());
		newMemoryInformationData.setMaxFreePhysMemory(this.memoryInformationData.getMaxFreePhysMemory());
		newMemoryInformationData.setTotalFreeSwapSpace(this.memoryInformationData.getTotalFreeSwapSpace());
		newMemoryInformationData.setMinFreeSwapSpace(this.memoryInformationData.getMinFreeSwapSpace());
		newMemoryInformationData.setMaxFreeSwapSpace(this.memoryInformationData.getMaxFreeSwapSpace());
		newMemoryInformationData.setTotalComittedVirtualMemSize(this.memoryInformationData.getTotalComittedVirtualMemSize());
		newMemoryInformationData.setMinComittedVirtualMemSize(this.memoryInformationData.getMinComittedVirtualMemSize());
		newMemoryInformationData.setMaxComittedVirtualMemSize(this.memoryInformationData.getMaxComittedVirtualMemSize());
		newMemoryInformationData.setTotalUsedHeapMemorySize(this.memoryInformationData.getTotalUsedHeapMemorySize());
		newMemoryInformationData.setMinUsedHeapMemorySize(this.memoryInformationData.getMinUsedHeapMemorySize());
		newMemoryInformationData.setMaxUsedHeapMemorySize(this.memoryInformationData.getMaxUsedHeapMemorySize());
		newMemoryInformationData.setTotalComittedHeapMemorySize(this.memoryInformationData.getTotalComittedHeapMemorySize());
		newMemoryInformationData.setMinComittedHeapMemorySize(this.memoryInformationData.getMinComittedHeapMemorySize());
		newMemoryInformationData.setMaxComittedHeapMemorySize(this.memoryInformationData.getMaxComittedHeapMemorySize());
		newMemoryInformationData.setTotalUsedNonHeapMemorySize(this.memoryInformationData.getTotalUsedNonHeapMemorySize());
		newMemoryInformationData.setMinComittedNonHeapMemorySize(this.memoryInformationData.getMinComittedNonHeapMemorySize());
		newMemoryInformationData.setMaxComittedNonHeapMemorySize(this.memoryInformationData.getMaxComittedNonHeapMemorySize());
		newMemoryInformationData.setTotalComittedNonHeapMemorySize(this.memoryInformationData.getTotalComittedNonHeapMemorySize());
		newMemoryInformationData.setTimeStamp(this.memoryInformationData.getTimeStamp());

		return newMemoryInformationData;
	}

	/**
	 * {@inheritDoc}
	 */
	public void reset() {
		this.memoryInformationData.setCount(0);

		this.memoryInformationData.setTotalFreePhysMemory(0L);
		this.memoryInformationData.setMinFreePhysMemory(Long.MAX_VALUE);
		this.memoryInformationData.setMaxFreePhysMemory(0L);

		this.memoryInformationData.setTotalFreeSwapSpace(0L);
		this.memoryInformationData.setMinFreeSwapSpace(Long.MAX_VALUE);
		this.memoryInformationData.setMaxFreeSwapSpace(0L);

		this.memoryInformationData.setTotalComittedVirtualMemSize(0L);
		this.memoryInformationData.setMinComittedVirtualMemSize(Long.MAX_VALUE);
		this.memoryInformationData.setMaxComittedVirtualMemSize(0L);

		this.memoryInformationData.setTotalUsedHeapMemorySize(0L);
		this.memoryInformationData.setMinUsedHeapMemorySize(Long.MAX_VALUE);
		this.memoryInformationData.setMaxUsedHeapMemorySize(0L);

		this.memoryInformationData.setTotalComittedHeapMemorySize(0L);
		this.memoryInformationData.setMinComittedHeapMemorySize(Long.MAX_VALUE);
		this.memoryInformationData.setMaxComittedHeapMemorySize(0L);

		this.memoryInformationData.setTotalUsedNonHeapMemorySize(0L);
		this.memoryInformationData.setMinComittedNonHeapMemorySize(Long.MAX_VALUE);
		this.memoryInformationData.setMaxComittedNonHeapMemorySize(0L);

		this.memoryInformationData.setTotalComittedNonHeapMemorySize(0L);

		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.memoryInformationData.setTimeStamp(timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SystemSensorData getSystemSensorData() {
		return this.memoryInformationData;
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
}
