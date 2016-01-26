package info.novatec.inspectit.agent.sensor.method;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.hooking.impl.HookSupplier;
import info.novatec.inspectit.instrumentation.config.impl.AbstractSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * {@link HookSupplier} for reporiting the sensor creation.
	 */
	@Autowired
	protected HookSupplier hookSupplier;

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

		hookSupplier.sensorInitialized(this);
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
