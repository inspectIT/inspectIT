package rocks.inspectit.agent.java.sensor.method.special;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.HttpRequestInterceptorProxy;
import rocks.inspectit.agent.java.util.ClassReference;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Hook that intercepts the addInterceptorFirst method of the {@link HttpAsyncClientBuilder}.
 *
 * @author Isabel Vico Peinado
 *
 */
public class HttpClientBuilderHook implements ISpecialHook {

	/**
	 * Fully qualified name for the HttpRequestInterceptor.
	 */
	private static final String HTTP_REQUEST_INTERCEPTOR_FQN = "org.apache.http.HttpRequestInterceptor";

	/**
	 * addInterceptorFirst method name.
	 */
	private static final String ADD_INTERCEPTOR_FIRST = "addInterceptorFirst";

	/**
	 * References the intercepted class.
	 */
	private static ClassReference interceptorClassReference;

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * {@link IRuntimeLinker} used to proxy the interceptor of the builder.
	 */
	private final IRuntimeLinker runtimeLinker;

	/**
	 * Default constructor.
	 *
	 * @param runtimeLinker
	 *            Used for proxy the interceptor of the builder.
	 */
	public HttpClientBuilderHook(IRuntimeLinker runtimeLinker) {
		this.runtimeLinker = runtimeLinker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		if ((parameters != null) && (parameters.length == 0)) {
			if (interceptorClassReference == null) {
				interceptorClassReference = new ClassReference(HTTP_REQUEST_INTERCEPTOR_FQN, object.getClass().getClassLoader());
			}

			Class<?> interceptorClass = interceptorClassReference.get();

			if (interceptorClass == null) {
				return null;
			}

			Object newProxy = runtimeLinker.createProxy(HttpRequestInterceptorProxy.class, new HttpRequestInterceptorProxy(), object.getClass().getClassLoader());
			cache.invokeMethod(object.getClass(), ADD_INTERCEPTOR_FIRST, new Class<?>[] { interceptorClass }, object, new Object[] { newProxy }, null);
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
