package rocks.inspectit.agent.java.eum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import rocks.inspectit.agent.java.util.AutoboxingHelper;

/**
 * @author Jonas Kunz
 *
 *         A utility class for handling calls to a method via reflection. Performs the caching on a
 *         per-classloader level, to make sure we invoke interface-based methods using the
 *         interface, not the class.
 *
 * @param <R>
 *            the return type of the method
 */
public class CachedMethod<R> {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CachedMethod.class);

	/**
	 * The name of the method that will be called via reflection.
	 */

	private String methodName;
	/**
	 * The name of the method that will be called via reflection.
	 */
	private String className;

	/**
	 * The types of the parameters.
	 */
	private String[] parameterTypes;

	/**
	 * Caches methods on a per-classloader basis, this ensures only one entry for each interface
	 * instead of one entry per implementing class.
	 */
	private LoadingCache<ClassLoader, Method> cachedMethods;

	/**
	 *
	 * Creates a method cache based on the given names. Expects that the method does not need any
	 * parameters.
	 *
	 * @param className
	 *            the full qualified name of the class declaring the method
	 * @param methodName
	 *            the name of the method
	 */
	public CachedMethod(String className, String methodName) {
		init(className, methodName, new String[0]);
	}

	/**
	 *
	 * Creates a method cache based on the given names.
	 *
	 * @param className
	 *            the full qualified name of the class declaring the method
	 * @param methodName
	 *            the name of the method
	 * @param parameterTypes
	 *            the types of the methods parameters
	 */
	public CachedMethod(String className, String methodName, Class<?>... parameterTypes) {
		if (parameterTypes == null) {
			throw new IllegalArgumentException("Null values for the constructor arguments are not permitted!");
		}
		String[] parameterTypesFetched = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesFetched[i] = parameterTypes[i].getName();
		}
		init(className, methodName, parameterTypesFetched);
	}

	/**
	 *
	 * Creates a method cache based on the given names.
	 *
	 * @param className
	 *            the full qualified name of the class declaring the method
	 * @param methodName
	 *            the name of the method
	 * @param parameterTypes
	 *            the names of the types of the methods parameters
	 */
	public CachedMethod(String className, String methodName, String... parameterTypes) {
		init(className, methodName, parameterTypes);
	}

	/**
	 * Calls the target method, but don't catch any exceptions.
	 *
	 * @param instance
	 *            the instance this method should be called on
	 * @param parameters
	 *            the parameters to be passed to the method
	 * @return the return value of the method call
	 * @throws Exception
	 *             any exception which occured when calling the method
	 */
	@SuppressWarnings("unchecked")
	public R call(Object instance, Object... parameters) throws Exception {

		ClassLoader cl = instance.getClass().getClassLoader();
		Method methodToCall = findMethod(cl);
		if (methodToCall == null) {
			throw new RuntimeException("Method " + methodName + " not found on " + instance.getClass().getName());
		} else {
			try {
				return (R) methodToCall.invoke(instance, parameters);
			} catch (InvocationTargetException exc) {
				Throwable cause = exc.getCause();
				if (cause instanceof Exception) {
					throw (Exception) cause;
				} else {
					throw exc;
				}
			}
		}

	}

	/**
	 * Calls the method and catches any occuring exception.
	 *
	 * @param instance
	 *            the instance to call the method on
	 * @param parameters
	 *            the paramters to pass to the method
	 * @return the balue returned by the call, or null if an exception occured
	 */
	public R callSafe(Object instance, Object... parameters) {
		try {
			return call(instance, parameters);
		} catch (Exception e) {
			LOG.error("Exception calling " + methodName + " on " + instance + ": " + e.getClass());
			return null;
		}
	}

	/**
	 * Calls the method and catches any occuring exception.
	 *
	 * @param exceptionToRethrow
	 *            exceptions of this type will not be caught. Can even be checked exceptions!
	 * @param instance
	 *            the instance to call the method on
	 * @param parameters
	 *            the parameters to pass to the method
	 * @return the value returned by the call, or null if an exception occured
	 */
	public R callSafeExceptions(Class<? extends Exception> exceptionToRethrow, Object instance, Object... parameters) {
		try {
			return call(instance, parameters);
		} catch (Exception e) {
			if (exceptionToRethrow.isInstance(e)) {
				throwCheckedException(e);
				return null; // unreachable
			} else {
				LOG.error("Exception calling " + methodName + " on " + instance + ": " + e.getClass());
				return null;
			}
		}
	}

	/**
	 * @param className
	 *            the name of the class
	 * @param methodName
	 *            the name of the method to find
	 * @param parameterTypes
	 *            the full qualified names of the parameters
	 */
	private void init(String className, String methodName, String... parameterTypes) {
		if ((className == null) || (methodName == null) || (parameterTypes == null)) {
			throw new IllegalArgumentException("Null values for the constructor arguments are not permitted!");
		}
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.cachedMethods = CacheBuilder.newBuilder().weakKeys().weakValues().build(new MethodCacheLoader());
	}

	/**
	 * Performs the cache lookup or fetches the mtod if it hasn't been cached yet.
	 *
	 * @param cl
	 *            the classloader to use for the lookup
	 * @return the found method, or null if it wasn't found
	 */
	private Method findMethod(ClassLoader cl) {
		try {
			return cachedMethods.get(cl);
		} catch (ExecutionException e) {
			LOG.error("Could not locate method", e.getCause());
			return null;
		}
	}

	/**
	 * Utiltiy method for throwing checked exceptions without having to declare them.
	 *
	 * @param exc
	 *            the exception to throw
	 */
	// NOCHKON
	@SuppressWarnings({ "PMD", "unchecked" })
	private void throwCheckedException(Exception exc) {
		class Unchecker<E extends Exception> {
			private void uncheck(Exception exc) throws E {
				throw (E) exc;
			}
		}
		new Unchecker<RuntimeException>().uncheck(exc);
	}
	// NOCHKOFF

	/**
	 * CacheLoader used for populating the method cache.
	 *
	 * @author Jonas Kunz
	 */
	private class MethodCacheLoader extends CacheLoader<ClassLoader, Method> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Method load(ClassLoader cl) throws Exception {
			Class<?>[] paramTypes = new Class<?>[parameterTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				paramTypes[i] = AutoboxingHelper.findClass(parameterTypes[i], false, cl);
			}
			Class<?> clazz = ClassLoaderAwareClassCache.lookupClass(className, cl);
			if (clazz != null) {
				return clazz.getMethod(methodName, paramTypes);
			} else {
				throw new ClassNotFoundException("Could not find class " + className);
			}

		}

	}
}