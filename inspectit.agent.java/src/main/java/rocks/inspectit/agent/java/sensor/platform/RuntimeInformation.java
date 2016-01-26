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
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class provides dynamic information about the runtime of the Virtual Machine through MXBeans.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class RuntimeInformation extends AbstractPlatformSensor implements IPlatformSensor {

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
	 * The {@link RuntimeInfoProvider} used to retrieve information from the runtime.
	 */
	private final RuntimeInfoProvider runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public RuntimeInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public RuntimeInformation(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * Returns the uptime of the virtual machine in milliseconds.
	 *
	 * @return the uptime in milliseconds.
	 */
	public long getUptime() {
		return runtimeBean.getUptime();
	}

	/**
	 * Updates all dynamic runtime informations.
	 *
	 * @param coreService
	 *            The {@link ICoreService}.
	 */
	public void update(ICoreService coreService) {
		long sensorTypeIdent = getSensorTypeConfig().getId();
		long uptime = this.getUptime();

		RuntimeInformationData runtimeData = (RuntimeInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (runtimeData == null) {
			try {
				long platformId = platformManager.getPlatformId();
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
				runtimeData = new RuntimeInformationData(timestamp, platformId, sensorTypeIdent);

				runtimeData.incrementCount();
				runtimeData.addUptime(uptime);
				runtimeData.setMinUptime(uptime);
				runtimeData.setMaxUptime(uptime);

				coreService.addPlatformSensorData(sensorTypeIdent, runtimeData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the runtime information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {

			runtimeData.incrementCount();
			runtimeData.addUptime(uptime);

			if (uptime < runtimeData.getMinUptime()) {
				runtimeData.setMinUptime(uptime);
			} else if (uptime > runtimeData.getMaxUptime()) {
				runtimeData.setMaxUptime(uptime);
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
