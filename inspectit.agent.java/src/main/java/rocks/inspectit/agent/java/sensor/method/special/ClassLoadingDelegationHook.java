package rocks.inspectit.agent.java.sensor.method.special;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;

/**
 * Hook for the class loading delegation. In the
 * {@link #beforeBody(long, Object, Object[], SpecialSensorConfig)} we check if the class should/can
 * be loaded by our class loader.
 *
 * @author Ivan Senic
 *
 */
public class ClassLoadingDelegationHook implements ISpecialHook {

	/**
	 * Our class start with {@value #CLASS_NAME_PREFIX}.
	 */
	private static final String CLASS_NAME_PREFIX = "rocks.inspectit";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		return loadClass(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}

	/**
	 * Loads class with the given parameters that have been passed to the target class loader.
	 * <p>
	 * Loading will be delegated only if parameters are of size 1 and that single parameter is
	 * String type.
	 *
	 * @see rocks.inspectit.agent.java.PicoAgent#loadClass(String)
	 * @param params
	 *            Original parameters passed to class loader.
	 * @return Loaded class or <code>null</code>.
	 */
	private Class<?> loadClass(Object[] params) {
		if ((null != params) && (params.length == 1)) {
			Object p = params[0];
			if (p instanceof String) {
				return loadClass((String) p);
			}
		}
		return null;
	}

	/**
	 * Delegates the class loading to the {@link #inspectItClassLoader} if the class name starts
	 * with {@value #CLASS_NAME_PREFIX}. Otherwise loads the class with the target class loader. If
	 * the inspectIT class loader throws {@link ClassNotFoundException}, the target class loader
	 * will be used.
	 *
	 * @param className
	 *            Class name.
	 * @return Loaded class or <code>null</code> if it can not be found with inspectIT class loader.
	 */
	private Class<?> loadClass(String className) {
		if (loadWithInspectItClassLoader(className)) {
			try {
				return getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Defines if the class should be loaded with our class loader.
	 *
	 * @param className
	 *            Name of the class to load.
	 * @return True if class name starts with {@value #CLASS_NAME_PREFIX}.
	 */
	private boolean loadWithInspectItClassLoader(String className) {
		return className.startsWith(CLASS_NAME_PREFIX);
	}

}
