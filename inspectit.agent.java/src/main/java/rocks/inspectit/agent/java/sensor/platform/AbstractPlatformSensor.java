package rocks.inspectit.agent.java.sensor.platform;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;

/**
 * Abstract class for all {@link IPlatformSensor}s to properly initialize after Spring has set all
 * the properties.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractPlatformSensor implements IPlatformSensor, InitializingBean {

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
	 * {@inheritDoc}
	 */
	public void init(PlatformSensorTypeConfig sensorTypeConfig) {
		this.sensorTypeConfig = sensorTypeConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}

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

}
