package rocks.inspectit.agent.java.eum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for looking up classes based on their names. This Cache takes the classloader of the
 * classes into account, it therefore allows multiple classes with the same name on the classpath.
 *
 * @author Jonas Kunz
 *
 */
public final class ClassLoaderAwareClassCache {

	/**
	 * Static Utility class, therefore no constructor.
	 */
	private ClassLoaderAwareClassCache() {
	}

	/**
	 * The actual cache. For each classloader a map for resolving the class names is stored.
	 */
	private static ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, Class<?>>> cache = new ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, Class<?>>>();

	/**
	 *
	 * Tries to find the class with the given name.
	 *
	 * @param fqn
	 *            The full qualified name of the class to find
	 * @param cl
	 *            The classloader to start the search from.
	 * @return the found class or null if it could not be found
	 */
	public static Class<?> lookupClass(String fqn, ClassLoader cl) {
		ConcurrentHashMap<String, Class<?>> clCache = cache.get(cl);
		if (clCache == null) {
			cache.putIfAbsent(cl, new ConcurrentHashMap<String, Class<?>>());
			clCache = cache.get(cl);
		}
		if (!clCache.containsKey(fqn)) {
			try {
				clCache.putIfAbsent(fqn, Class.forName(fqn, false, cl));
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return clCache.get(fqn);
	}

	/**
	 * Looks for the class with the given name which is accessible from the given object's
	 * classloader.
	 *
	 * @param fqn
	 *            The full qualified name of the class to find
	 * @param classLoaderSource
	 *            The object to use as context.
	 * @return the class or null if it could not be found
	 */
	public static Class<?> lookupClassRelative(String fqn, Object classLoaderSource) {
		return lookupClass(fqn, classLoaderSource.getClass().getClassLoader());
	}

	/**
	 * Checks if the given object is an isntance of the class specified by the given full qualified
	 * name. If such a class does not exist in the context of this object, false is returned.
	 *
	 * @param objToCheck
	 *            the object for which the instaceof test shall be performed.
	 * @param fqn
	 *            the full qualified name of the class to check against
	 * @return true, if objToCheck is an instance of the class or one of its subclasses
	 */
	public static boolean isInstance(Object objToCheck, String fqn) {
		Class<?> clazz = lookupClass(fqn, objToCheck.getClass().getClassLoader());
		if (clazz == null) {
			return false;
		} else {
			return clazz.isInstance(objToCheck);
		}
	}

}
