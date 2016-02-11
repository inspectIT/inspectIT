package info.novatec.inspectit.agent.sensor.method.averagetimer;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The average timer sensor which initializes and returns the {@link AverageTimerHook} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AverageTimerSensor extends AbstractMethodSensor implements IMethodSensor {

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
	 * The used average timer hook.
	 */
	private AverageTimerHook averageTimerHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public AverageTimerSensor() {
	}

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 * 
	 * @param timer
	 *            The timer used for accurate measuring.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public AverageTimerSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return averageTimerHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		averageTimerHook = new AverageTimerHook(timer, idManager, propertyAccessor, parameter);
	}

}
