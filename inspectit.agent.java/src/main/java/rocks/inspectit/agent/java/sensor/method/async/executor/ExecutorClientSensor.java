package rocks.inspectit.agent.java.sensor.method.async.executor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;

/**
 * The executor client sensor which initializes and returns the {@link ExecutorClientHook} class.
 *
 * @author Marius Oehler
 *
 */
public class ExecutorClientSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private ExecutorClientHook hook;

	/**
	 * Listener for firing async spans.
	 */
	@Autowired
	private IAsyncSpanContextListener asyncSpanContextListener;

	/**
	 * The tracer.
	 */
	@Autowired
	private TracerImpl tracer;

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
		hook = new ExecutorClientHook(asyncSpanContextListener, tracer);
	}
}
