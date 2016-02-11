package rocks.inspectit.agent.java.sensor.method.invocationsequence;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.core.IIdManager;
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
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

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
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param configurationStorage
	 *            {@link IConfigurationStorage}.
	 */
	public InvocationSequenceSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, IConfigurationStorage configurationStorage) {
		this.timer = timer;
		this.idManager = idManager;
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
	public void init(Map<String, Object> parameter) {
		invocationSequenceHook = new InvocationSequenceHook(timer, idManager, propertyAccessor, parameter, configurationStorage.isEnhancedExceptionSensorActivated());
	}

}
