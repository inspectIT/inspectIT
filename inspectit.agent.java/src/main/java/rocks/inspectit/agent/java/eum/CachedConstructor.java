package rocks.inspectit.agent.java.eum;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.util.AutoboxingHelper;

/**
 * @author Jonas Kunz
 *
 *         A utility class for handling calls to a constructor via reflection. Performs caching on a
 *         per-classloader level.
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
	private ConcurrentHashMap<ClassLoader, Constructor<?>> cachedConstructors;

	/**
	 * This will be the cache value if a given constructor does not exist. It is necessary because
	 * null-values are used to show that the method has not been fetched yet.
	 */
	private static final Constructor<?> NOT_FOUND_MARKER;

	static {
		try {
			NOT_FOUND_MARKER = CachedConstructor.class.getConstructor(String.class);
		} catch (Exception e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * Creates a constructor cache for the default constructor.
	 *
	 * @param className
	 *            the full qualified name of the class to construct
	 */
	public CachedConstructor(String className) {
		this.className = className;
		this.parameterTypes = new String[0];
		this.cachedConstructors = new ConcurrentHashMap<ClassLoader, Constructor<?>>();
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
		this.className = className;
		this.parameterTypes = new String[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			this.parameterTypes[i] = parameterTypes[i].getName();
		}
		this.cachedConstructors = new ConcurrentHashMap<ClassLoader, Constructor<?>>();
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
		this.className = className;
		this.parameterTypes = parameterTypes;
		this.cachedConstructors = new ConcurrentHashMap<ClassLoader, Constructor<?>>();
	}


	/**
	 * Fetches the Constructor based from the context of the given classloader. Caches the
	 * constructor or marks it as unresolveable after the first invocation for this classloader.
	 *
	 * @param cl
	 *            The context to fetch the constructor from.
	 * @return The fetched constructor.
	 *
	 */
	private Constructor<?> findConstructor(ClassLoader cl) {
		if (!(cachedConstructors.containsKey(cl))) {

			Constructor<?> constr;
			try {
				Class<?>[] paramTypes = new Class<?>[parameterTypes.length];
				for (int i = 0; i < paramTypes.length; i++) {
					paramTypes[i] = AutoboxingHelper.findClass(parameterTypes[i], false, cl);
				}
				Class<?> clazz = ClassLoaderAwareClassCache.lookupClass(className, cl);
				if (clazz != null) {
					constr = clazz.getConstructor(paramTypes);
					cachedConstructors.putIfAbsent(cl, constr);
				} else {
					cachedConstructors.putIfAbsent(cl, NOT_FOUND_MARKER);
					LOG.error("Could not find class: " + className);
				}
			} catch (NoSuchMethodException e) {
				cachedConstructors.putIfAbsent(cl, NOT_FOUND_MARKER);
				LOG.error("Could not find constructor of class " + className);
			} catch (ClassNotFoundException e) {
				cachedConstructors.putIfAbsent(cl, NOT_FOUND_MARKER);
				LOG.error("Could not find class: " + e.getMessage());
			}

		}
		Constructor<?> constr = cachedConstructors.get(cl);
		if (NOT_FOUND_MARKER.equals(constr)) {
			return null;
		} else {
			return constr;
		}
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

}