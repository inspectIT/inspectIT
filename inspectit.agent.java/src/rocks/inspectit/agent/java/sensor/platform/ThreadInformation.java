package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.provider.ThreadInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class provides dynamic information about the thread system through MXBeans.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThreadInformation extends AbstractPlatformSensor implements IPlatformSensor {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The ID Manager used to get the correct IDs.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The {@link ThreadInfoProvider} used to retrieve information from the thread system.
	 */
	private ThreadInfoProvider threadBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getThreadInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public ThreadInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public ThreadInformation(IIdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * Returns the current number of live daemon threads.
	 * 
	 * @return The daemon thread count.
	 */
	public int getDaemonThreadCount() {
		return threadBean.getDaemonThreadCount();
	}

	/**
	 * Returns the peak live thread count since the virtual machine started.
	 * 
	 * @return The peak thread count.
	 */
	public int getPeakThreadCount() {
		return threadBean.getPeakThreadCount();
	}

	/**
	 * Returns the live thread count both daemon and non-daemon threads.
	 * 
	 * @return The thread count.
	 */
	public int getThreadCount() {
		return threadBean.getThreadCount();
	}

	/**
	 * Returns the total number of created and also started threads.
	 * 
	 * @return The total started thread count.
	 */
	public long getTotalStartedThreadCount() {
		return threadBean.getTotalStartedThreadCount();
	}

	/**
	 * Updates all dynamic thread information.
	 * 
	 * @param coreService
	 *            The {@link ICoreService}.
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		int daemonThreadCount = this.getDaemonThreadCount();
		int peakThreadCount = this.getPeakThreadCount();
		int threadCount = this.getThreadCount();
		long totalStartedThreadCount = this.getTotalStartedThreadCount();

		ThreadInformationData threadData = (ThreadInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (threadData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				threadData = new ThreadInformationData(timestamp, platformId, registeredSensorTypeId);
				threadData.incrementCount();

				threadData.addDaemonThreadCount(daemonThreadCount);
				threadData.setMinDaemonThreadCount(daemonThreadCount);
				threadData.setMaxDaemonThreadCount(daemonThreadCount);

				threadData.addPeakThreadCount(peakThreadCount);
				threadData.setMinPeakThreadCount(peakThreadCount);
				threadData.setMaxPeakThreadCount(peakThreadCount);

				threadData.addThreadCount(threadCount);
				threadData.setMinThreadCount(threadCount);
				threadData.setMaxThreadCount(threadCount);

				threadData.addTotalStartedThreadCount(totalStartedThreadCount);
				threadData.setMinTotalStartedThreadCount(totalStartedThreadCount);
				threadData.setMaxTotalStartedThreadCount(totalStartedThreadCount);

				coreService.addPlatformSensorData(sensorTypeIdent, threadData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the thread information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			threadData.incrementCount();
			threadData.addDaemonThreadCount(daemonThreadCount);
			threadData.addPeakThreadCount(peakThreadCount);
			threadData.addThreadCount(threadCount);
			threadData.addTotalStartedThreadCount(totalStartedThreadCount);

			if (daemonThreadCount < threadData.getMinDaemonThreadCount()) {
				threadData.setMinDaemonThreadCount(daemonThreadCount);
			} else if (daemonThreadCount > threadData.getMaxDaemonThreadCount()) {
				threadData.setMaxDaemonThreadCount(daemonThreadCount);
			}

			if (peakThreadCount < threadData.getMinPeakThreadCount()) {
				threadData.setMinPeakThreadCount(peakThreadCount);
			} else if (peakThreadCount > threadData.getMaxPeakThreadCount()) {
				threadData.setMaxPeakThreadCount(peakThreadCount);
			}

			if (threadCount < threadData.getMinThreadCount()) {
				threadData.setMinThreadCount(threadCount);
			} else if (threadCount > threadData.getMaxThreadCount()) {
				threadData.setMaxThreadCount(threadCount);
			}

			if (totalStartedThreadCount < threadData.getMinTotalStartedThreadCount()) {
				threadData.setMinTotalStartedThreadCount(totalStartedThreadCount);
			} else if (totalStartedThreadCount > threadData.getMaxTotalStartedThreadCount()) {
				threadData.setMaxTotalStartedThreadCount(totalStartedThreadCount);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return true;
	}

}
