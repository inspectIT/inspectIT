package rocks.inspectit.agent.java.sensor.method.async.http;

import java.util.Map;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * The HTTP client builder sensor.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class NHttpClientConnectionManagerSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * Hook to use.
	 */
	private IMethodHook hook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHook getHook() {
		return hook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		hook = new NHttpClientConnectionManagerHook();
	}

}
