package rocks.inspectit.agent.java.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small util class for the easer checking of {@link Class} objects.
 *
 * @see #searchInterface(Class, String)
 * @author Ivan Senic
 */
public final class ClassUtil {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);

	/**
	 * Private constructor.
	 */
	private ClassUtil() {
	}

	/**
	 * Returns the name of the class loader of the given class.
	 *
	 * @param clazz
	 *            the class
	 * @param defaultValue
	 *            the string which is returned if an exception occurred
	 * @return the name of the class loader
	 */
	public static String getClassLoaderName(Class<?> clazz, String defaultValue) {
		try {
			ClassLoader classLoader = clazz.getClassLoader();
			if (classLoader == null) {
				return "bootstrap classloader";
			} else {
				return clazz.getClassLoader().getClass().getName();
			}
		} catch (SecurityException e) {
			return defaultValue;
		}
	}

	/**
	 * Search if class implements the specified interface. If so it returns the class representing
	 * the interface. This is recursive method, so it searches also all the superclasses of the
	 * given class and all the superinterfaces of directly implemented interfaces.
	 *
	 * @param clazz
	 *            Class to check
	 * @param interfaceName
	 *            Interface to locate
	 * @return Interface {@link Class} object or <code>null</code> if one can not be found on the
	 *         given class and it's superclasses.
	 */
	public static Class<?> searchInterface(Class<?> clazz, String interfaceName) {
		if (clazz == null) {
			return null;
		}

		List<Class<?>> searched = new ArrayList<Class<?>>();
		return searchInterface(clazz, interfaceName, searched);
	}

	/**
	 * Internal search interface method.
	 *
	 * @param clazz
	 *            Class to check
	 * @param interfaceName
	 *            Interface to locate
	 * @param searched
	 *            List of already checked interfaces.
	 * @return Interface {@link Class} object or <code>null</code> if one can not be found on the
	 *         given class and it's superclasses.
	 */
	private static Class<?> searchInterface(Class<?> clazz, String interfaceName, List<Class<?>> searched) {
		try {
			if ((clazz != null) && !clazz.equals(Object.class)) {
				// get all interfaces
				Class<?>[] interfaces = clazz.getInterfaces();
				for (Class<?> interf : interfaces) {
					// only if we did not checked this before check it
					if (!searched.contains(interf)) {
						// if we have the one return it
						if (interf.getName().equals(interfaceName)) {
							return interf;
						}
						// otherwise mark as searched
						searched.add(interf);
						// and check the interface itself
						Class<?> found = searchInterface(interf, interfaceName, searched);
						if (null != found) {
							return found;
						}
					}
				}

				// check super class
				return searchInterface(clazz.getSuperclass(), interfaceName, searched);
			}

			// if nothing found return null
			return null;
		} catch (Throwable t) { // NOPMD
			// catching any throwable just for case
			LOG.warn("Unexpected error occurred checking the " + clazz.getName() + " for interface " + interfaceName + ".");
			return null;
		}
	}
}
