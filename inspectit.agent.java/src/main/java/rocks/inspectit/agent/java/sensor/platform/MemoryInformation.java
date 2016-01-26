package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class provides dynamic information about the memory system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class MemoryInformation extends AbstractPlatformSensor implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The Platform manager used to get the correct IDs.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The {@link MemoryInfoProvider} used to retrieve heap memory information.
	 */
	private final MemoryInfoProvider memoryBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getMemoryInfoProvider();

	/**
	 * The {@link OperatingSystemInfoProvider} used to retrieve physical memory information.
	 */
	private final OperatingSystemInfoProvider osBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getOperatingSystemInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public MemoryInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public MemoryInformation(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * Returns the amount of free physical memory.
	 *
	 * @return the free physical memory.
	 */
	public long getFreePhysMemory() {
		return osBean.getFreePhysicalMemorySize();
	}

	/**
	 * Returns the amount of free swap space.
	 *
	 * @return the free swap space.
	 */
	public long getFreeSwapSpace() {
		return osBean.getFreeSwapSpaceSize();
	}

	/**
	 * Returns the amount of virtual memory that is guaranteed to be available to the running
	 * process.
	 *
	 * @return the virtual memory size.
	 */
	public long getComittedVirtualMemSize() {
		return osBean.getCommittedVirtualMemorySize();
	}

	/**
	 * Returns the memory usage of the heap that is used for object allocation.
	 *
	 * @return the memory usage of the heap for object allocation.
	 */
	public long getUsedHeapMemorySize() {
		return memoryBean.getHeapMemoryUsage().getUsed();
	}

	/**
	 * Returns the amount of memory that is guaranteed to be available for use by the virtual
	 * machine for heap memory usage.
	 *
	 * @return the amount of guaranteed to be available memory for heap memory usage.
	 */
	public long getComittedHeapMemorySize() {
		return memoryBean.getHeapMemoryUsage().getCommitted();
	}

	/**
	 * Returns the amount of memory for non-heap memory usage of the virtual machine.
	 *
	 * @return the amount of memory for non-heap memory usage.
	 */
	public long getUsedNonHeapMemorySize() {
		return memoryBean.getNonHeapMemoryUsage().getUsed();
	}

	/**
	 * Returns the amount of memory that is guaranteed to be available for use by the virtual
	 * machine for non-heap memory usage.
	 *
	 * @return the guaranteed to be available memory for non-heap memory usage.
	 */
	public long getComittedNonHeapMemoryUsage() {
		return memoryBean.getNonHeapMemoryUsage().getCommitted();
	}

	/**
	 * Updates all dynamic memory informations.
	 *
	 * @param coreService
	 *            The {@link ICoreService}.
	 */
	public void update(ICoreService coreService) {
		long sensorTypeIdent = getSensorTypeConfig().getId();
		long freePhysMemory = this.getFreePhysMemory();
		long freeSwapSpace = this.getFreeSwapSpace();
		long comittedVirtualMemSize = this.getComittedVirtualMemSize();
		long usedHeapMemorySize = this.getUsedHeapMemorySize();
		long comittedHeapMemorySize = this.getComittedHeapMemorySize();
		long usedNonHeapMemorySize = this.getUsedNonHeapMemorySize();
		long comittedNonHeapMemorySize = this.getComittedNonHeapMemoryUsage();

		MemoryInformationData memoryData = (MemoryInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (memoryData == null) {
			try {
				long platformId = platformManager.getPlatformId();
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				memoryData = new MemoryInformationData(timestamp, platformId, sensorTypeIdent);
				memoryData.incrementCount();

				memoryData.addFreePhysMemory(freePhysMemory);
				memoryData.setMinFreePhysMemory(freePhysMemory);
				memoryData.setMaxFreePhysMemory(freePhysMemory);

				memoryData.addFreeSwapSpace(freeSwapSpace);
				memoryData.setMinFreeSwapSpace(freeSwapSpace);
				memoryData.setMaxFreeSwapSpace(freeSwapSpace);

				memoryData.addComittedVirtualMemSize(comittedVirtualMemSize);
				memoryData.setMinComittedVirtualMemSize(comittedVirtualMemSize);
				memoryData.setMaxComittedVirtualMemSize(comittedVirtualMemSize);

				memoryData.addUsedHeapMemorySize(usedHeapMemorySize);
				memoryData.setMinUsedHeapMemorySize(usedHeapMemorySize);
				memoryData.setMaxUsedHeapMemorySize(usedHeapMemorySize);

				memoryData.addComittedHeapMemorySize(comittedHeapMemorySize);
				memoryData.setMinComittedHeapMemorySize(comittedHeapMemorySize);
				memoryData.setMaxComittedHeapMemorySize(comittedHeapMemorySize);

				memoryData.addUsedNonHeapMemorySize(usedNonHeapMemorySize);
				memoryData.setMinUsedNonHeapMemorySize(usedNonHeapMemorySize);
				memoryData.setMaxUsedNonHeapMemorySize(usedNonHeapMemorySize);

				memoryData.addComittedNonHeapMemorySize(comittedNonHeapMemorySize);
				memoryData.setMinComittedNonHeapMemorySize(comittedNonHeapMemorySize);
				memoryData.setMaxComittedNonHeapMemorySize(comittedNonHeapMemorySize);

				coreService.addPlatformSensorData(sensorTypeIdent, memoryData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the memory information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			memoryData.incrementCount();
			memoryData.addFreePhysMemory(freePhysMemory);
			memoryData.addFreeSwapSpace(freeSwapSpace);
			memoryData.addComittedVirtualMemSize(comittedVirtualMemSize);
			memoryData.addUsedHeapMemorySize(usedHeapMemorySize);
			memoryData.addComittedHeapMemorySize(comittedHeapMemorySize);
			memoryData.addUsedNonHeapMemorySize(usedNonHeapMemorySize);
			memoryData.addComittedNonHeapMemorySize(comittedNonHeapMemorySize);

			if (freePhysMemory < memoryData.getMinFreePhysMemory()) {
				memoryData.setMinFreePhysMemory(freePhysMemory);
			} else if (freePhysMemory > memoryData.getMaxFreePhysMemory()) {
				memoryData.setMaxFreePhysMemory(freePhysMemory);
			}

			if (freeSwapSpace < memoryData.getMinFreeSwapSpace()) {
				memoryData.setMinFreeSwapSpace(freeSwapSpace);
			} else if (freeSwapSpace > memoryData.getMaxFreeSwapSpace()) {
				memoryData.setMaxFreeSwapSpace(freeSwapSpace);
			}

			if (comittedVirtualMemSize < memoryData.getMinComittedVirtualMemSize()) {
				memoryData.setMinComittedVirtualMemSize(comittedVirtualMemSize);
			} else if (comittedVirtualMemSize > memoryData.getMaxComittedVirtualMemSize()) {
				memoryData.setMaxComittedVirtualMemSize(comittedVirtualMemSize);
			}

			if (usedHeapMemorySize < memoryData.getMinUsedHeapMemorySize()) {
				memoryData.setMinUsedHeapMemorySize(usedHeapMemorySize);
			} else if (usedHeapMemorySize > memoryData.getMaxUsedHeapMemorySize()) {
				memoryData.setMaxUsedHeapMemorySize(usedHeapMemorySize);
			}

			if (comittedHeapMemorySize < memoryData.getMinComittedHeapMemorySize()) {
				memoryData.setMinComittedHeapMemorySize(comittedHeapMemorySize);
			} else if (comittedHeapMemorySize > memoryData.getMaxComittedHeapMemorySize()) {
				memoryData.setMaxComittedHeapMemorySize(comittedHeapMemorySize);
			}

			if (usedNonHeapMemorySize < memoryData.getMinUsedNonHeapMemorySize()) {
				memoryData.setMinUsedNonHeapMemorySize(usedNonHeapMemorySize);
			} else if (usedNonHeapMemorySize > memoryData.getMaxUsedNonHeapMemorySize()) {
				memoryData.setMaxUsedNonHeapMemorySize(usedNonHeapMemorySize);
			}

			if (comittedNonHeapMemorySize < memoryData.getMinComittedNonHeapMemorySize()) {
				memoryData.setMinComittedNonHeapMemorySize(comittedNonHeapMemorySize);
			} else if (comittedNonHeapMemorySize > memoryData.getMaxComittedNonHeapMemorySize()) {
				memoryData.setMaxComittedNonHeapMemorySize(comittedNonHeapMemorySize);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return true;
	}

}
