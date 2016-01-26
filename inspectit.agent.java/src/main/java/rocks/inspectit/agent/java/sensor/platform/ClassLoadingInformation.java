package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class provides dynamic information about the class loading system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class ClassLoadingInformation extends AbstractPlatformSensor implements IPlatformSensor {

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
	 * The {@link RuntimeInfoProvider} used to retrieve information from the class loading system.
	 */
	private final RuntimeInfoProvider runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public ClassLoadingInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public ClassLoadingInformation(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * Returns the number of loaded classes in the virtual machine.
	 *
	 * @return The number of loaded classes.
	 */
	public int getLoadedClassCount() {
		return runtimeBean.getLoadedClassCount();
	}

	/**
	 * Returns the total number of loaded classes since the virtual machine started.
	 *
	 * @return The total number of loaded classes.
	 */
	public long getTotalLoadedClassCount() {
		return runtimeBean.getTotalLoadedClassCount();
	}

	/**
	 * Returns the number of unloaded classes since the virtual machine started.
	 *
	 * @return The number of unloaded classes.
	 */
	public long getUnloadedClassCount() {
		return runtimeBean.getUnloadedClassCount();
	}

	/**
	 * Updates all dynamic class loading information.
	 *
	 * @param coreService
	 *            The {@link ICoreService}.
	 */
	public void update(ICoreService coreService) {
		long sensorTypeIdent = getSensorTypeConfig().getId();
		int loadedClassCount = this.getLoadedClassCount();
		long totalLoadedClassCount = this.getTotalLoadedClassCount();
		long unloadedClassCount = this.getUnloadedClassCount();

		ClassLoadingInformationData classLoadingData = (ClassLoadingInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (classLoadingData == null) {
			try {
				long platformId = platformManager.getPlatformId();
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				classLoadingData = new ClassLoadingInformationData(timestamp, platformId, sensorTypeIdent);
				classLoadingData.incrementCount();

				classLoadingData.addLoadedClassCount(loadedClassCount);
				classLoadingData.setMinLoadedClassCount(loadedClassCount);
				classLoadingData.setMaxLoadedClassCount(loadedClassCount);

				classLoadingData.addTotalLoadedClassCount(totalLoadedClassCount);
				classLoadingData.setMinTotalLoadedClassCount(totalLoadedClassCount);
				classLoadingData.setMaxTotalLoadedClassCount(totalLoadedClassCount);

				classLoadingData.addUnloadedClassCount(unloadedClassCount);
				classLoadingData.setMinUnloadedClassCount(unloadedClassCount);
				classLoadingData.setMaxUnloadedClassCount(unloadedClassCount);

				coreService.addPlatformSensorData(sensorTypeIdent, classLoadingData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the class loading information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			classLoadingData.incrementCount();
			classLoadingData.addLoadedClassCount(loadedClassCount);
			classLoadingData.addTotalLoadedClassCount(totalLoadedClassCount);
			classLoadingData.addUnloadedClassCount(unloadedClassCount);

			if (loadedClassCount < classLoadingData.getMinLoadedClassCount()) {
				classLoadingData.setMinLoadedClassCount(loadedClassCount);
			} else if (loadedClassCount > classLoadingData.getMaxLoadedClassCount()) {
				classLoadingData.setMaxLoadedClassCount(loadedClassCount);
			}

			if (totalLoadedClassCount < classLoadingData.getMinTotalLoadedClassCount()) {
				classLoadingData.setMinTotalLoadedClassCount(totalLoadedClassCount);
			} else if (totalLoadedClassCount > classLoadingData.getMaxTotalLoadedClassCount()) {
				classLoadingData.setMaxTotalLoadedClassCount(totalLoadedClassCount);
			}

			if (unloadedClassCount < classLoadingData.getMinUnloadedClassCount()) {
				classLoadingData.setMinUnloadedClassCount(unloadedClassCount);
			} else if (unloadedClassCount > classLoadingData.getMaxUnloadedClassCount()) {
				classLoadingData.setMaxUnloadedClassCount(unloadedClassCount);
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
