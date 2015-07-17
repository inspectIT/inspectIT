package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.factory.PlatformSensorInfoProviderFactory;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * The ID Manager used to get the correct IDs.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The {@link RuntimeInfoProvider} used to retrieve information from the compilation system.
	 */
	private RuntimeInfoProvider runtimeBean = PlatformSensorInfoProviderFactory.getPlatformSensorInfoProvider().getRuntimeInfoProvider();

	/**
	 * No-arg constructor needed for Spring.
	 */
	public CompilationInformation() {
	}

	/**
	 * The default constructor which needs one parameter.
	 * 
	 * @param idManager
	 *            The ID Manager.
	 */
	public CompilationInformation(IIdManager idManager) {
		this.idManager = idManager;
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
	 * 
	 * @param sensorTypeIdent
	 *            The sensorTypeIdent.
	 */
	public void update(ICoreService coreService, long sensorTypeIdent) {
		long totalCompilationTime = this.getTotalCompilationTime();

		CompilationInformationData compilationData = (CompilationInformationData) coreService.getPlatformSensorData(sensorTypeIdent);

		if (compilationData == null) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeIdent);
				Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());

				compilationData = new CompilationInformationData(timestamp, platformId, registeredSensorTypeId);
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
	public void init(Map<String, Object> parameter) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean automaticUpdate() {
		return true;
	}

}
