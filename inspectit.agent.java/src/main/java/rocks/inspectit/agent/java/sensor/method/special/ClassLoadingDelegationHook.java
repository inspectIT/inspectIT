package rocks.inspectit.agent.java.sensor.method.special;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.util.ClassLoadingUtil;

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
	 * Fully qualified name of the reflect ASM Acess class loader.
	 */
	private static final String REFLECTASM_ACCESS_CLASS_LOADER_FQN = "com.esotericsoftware.reflectasm.AccessClassLoader";

	/**
	 * Logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ClassLoadingDelegationHook.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		return loadClass(object, parameters);
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
	 * @see #loadClass(String)
	 * @param classLoader
	 *            Class loader loading the class (object where the method is executed).
	 * @param params
	 *            Original parameters passed to class loader.
	 * @return Loaded class or <code>null</code>.
	 */
	private Class<?> loadClass(Object classLoader, Object[] params) {
		if ((null != params) && (params.length == 1)) {
			Object p = params[0];
			if (p instanceof String) {
				return loadClass(classLoader, (String) p);
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
	 * @param classLoader
	 *            Class loader loading the class (object where the method is executed).
	 * @param className
	 *            Class name.
	 * @return Loaded class or <code>null</code> if it can not be found with inspectIT class loader.
	 */
	private Class<?> loadClass(Object classLoader, String className) {
		if (loadWithInspectItClassLoader(classLoader, className)) {
			try {
				return getInspectITClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				LOG.warn("Class " + className + " could not be loaded with the inspectIT class loader, although it has the correct prefix.", e);
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Defines if the class should be loaded with our class loader. Only if the class starts with
	 * the inspectIT prefix and the class loader trying to load the class is not the
	 * {@value #REFLECTASM_ACCESS_CLASS_LOADER_FQN}.
	 *
	 * @param classLoader
	 *            Class loader loading the class (object where the method is executed).
	 * @param className
	 *            Name of the class to load.
	 * @return True if we should try to load this with the inspectIT class loader.
	 */
	private boolean loadWithInspectItClassLoader(Object classLoader, String className) {
		return ClassLoadingUtil.isInspectITClass(className) && !REFLECTASM_ACCESS_CLASS_LOADER_FQN.equals(classLoader.getClass().getName());
	}

	/**
	 * Returns inspectIT class loader.
	 *
	 * @return Returns inspectIT class loader.
	 */
	private ClassLoader getInspectITClassLoader() {
		return getClass().getClassLoader();
	}

}
