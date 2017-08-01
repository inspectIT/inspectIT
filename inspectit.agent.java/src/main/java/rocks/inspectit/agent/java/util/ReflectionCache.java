package rocks.inspectit.agent.java.util;

import java.lang.reflect.Field;
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
	 * Cache that holds the {@link Method} instances for the Class and method Names.
	 */
	Cache<Class<?>, Cache<String, Method>> methodCache = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * Cache that holds the {@link Field} instances for the Class and method Names.
	 */
	Cache<Class<?>, Cache<String, Field>> fieldCache = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * Invokes the given method on the given class. This method uses the cache to only lookup the
	 * method if the same method was not looked up before for the same class. Same as calling
	 * {@link #invokeMethod(Class, String, Class[], Object, Object[], Object, null)}.
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
		return invokeMethod(clazz, methodName, parameterTypes, instance, values, errorValue, null);
	}

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
	 * @param interfaceName
	 *            If interface name is passed then a search will be performed to find specified
	 *            method on the given interface. If interface method is not found, the class method
	 *            will be executed. Can be <code>null</code> to directly use class method.
	 * @return the invocation result.
	 */
	public Object invokeMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object instance, Object[] values, Object errorValue, String interfaceName) {
		if ((null == clazz) || (null == methodName)) {
			return errorValue;
		}
		Cache<String, Method> classCache = methodCache.getIfPresent(clazz);
		if (null == classCache) {
			// create a new cache and add it. There can be race conditions here. There is no entry
			// for class C and caller A and caller B
			// want to execute method a or b on the same class. It can thus happen that one
			// overwrites the other. This is not a big issue
			// as the only result will be that the method will not be cached for one and will be
			// cached with the next invocation. This
			// approach is way cheaper than synchronization or copy on write.
			classCache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).weakKeys().build();
			methodCache.put(clazz, classCache);
		}

		// get method
		Method method = classCache.getIfPresent(methodName);
		if (null == method) {
			try {
				// check if we need to search with interface
				Class<?> interfaceClass = null;
				if (null != interfaceName) {
					interfaceClass = ClassUtil.searchInterface(clazz, interfaceName);
				}

				// if interface class is found than use method from interface, otherwise from class
				if (interfaceClass != null) {
					try {
						method = interfaceClass.getMethod(methodName, parameterTypes);
					} catch (Throwable t) { // NOPMD
						LOG.warn("Could not lookup method " + methodName + " on class " + clazz.getName() + ". Trying with the class.", t);
					}
				}

				if (null == method) {
					method = clazz.getMethod(methodName, parameterTypes);
				}
				method.setAccessible(true);
			} catch (Throwable t) { // NOPMD
				LOG.warn("Could not lookup method " + methodName + " on class " + clazz.getName(), t);
				return errorValue;
			}
			classCache.put(methodName, method);
		}

		// invoke method
		try {
			return method.invoke(instance, values);
		} catch (Throwable t) { // NOPMD
			LOG.warn("Could not invoke method " + methodName + " on instance " + instance, t);
			return errorValue;
		}
	}

	/**
	 * Gets the given field on the given class. This method uses the cache to only lookup the field
	 * if the same field was not looked up before for the same class.
	 *
	 * @param clazz
	 *            The class on which the field should be gotten.
	 * @param fieldName
	 *            The name of the field.
	 * @param instance
	 *            The instance on which the method call should be invoked.
	 * @param errorValue
	 *            The value that should be returned in case of an error or <code>null</code> if none
	 *            are needed.
	 * @return The current value of the field.
	 */
	public Object getField(Class<?> clazz, String fieldName, Object instance, Object errorValue) {
		if ((null == clazz) || (null == fieldName)) {
			return errorValue;
		}
		Cache<String, Field> classCache = fieldCache.getIfPresent(clazz);
		if (null == classCache) {
			// create a new cache and add it. There can be race conditions here. There is no entry
			// for class C and caller A and caller B
			// want to execute method a or b on the same class. It can thus happen that one
			// overwrites the other. This is not a big issue
			// as the only result will be that the method will not be cached for one and will be
			// cached with the next invocation. This
			// approach is way cheaper than synchronization or copy on write.
			classCache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).weakKeys().build();
			fieldCache.put(clazz, classCache);
		}

		// get field
		Field field = classCache.getIfPresent(fieldName);
		if (null == field) {
			Class<?> lookupClass = clazz;
			while (field == null) {
				try {
					field = lookupClass.getDeclaredField(fieldName);
				} catch (NoSuchFieldException nsfe) {
					lookupClass = lookupClass.getSuperclass();
					if (lookupClass == null) {
						LOG.warn("Could not lookup field " + fieldName + " on class " + clazz.getName(), nsfe);
						return errorValue;
					}
				} catch (Throwable t) { // NOPMD
					LOG.warn("Could not lookup field " + fieldName + " on class " + clazz.getName(), t);
					return errorValue;
				}
			}

			field.setAccessible(true);
			classCache.put(fieldName, field);
		}

		// get field
		try {
			return field.get(instance);
		} catch (Throwable t) { // NOPMD
			LOG.warn("Could not get field " + fieldName + " on instance " + instance, t);
			return errorValue;
		}
	}
}
