package info.novatec.inspectit.agent.sensor.method;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for all {@link IMethodSensor}s to properly initialize after Spring has set all the
 * properties.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractMethodSensor implements InitializingBean, IMethodSensor {

	/**
	 * Configuration storage for initializing the sensor and registering with the config.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		for (MethodSensorTypeConfig config : configurationStorage.getMethodSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.init(config.getParameters());
				config.setSensorType(this);
				break;
			}
		}
	}

}
