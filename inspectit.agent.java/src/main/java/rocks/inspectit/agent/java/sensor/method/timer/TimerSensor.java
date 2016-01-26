package rocks.inspectit.agent.java.sensor.method.timer;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

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
	 * @param platformManager
	 *            The Platform manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public TimerSensor(Timer timer, IPlatformManager platformManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.platformManager = platformManager;
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
	@Override
	public void initHook(Map<String, Object> parameter) {
		timerHook = new TimerHook(timer, platformManager, propertyAccessor, parameter, ManagementFactory.getThreadMXBean());
	}

}
