package rocks.inspectit.agent.java.sensor.method;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.MethodSensorTypeConfig;

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
