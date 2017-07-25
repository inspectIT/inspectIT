package rocks.inspectit.agent.java.sensor.method.special;

import java.util.concurrent.Executor;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.tracing.core.async.executor.SpanStoreRunnable;

/**
 * Hook that intercepts the {@link Executor#execute(Runnable)} method to substidude the given
 * {@link Runnable} by {@link SpanStoreRunnable} objects.
 *
 * @author Marius Oehler
 *
 */
public class ExecutorIntercepterHook implements ISpecialHook {

	/**
	 * The tracer.
	 */
	private TracerImpl tracer;

	/**
	 * Constructor.
	 *
	 * @param tracer
	 *            the trader to use
	 */
	public ExecutorIntercepterHook(TracerImpl tracer) {
		this.tracer = tracer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		if ((parameters[0] instanceof SpanStore) || !tracer.isCurrentContextExisting()) {
			return null;
		}

		if (parameters[0] instanceof Runnable) {
			Runnable runnable = (Runnable) parameters[0];
			SpanStoreRunnable spanStoreRunnable = new SpanStoreRunnable(runnable);

			parameters[0] = spanStoreRunnable;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}
}
