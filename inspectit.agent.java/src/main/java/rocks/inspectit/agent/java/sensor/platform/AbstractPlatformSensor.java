package rocks.inspectit.agent.java.sensor.platform;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Abstract class for all {@link IPlatformSensor}s to properly initialize after Spring has set all
 * the properties.
 *
 * @author Ivan Senic
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public abstract class AbstractPlatformSensor implements IPlatformSensor, InitializingBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Configuration storage for initializing the sensor and registering with the config.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * Sensor type configuration used.
	 */
	private PlatformSensorTypeConfig sensorTypeConfig;

	/**
	 * The Platform manager used to get the correct IDs.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Get the corresponding {@link SystemSensorData} of the {@link IPlatformSensor}. Each sensor
	 * has its own data collector where collected data is put and on the first run each data
	 * collector has to be initialized. Therefore the method is used to get specific data collector
	 * of each sensor for the initialization.
	 *
	 * @return the corresponding {@link PlatformSensorData}.
	 *
	 * @see {@link AbstractPlatformSensor#initSensorData()} for sensor data initialization.
	 */
	protected abstract SystemSensorData getSystemSensorData();

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		for (PlatformSensorTypeConfig config : configurationStorage.getPlatformSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.init(config);
				break;
			}
		}
	}

	/**
	 * Initializes the {@link IPlatformSensor}.
	 *
	 * @param sensorTypeConfig
	 *            initialization configuration.
	 */
	private void init(PlatformSensorTypeConfig sensorTypeConfig) {
		this.sensorTypeConfig = sensorTypeConfig;
		this.initSensorData();
	}

	/**
	 * Initializes the {@link SystemSensorData}.
	 */
	private void initSensorData() {
		try {
			SystemSensorData systemSensorData = this.getSystemSensorData();
			systemSensorData.setPlatformIdent(this.platformManager.getPlatformId());
			systemSensorData.setSensorTypeIdent(sensorTypeConfig.getId());
		} catch (IdNotAvailableException e) {
			if (log.isDebugEnabled()) {
				log.debug("Could not save the " + this.getClass().getCanonicalName() + " because of an unavailable id. " + e.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}
}
