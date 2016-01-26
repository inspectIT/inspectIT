package rocks.inspectit.agent.java.sensor.method;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.shared.all.instrumentation.config.impl.AbstractSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;

/**
 * Abstract class for all {@link IMethodSensor}s to properly initialize after Spring has set all the
 * properties.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractMethodSensor implements IMethodSensor, InitializingBean {

	/**
	 * Configuration storage for initializing the sensor and registering with the config.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * Sensor type configuration used.
	 */
	private MethodSensorTypeConfig sensorTypeConfig;

	/**
	 * Called when hook should be initialized.
	 *
	 * @param parameters
	 *            Parameters passed via the {@link AbstractSensorTypeConfig}.
	 */
	protected abstract void initHook(Map<String, Object> parameters);

	/**
	 * {@inheritDoc}
	 */
	public void init(MethodSensorTypeConfig sensorTypeConfig) {
		this.sensorTypeConfig = sensorTypeConfig;

		initHook(sensorTypeConfig.getParameters());
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		for (MethodSensorTypeConfig config : configurationStorage.getMethodSensorTypes()) {
			if (config.getClassName().equals(this.getClass().getName())) {
				this.init(config);
				break;
			}
		}
	}

}
