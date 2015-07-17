package info.novatec.inspectit.util;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Provides caching of Reflection calls.
 * 
 * @author Stefan Siegl
 */
public class ReflectionCache {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionCache.class);

	/**
	 * Cache that holds the <code> Method </code> instances for the Class and method Names.
	 */
	Cache<Class<?>, Cache<String, Method>> cache = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * Invokes the given method on the given class. This method uses the cache to only lookup the
	 * method if the same method was not looked up before for the same class.
	 * 
	 * <b> Known limitation: The method name is used as key and not the whole signature, so you
	 * cannot cache methods with the same name but different parameters. The reason is that we do
	 * not need this feature right now and it would complicate and slow down the cache. </b>
	 * 
	 * <b> Known limitation: Exceptions are not passed along, but will break the invocation. </b>
	 * 
	 * @param clazz
	 *            The class on which the method should be invoked.
	 * @param methodName
	 *            the name of the method.
	 * @param parameterTypes
	 *            the parameters of the method or <code>null</code> if none are needed.
	 * @param instance
	 *            the instance on which the method call should be invoked.
	 * @param values
	 *            the parameter values or <code>null</code> if none are needed.
	 * @param errorValue
	 *            the value that should be returned in case of an error or <code>null</code> if none
	 *            are needed.
	 * @return the invocation result.
	 */
	public Object invokeMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object instance, Object[] values, Object errorValue) {
		if (null == clazz || null == methodName) {
			return errorValue;
		}
		Cache<String, Method> classCache = cache.getIfPresent(clazz);
		if (null == classCache) {
			// create a new cache and add it. There can be race conditions here. There is no entry
			// for class C and caller A and caller B
			// want to execute method a or b on the same class. It can thus happen that one
			// overwrites the other. This is not a big issue
			// as the only result will be that the method will not be cached for one and will be
			// cached with the next invocation. This
			// approach is way cheaper than synchronisation or copy on write.
			classCache = CacheBuilder.newBuilder().expireAfterAccess(20 * 60, TimeUnit.SECONDS).weakKeys().build();
			cache.put(clazz, classCache);
		}

		// get method
		Method method = classCache.getIfPresent(methodName);
		if (null == method) {
			try {
				method = clazz.getMethod(methodName, parameterTypes);
			} catch (Exception e) {
				LOG.warn("Could not lookup method " + methodName + " on class " + clazz.getName(), e);
				return errorValue;
			}
			classCache.put(methodName, method);
		}

		// invoke method
		try {
			return method.invoke(instance, values);
		} catch (Exception e) {
			LOG.warn("Could not invoke method " + methodName + " on instance " + instance, e);
			return errorValue;
		}
	}
}
