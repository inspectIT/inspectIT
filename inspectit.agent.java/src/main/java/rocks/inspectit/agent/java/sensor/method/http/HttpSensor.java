package rocks.inspectit.agent.java.sensor.method.http;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

/**
 * The http sensor which initializes and returns the {@link HttpHook} class.
 *
 * @author Stefan Siegl
 */
public class HttpSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private HttpHook hook = null;

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
	 * No-arg constructor needed for Spring.
	 */
	public HttpSensor() {
	}

	/**
	 * Constructor.
	 *
	 * @param timer
	 *            the timer.
	 * @param platformManager
	 *            the platform manager.
	 */
	public HttpSensor(Timer timer, IPlatformManager platformManager) {
		this.timer = timer;
		this.platformManager = platformManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		hook = new HttpHook(timer, platformManager, parameters, ManagementFactory.getThreadMXBean());
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}
}