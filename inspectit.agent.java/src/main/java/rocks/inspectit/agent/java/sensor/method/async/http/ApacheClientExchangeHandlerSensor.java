package rocks.inspectit.agent.java.sensor.method.async.http;

import java.util.Map;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * Sensor for the <code>org.apache.http.impl.nio.client.AbstractClientExchangeHandler</code> of the
 * Apache asynchronous HTTP client.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class ApacheClientExchangeHandlerSensor extends AbstractMethodSensor implements IMethodSensor {

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
		hook = new ApacheClientExchangeHandlerHook();
	}

}
