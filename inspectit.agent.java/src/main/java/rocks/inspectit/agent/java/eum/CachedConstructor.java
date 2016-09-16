package rocks.inspectit.agent.java.eum;

import java.lang.reflect.Constructor;
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
 *         A utility class for handling calls to a constructor via reflection. Performs caching on a
 *         per-classloader level, as for the constructor invocation the actual Class<?> obejct might
 *         be unknown.
 *
 */
public class CachedConstructor {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CachedConstructor.class);

	/**
	 * The name of the class to construct.
	 */
	private String className;


	/**
	 * The types of the parameters the cosntructor accepts.
	 */
	private String[] parameterTypes;


	/**
	 * Caches constructors on a per-classloader basis.
	 */
	private LoadingCache<ClassLoader, Constructor<?>> cachedConstructors;

	/**
	 *
	 * Creates a constructor cache for the default constructor.
	 *
	 * @param className
	 *            the full qualified name of the class to construct
	 */
	public CachedConstructor(String className) {
		init(className, new String[0]); // performs null checks
	}

	/**
	 *
	 * Creates a constructor cache based on the given class name and constructor parameters.
	 *
	 * @param className
	 *            the full qualified name of the class declaring the constructor
	 * @param parameterTypes
	 *            the types of the constructors parameters
	 */
	public CachedConstructor(String className, Class<?>... parameterTypes) {
		if (parameterTypes == null) {
			throw new IllegalArgumentException("Null values for the constructor arguments are not permitted!");
		}
		String[] parameterTypesFetched = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesFetched[i] = parameterTypes[i].getName();
		}
		init(className, parameterTypesFetched); // performs additional null checks
	}

	/**
	 *
	 * Creates a constructor cache based on the given class name and parameter types for the
	 * constructor.
	 *
	 * @param className
	 *            the full qualified name of the class declaring the constructor
	 *
	 * @param parameterTypes
	 *            the full qualified names of the types of the constructors parameters
	 */
	public CachedConstructor(String className, String... parameterTypes) {
		init(className, parameterTypes);
	}

	/**
	 * Invokes the constructor in the given classloader context. Catches any occurring exceptions.
	 *
	 * @param env
	 *            the context to fetch the constructor from
	 * @param parameters
	 *            the parameters to pass to the constructor
	 * @return the newly created instance or null if the call to the constructor failed
	 */
	public Object newInstanceSafe(ClassLoader env, Object... parameters) {
		try {
			return newInstance(env, parameters);
		} catch (Exception e) {
			LOG.error("Exception invoking constructor on " + className + ": " + e.getClass());
			return null;
		}
	}

	/**
	 * Invokes the constructor in the given classloader context. Does not catch any occurring
	 * exceptions.
	 *
	 * @param env
	 *            the context to fetch the constructor from
	 * @param parameters
	 *            the parameters to pass to the constructor
	 * @return the newly created instance
	 * @throws Exception
	 *             any exception occurring during the instantiation.
	 */
	public Object newInstance(ClassLoader env, Object... parameters) throws Exception {

		Constructor<?> constructor = findConstructor(env);
		if (constructor == null) {
			throw new RuntimeException("Constructor not found on " + className);
		} else {
			return constructor.newInstance(parameters);
		}

	}

	/**
	 * @param className
	 *            the name of the class
	 * @param parameterTypes
	 *            the full qualified names of the parameters
	 */
	private void init(String className, String... parameterTypes) {
		if ((className == null) || (parameterTypes == null)) {
			throw new IllegalArgumentException("Null values for the constructor arguments are not permitted!");
		}
		this.className = className;
		this.parameterTypes = parameterTypes;
		this.cachedConstructors = CacheBuilder.newBuilder().weakKeys().weakValues().build(new ConstructorCacheLoader());
	}

	/**
	 * Fetches the Constructor based from the context of the given classloader. Caches the
	 * constructor.
	 *
	 * @param cl
	 *            The context to fetch the constructor from.
	 * @return The fetched constructor.
	 *
	 */
	private Constructor<?> findConstructor(ClassLoader cl) {
		try {
			return cachedConstructors.get(cl);
		} catch (ExecutionException e) {
			LOG.error("Could not locate constructor", e.getCause());
			return null;
		}
	}

	/**
	 * CacheLoader used for populating the constructor cache.
	 *
	 * @author Jonas Kunz
	 */
	private class ConstructorCacheLoader extends CacheLoader<ClassLoader, Constructor<?>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Constructor<?> load(ClassLoader cl) throws Exception {
			Class<?>[] paramTypes = new Class<?>[parameterTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				paramTypes[i] = AutoboxingHelper.findClass(parameterTypes[i], false, cl);
			}
			Class<?> clazz = ClassLoaderAwareClassCache.lookupClass(className, cl);
			if (clazz != null) {
				return clazz.getConstructor(paramTypes);
			} else {
				throw new ClassNotFoundException("Could not find class " + className);
			}

		}

	}

}