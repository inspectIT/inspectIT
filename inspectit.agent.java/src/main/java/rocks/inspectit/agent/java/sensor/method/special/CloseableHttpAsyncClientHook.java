package rocks.inspectit.agent.java.sensor.method.special;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.FutureCallbackProxy;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Hook that intercepts the execute method of the {@link CloseableHttpAsyncClient}.
 *
 * @author Isabel Vico Peinado
 *
 */
public class CloseableHttpAsyncClientHook implements ISpecialHook {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * {@link IRuntimeLinker} used to proxy the interceptor of the class.
	 */
	private final IRuntimeLinker runtimeLinker;

	/**
	 * Default constructor.
	 *
	 * @param runtimeLinker
	 *            Used for proxy the interceptor of the builder.
	 */
	public CloseableHttpAsyncClientHook(IRuntimeLinker runtimeLinker) {
		this.runtimeLinker = runtimeLinker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		if ((null != parameters) && (parameters.length == 4)) {
			Object context = parameters[2];
			SpanStore spanStore = (SpanStore) CACHE.invokeMethod(context.getClass(), "getAttribute", new Class<?>[] { String.class }, context, new Object[] { "spanStore" }, null);
			FutureCallbackProxy proxy = new FutureCallbackProxy(spanStore);
			Object newProxy = runtimeLinker.createProxy(FutureCallbackProxy.class, proxy, object.getClass().getClassLoader());
			parameters[3] = newProxy;
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
