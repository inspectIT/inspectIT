package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The timer sensor which initializes and returns the {@link TimerHook} class.
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerSensor extends AbstractMethodSensor implements IMethodSensor {

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
	 * The used timer hook.
	 */
	private TimerHook timerHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public TimerSensor() {
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
	public TimerSensor(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return timerHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		timerHook = new TimerHook(timer, idManager, propertyAccessor, parameter, ManagementFactory.getThreadMXBean());
	}

}
