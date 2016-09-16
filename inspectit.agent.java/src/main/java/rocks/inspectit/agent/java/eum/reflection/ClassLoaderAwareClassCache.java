package rocks.inspectit.agent.java.eum.reflection;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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
	 * The actual cache. For each classloader a map for resolving the class names is stored. Weak
	 * keys do not prevent the GC from unloading a classloader, soft values allow removal of the
	 * cache entries if the memory is full.
	 */
	private static LoadingCache<ClassLoader, LoadingCache<String, Class<?>>> cache = CacheBuilder.newBuilder().weakKeys().softValues()
			.build(new CacheLoader<ClassLoader, LoadingCache<String, Class<?>>>() {
				@Override
				public LoadingCache<String, Class<?>> load(final ClassLoader classloader) {
					return CacheBuilder.newBuilder().weakValues().build(new CacheLoader<String, Class<?>>() {
						@Override
						public Class<?> load(String fullQualifiedName) throws ClassNotFoundException {
							return Class.forName(fullQualifiedName, false, classloader);
						}

					});
				}

			});

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
		try {
			if (cl == null) {
				cl = ClassLoader.getSystemClassLoader();
			}
			LoadingCache<String, Class<?>> clCache = cache.getUnchecked(cl);
			return clCache.get(fqn);
		} catch (ExecutionException e) {
			return null;
		}
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
