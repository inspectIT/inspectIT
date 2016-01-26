package rocks.inspectit.agent.java.sensor.method.averagetimer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

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
	 * @param platformManager
	 *            The Platform manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 */
	public AverageTimerSensor(Timer timer, IPlatformManager platformManager, IPropertyAccessor propertyAccessor) {
		this.timer = timer;
		this.platformManager = platformManager;
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
	@Override
	protected void initHook(Map<String, Object> parameters) {
		averageTimerHook = new AverageTimerHook(timer, platformManager, propertyAccessor, parameters);
	}

}
