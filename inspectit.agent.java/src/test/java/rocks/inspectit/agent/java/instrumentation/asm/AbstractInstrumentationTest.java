package rocks.inspectit.agent.java.instrumentation.asm;

import java.lang.reflect.Method;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Abstract class for the instrumentation tests. Provides method for creating a class from bytes,
 * creating instance of such class and calling methods.
 *
 * @see #createClass(String, byte[])
 * @see #createInstance(String, byte[])
 * @see #callMethod(Object, String, Object[])
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public abstract class AbstractInstrumentationTest extends TestBase {

	/**
	 * Creates instance of a class defined by given bytes. Class will be created with
	 * {@link #createClass(String, byte[])} method.
	 */
	protected Object createInstance(String className, byte[] bytes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return createClass(className, bytes).newInstance();
	}

	/**
	 * Creates class from given bytes. Class will have the given FQN. Class will be created with the
	 * {@link InstrumentingClassLoader}, thus not the same class loader as the one test class is
	 * loaded.
	 */
	protected Class<?> createClass(String className, byte[] bytes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return new InstrumentingClassLoader().findClass(className, bytes);
	}

	/**
	 * Calls method on the given object using reflection. Method will be invoked with the given
	 * parameters.
	 * <p>
	 * There is support for String, int and boolean parameter types.
	 */
	protected Object callMethod(Object object, String methodName, Object[] parameters) throws Exception {
		if (null == parameters) {
			parameters = new Object[0];
		}
		Class<?> clazz = object.getClass();
		Class<?>[] parameterClasses = null;
		parameterClasses = new Class[parameters.length];
		for (int i = 0; i < parameterClasses.length; i++) {
			String parameter = (String) parameters[i];
			if ("int".equals(parameter)) {
				parameterClasses[i] = Integer.TYPE;
				parameters[i] = 3;
			} else if ("boolean".equals(parameter)) {
				parameterClasses[i] = Boolean.TYPE;
				parameters[i] = false;
			} else {
				parameterClasses[i] = Class.forName(parameter);
			}
		}
		Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
		method.setAccessible(true);
		return method.invoke(object, parameters);
	}

	protected class InstrumentingClassLoader extends ClassLoader {

		protected Class<?> findClass(String name, byte[] bytes) throws ClassNotFoundException {
			return defineClass(name, bytes, 0, bytes.length);
		}
	}
}
