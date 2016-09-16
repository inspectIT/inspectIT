package rocks.inspectit.agent.java.eum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.util.AutoboxingHelper;

/**
 * @author Jonas Kunz
 *
 *         A utility class for handling calls to a method via reflection. Performs the caching on a
 *         per-classloader level
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
	 * Caches methods on a per-classloader basis.
	 */
	private ConcurrentHashMap<ClassLoader, Method> cachedMethods;

	/**
	 * This will be the cache value if a given method does not exist. It is necessary because
	 * null-values are used to show thaht the method has not been fetched yet.
	 */
	private static final Method NOT_FOUND_MARKER;

	static {
		try {
			NOT_FOUND_MARKER = CachedMethod.class.getDeclaredMethod("findMethod", ClassLoader.class);
		} catch (Exception e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

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
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = new String[0];
		this.cachedMethods = new ConcurrentHashMap<ClassLoader, Method>();
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
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			this.parameterTypes[i] = parameterTypes[i].getName();
		}
		this.cachedMethods = new ConcurrentHashMap<ClassLoader, Method>();
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
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.cachedMethods = new ConcurrentHashMap<ClassLoader, Method>();
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
	 * Performs the cache lookup or fetches the mtod if it hasn't been cached yet.
	 *
	 * @param cl
	 *            the classloader to use for the lookup
	 * @return the found method, or null if it wasn't found
	 * @throws SecurityException
	 *             delegated
	 */
	private Method findMethod(ClassLoader cl) throws SecurityException {
		if (!(cachedMethods.containsKey(cl))) {
			// no need for synchronization, assignment is atomic
			Method meth;
			try {
				Class<?>[] paramTypes = new Class<?>[parameterTypes.length];
				for (int i = 0; i < paramTypes.length; i++) {
					paramTypes[i] = AutoboxingHelper.findClass(parameterTypes[i], false, cl);
				}
				meth = Class.forName(className, true, cl).getMethod(methodName, paramTypes);
				cachedMethods.putIfAbsent(cl, meth);
			} catch (NoSuchMethodException e) {
				cachedMethods.putIfAbsent(cl, NOT_FOUND_MARKER);
				LOG.error("Could not find method " + methodName + " of class " + className);
			} catch (ClassNotFoundException e) {
				cachedMethods.putIfAbsent(cl, NOT_FOUND_MARKER);
				LOG.error("Could not find class: " + e.getMessage());
			}

		}
		Object method = cachedMethods.get(cl);
		if (NOT_FOUND_MARKER.equals(method)) {
			return null;
		} else {
			return (Method) method;
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
	 * Utiltiy method for throwing checked exceptions without having to declare them.
	 *
	 * @param exc
	 *            the exception to throw
	 */
	// NOCHKON
	@SuppressWarnings({ "PMD", "unchecked" })
	static void throwCheckedException(Exception exc) {
		class Unchecker<E extends Exception> {
			private void uncheck(Exception exc) throws E {
				throw (E) exc;
			}
		}
		new Unchecker<RuntimeException>().uncheck(exc);
	}
	// NOCHKOFF

}