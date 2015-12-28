package info.novatec.inspectit.util;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Provides caching of Reflection calls for improved performance.
 *
 * @author Stefan Siegl
 * @author Patrice Bouillet
 */
public class ReflectionCache {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionCache.class);

	/**
	 * Cache that holds the {@link MethodAccess} classes which ReflectASM is generating for each
	 * class to invoke on.
	 */
	private final Cache<Class<?>, MethodAccess> cache = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * Invokes the given method on the given class.
	 *
	 * <b> Known limitation: Exceptions are not passed along, but will break the invocation. </b>
	 *
	 * @param clazz
	 *            The class on which the method should be invoked.
	 * @param methodName
	 *            the name of the method.
	 * @param instance
	 *            the instance on which the method call should be invoked.
	 * @param values
	 *            the parameter values or <code>null</code> if none are needed.
	 * @param errorValue
	 *            the value that should be returned in case of an error or <code>null</code> if none
	 *            are needed. Must be of same type as <T>.
	 * @return the invocation result.
	 * @param <T>
	 *            the type of the result of the execution.
	 */
	@SuppressWarnings("unchecked")
	public <T> T invokeMethod(final Class<?> clazz, String methodName, Object instance, Object[] values, T errorValue) {
		if (null == clazz || null == methodName) {
			return errorValue;
		}

		try {
			MethodAccess methodAccess = cache.get(clazz, new Callable<MethodAccess>() {

				public MethodAccess call() throws Exception {
					return MethodAccess.get(clazz);
				}
			});

			return (T) methodAccess.invoke(instance, methodName, values);
		} catch (Exception e) {
			LOG.warn("Could not invoke method " + methodName + " on instance " + instance, e);
			return errorValue;
		}
	}

}
