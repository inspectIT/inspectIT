package rocks.inspectit.agent.java.sensor.platform;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
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

	/** The logger of the class. */
	@Log
	Logger log;

	/** Configuration storage for initializing the sensor and registering with the config. */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/** Sensor type configuration used. */
	private PlatformSensorTypeConfig sensorTypeConfig;

	/** The Platform manager used to get the correct IDs. */
	@Autowired
	private IPlatformManager platformManager;

	/** {@inheritDoc} */
	public void afterPropertiesSet() throws Exception {
		for (PlatformSensorTypeConfig config : configurationStorage.getPlatformSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.sensorTypeConfig = config;
				this.init();
				break;
			}
		}
	}

	/** Init the state of the collector class in the corresponding sensor and . */
	private void init() {
		try {
			PlatformSensorData platformSensorData = this.getPlatformSensorData();
			platformSensorData.setPlatformIdent(this.platformManager.getPlatformId());
			platformSensorData.setSensorTypeIdent(this.sensorTypeConfig.getId());
		} catch (IdNotAvailableException e) {
			if (log.isDebugEnabled()) {
				log.debug("Could not save the " + this.getClass().getCanonicalName() + " because of an unavailable id. " + e.getMessage());
			}
		}
	}

	/** {@inheritDoc} */
	public PlatformSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}

	/**
	 * Get the corresponding {@link PlatformSensorData} of the {@link IPlatformSensor}.
	 *
	 * @return the corresponding {@link PlatformSensorData}.
	 */
	protected abstract PlatformSensorData getPlatformSensorData();
}
