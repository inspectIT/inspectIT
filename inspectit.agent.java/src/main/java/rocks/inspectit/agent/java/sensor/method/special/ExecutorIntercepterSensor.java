package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Map;
import java.util.concurrent.Executor;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.tracing.core.async.executor.SpanStoreRunnable;

/**
 * Sensor that intercepts {@link Executor#execute(Runnable)} to substitute {@link Runnable} objects
 * by objects of type {@link SpanStoreRunnable}.
 *
 * @author Marius Oehler
 *
 */
public class ExecutorIntercepterSensor extends AbstractMethodSensor {

	/**
	 * Hook to use.
	 */
	ISpecialHook hook;

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
		hook = new ExecutorIntercepterHook();
	}
}
