package rocks.inspectit.agent.java.sensor.platform;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class provides dynamic information about the underlying operating system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class CpuInformation extends AbstractPlatformSensor implements IPlatformSensor {

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
	 * The {@link OperatingSystemInfoProvider} used to retrieve information from the operating
	 * system.
	 */
	private final OperatingSystemInfoProvider osBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getOperatingSystemInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public CpuInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public CpuInformation(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * Returns the process cpu time.
	 *
	 * @return the process cpu time.
	 */
	public long getProcessCpuTime() {
		return osBean.getProcessCpuTime();
	}

	/**
	 * Updates all dynamic cpu information.
	 *
	 * @param coreService
	 *            The {@link ICoreService}.
	 */
	public void update(ICoreService coreService) {
		long sensorTypeIdent = getSensorTypeConfig().getId();
		long processCpuTime = this.getProcessCpuTime();
		float cpuUsage = osBean.retrieveCpuUsage();

		CpuInformationData osData = (CpuInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (osData == null) {
			try {
				long platformId = platformManager.getPlatformId();
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				osData = new CpuInformationData(timestamp, platformId, sensorTypeIdent);
				osData.incrementCount();

				osData.updateProcessCpuTime(processCpuTime);

				osData.addCpuUsage(cpuUsage);
				osData.setMinCpuUsage(cpuUsage);
				osData.setMaxCpuUsage(cpuUsage);

				coreService.addPlatformSensorData(sensorTypeIdent, osData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the cpu information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			osData.incrementCount();
			osData.updateProcessCpuTime(processCpuTime);
			osData.addCpuUsage(cpuUsage);

			if (cpuUsage < osData.getMinCpuUsage()) {
				osData.setMinCpuUsage(cpuUsage);
			} else if (cpuUsage > osData.getMaxCpuUsage()) {
				osData.setMaxCpuUsage(cpuUsage);
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
