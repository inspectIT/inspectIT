package info.novatec.inspectit.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Provides caching of Reflection calls.
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
	 * Cache that holds the <code> MethodTuple </code> instances for the Class and method indexes.
	 */
	private Cache<Class<?>, Cache<String, MethodTuple>> cache = CacheBuilder.newBuilder().weakKeys().softValues().build();

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
		Cache<String, MethodTuple> classCache = cache.getIfPresent(clazz);
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
		MethodTuple methodTuple = classCache.getIfPresent(methodName);
		if (null == methodTuple) {
			try {
				MethodAccess methodAccess = MethodAccess.get(clazz);
				int methodIndex;
				if (parameterTypes == null || parameterTypes.length == 0) {
					methodIndex = methodAccess.getIndex(methodName);
				} else {
					methodIndex = methodAccess.getIndex(methodName, parameterTypes);
				}
				methodTuple = new MethodTuple(methodAccess, methodIndex);
			} catch (Exception e) {
				LOG.warn("Could not lookup method " + methodName + " on class " + clazz.getName(), e);
				return errorValue;
			}
			classCache.put(methodName, methodTuple);
		}

		// invoke method
		try {
			if (parameterTypes == null || parameterTypes.length == 0) {
				return methodTuple.getMethodAccess().invoke(instance, methodTuple.getMethodIndex(), methodName);
			} else {
				return methodTuple.getMethodAccess().invoke(instance, methodTuple.getMethodIndex(), methodName, parameterTypes, values);
			}
			// return method.invoke(instance, values);
		} catch (Exception e) {
			LOG.warn("Could not invoke method " + methodName + " on instance " + instance, e);
			return errorValue;
		}
	}

	/**
	 * Small tuple inner class for storing the method access object and the appropriate index of the
	 * method to be accessed.
	 * 
	 * @author Patrice Bouillet
	 *
	 */
	private static class MethodTuple {

		/**
		 * The generated method access object which could basically access all methods of one class.
		 */
		private final MethodAccess methodAccess;

		/**
		 * The index attribute to have no lookup costs for a method in the method access object.
		 */
		private final int methodIndex;

		/**
		 * Constructor to build up the tuple class.
		 * 
		 * @param methodAccess
		 *            method access object to set.
		 * @param methodIndex
		 *            method index to set.
		 */
		public MethodTuple(MethodAccess methodAccess, int methodIndex) {
			this.methodAccess = methodAccess;
			this.methodIndex = methodIndex;
		}

		/**
		 * Gets {@link #methodAccess}.
		 * 
		 * @return {@link #methodAccess}
		 */
		public MethodAccess getMethodAccess() {
			return methodAccess;
		}

		/**
		 * Gets {@link #methodIndex}.
		 * 
		 * @return {@link #methodIndex}
		 */
		public int getMethodIndex() {
			return methodIndex;
		}

	}
}
