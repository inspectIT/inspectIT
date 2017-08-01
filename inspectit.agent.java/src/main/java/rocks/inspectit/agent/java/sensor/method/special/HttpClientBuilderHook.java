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
	 * The logger of this class.
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
	 * The intercepted class.
	 */
	private static Class<?> interceptorClass;

	/**
	 * Flag whether the intercepter class has been loaded or at least it has to be tried.
	 */
	private static boolean interceptorClassLoaded = false;

	/**
	 * Sets the intercepted class.
	 *
	 * @param clazz
	 *            the intercepted class
	 */
	private static void setInterceptedClass(Class<?> clazz) {
		interceptorClass = clazz;
		interceptorClassLoaded = true;
	}

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
			if (!interceptorClassLoaded) {
				setInterceptedClass(getClass(object.getClass().getClassLoader()));
			}

			if (interceptorClass == null) {
				return null;
			}

			Object newProxy = runtimeLinker.createProxy(HttpRequestInterceptorProxy.class, new HttpRequestInterceptorProxy(), object.getClass().getClassLoader());
			cache.invokeMethod(object.getClass(), ADD_INTERCEPTOR_FIRST, new Class<?>[] { interceptorClass }, object, new Object[] { newProxy }, null);
		}
		return null;
	}

	/**
	 * Get the class of the object through Class.forName.
	 *
	 * @param classLoader
	 *            the class loader to use
	 * @return Returns the class of the object.
	 */
	public Class<?> getClass(ClassLoader classLoader) {
		try {
			return Class.forName(HTTP_REQUEST_INTERCEPTOR_FQN, false, classLoader);
		} catch (ClassNotFoundException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("The class '" + HTTP_REQUEST_INTERCEPTOR_FQN + "' could not be loaded.");
			}
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}
}
