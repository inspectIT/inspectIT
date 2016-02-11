package info.novatec.inspectit.agent.sensor.method.invocationsequence;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
