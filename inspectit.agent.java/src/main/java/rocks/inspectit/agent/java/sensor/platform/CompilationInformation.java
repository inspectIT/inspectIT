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
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class provides dynamic information about the compilation system through MXBeans.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class CompilationInformation extends AbstractPlatformSensor implements IPlatformSensor {

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
	 * The {@link RuntimeInfoProvider} used to retrieve information from the compilation system.
	 */
	private final RuntimeInfoProvider runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public CompilationInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public CompilationInformation(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * Returns the approximate accumulated elapsed time (milliseconds) spent in compilation.
	 *
	 * @return The compilation time in milliseconds.
	 */
	public long getTotalCompilationTime() {
		return runtimeBean.getTotalCompilationTime();
	}

	/**
	 * Updates all dynamic compilation information.
	 *
	 * @param coreService
	 *            The {@link ICoreService}.
	 */
	public void update(ICoreService coreService) {
		long sensorTypeIdent = getSensorTypeConfig().getId();
		long totalCompilationTime = this.getTotalCompilationTime();

		CompilationInformationData compilationData = (CompilationInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (compilationData == null) {
			try {
				long platformId = platformManager.getPlatformId();
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				compilationData = new CompilationInformationData(timestamp, platformId, sensorTypeIdent);
				compilationData.incrementCount();

				compilationData.addTotalCompilationTime(totalCompilationTime);
				compilationData.setMinTotalCompilationTime(totalCompilationTime);
				compilationData.setMaxTotalCompilationTime(totalCompilationTime);

				coreService.addPlatformSensorData(sensorTypeIdent, compilationData);
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Could not save the compilation information because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			compilationData.incrementCount();
			compilationData.addTotalCompilationTime(totalCompilationTime);

			if (totalCompilationTime < compilationData.getMinTotalCompilationTime()) {
				compilationData.setMinTotalCompilationTime(totalCompilationTime);
			} else if (totalCompilationTime > compilationData.getMaxTotalCompilationTime()) {
				compilationData.setMaxTotalCompilationTime(totalCompilationTime);
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
