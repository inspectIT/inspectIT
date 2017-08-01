package rocks.inspectit.agent.java.sensor.method.special;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.HttpRequestInterceptorProxy;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Hook that intercepts the addInterceptorFirst method of the {@link HttpAsyncClientBuilder}.
 *
 * @author Isabel Vico Peinado
 *
 */
public class HttpClientBuilderHook implements ISpecialHook {

	/**
	 * Logger of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(HttpClientBuilderHook.class);

	/**
	 * Fully qualified name for the HttpRequestInterceptor.
	 */
	private static final String HTTP_REQUEST_INTERCEPTOR_FQN = "org.apache.http.HttpRequestInterceptor";

	/**
	 * addInterceptorFirst method name.
	 */
	private static final String ADD_INTERCEPTOR_FIRST = "addInterceptorFirst";

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
			Class<?> interceptorClass = getRequestInterceptorClass(object.getClass().getClassLoader());

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

	/**
	 * Returns the {@link #HTTP_REQUEST_INTERCEPTOR_FQN} class.
	 *
	 * @param classLoader
	 *            the class loader to use to get the class
	 * @return Instance of the {@link #HTTP_REQUEST_INTERCEPTOR_FQN} class or <code>null</code> if
	 *         it cannot be loaded.
	 */
	Class<?> getRequestInterceptorClass(ClassLoader classLoader) {
		try {
			return Class.forName(HTTP_REQUEST_INTERCEPTOR_FQN, false, classLoader);
		} catch (ClassNotFoundException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Failed loading of '" + HTTP_REQUEST_INTERCEPTOR_FQN + "' class.", e);
			}
			return null;
		}
	}
}
