package rocks.inspectit.agent.java.sensor.method.invocationsequence;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

/**
 * The invocation sequence sensor which initializes and returns the {@link InvocationSequenceHook}
 * class.
 *
 * @author Patrice Bouillet
 *
 */
public class InvocationSequenceSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The timer used for accurate measuring.
	 */
	@Autowired
	private Timer timer;

	/**
	 * The Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The property accessor.
	 */
	@Autowired
	private IPropertyAccessor propertyAccessor;

	/**
	 * Configuration storage for checking if enhanced exception sensor is ON.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * The invocation sequence hook.
	 */
	private InvocationSequenceHook invocationSequenceHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public InvocationSequenceSensor() {
	}

	/**
	 * The default constructor which needs 2 parameter for initialization.
	 *
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param platformManager
	 *            The Platform manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param configurationStorage
	 *            {@link IConfigurationStorage}.
	 */
	public InvocationSequenceSensor(Timer timer, IPlatformManager platformManager, IPropertyAccessor propertyAccessor, IConfigurationStorage configurationStorage) {
		this.timer = timer;
		this.platformManager = platformManager;
		this.propertyAccessor = propertyAccessor;
		this.configurationStorage = configurationStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return invocationSequenceHook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		boolean enhancedExceptionSensor;
		try {
			enhancedExceptionSensor = configurationStorage.isEnhancedExceptionSensorActivated();
		} catch (StorageException storageException) {
			enhancedExceptionSensor = false;
		}

		invocationSequenceHook = new InvocationSequenceHook(timer, platformManager, propertyAccessor, parameters, enhancedExceptionSensor);
	}

}
