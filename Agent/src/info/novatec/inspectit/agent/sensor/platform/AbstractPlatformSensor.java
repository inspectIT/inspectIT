package info.novatec.inspectit.agent.sensor.platform;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		for (PlatformSensorTypeConfig config : configurationStorage.getPlatformSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.init(config.getParameters());
				config.setSensorType(this);
				break;
			}
		}
	}

}
