package rocks.inspectit.agent.java.instrumentation.asm;

import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
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
	 * There is support for String, int, boolean and Object[] array parameter types.
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
			} else if ("array".equals(parameter)) {
				parameterClasses[i] = Object[].class;
				parameters[i] = new Object[] { 1, 2L, true, "test" };
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

	protected void prepareConfigurationMockMethod(MethodInstrumentationConfig point, Class<?> clazz, String methodName, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		when(point.getTargetClassFqn()).thenReturn(clazz.getName());
		when(point.getTargetMethodName()).thenReturn(methodName);
		if (method.getReturnType().isArray()) {
			when(point.getReturnType()).thenReturn(method.getReturnType().getComponentType().getName() + "[]");
		} else {
			when(point.getReturnType()).thenReturn(method.getReturnType().getName());
		}
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			List<String> params = new ArrayList<String>();
			for (Class<?> paramType : parameterTypes) {
				if (paramType.isArray()) {
					params.add(paramType.getComponentType().getName() + "[]");
				} else {
					params.add(paramType.getName());
				}
			}
			when(point.getParameterTypes()).thenReturn(params);
		}
	}

	protected void prepareConfigurationMockConstructor(MethodInstrumentationConfig point, Class<?> clazz, boolean staticConstructor, Class<?>... parameterTypes)
			throws SecurityException, NoSuchMethodException {
		clazz.getDeclaredConstructor(parameterTypes);
		when(point.getTargetClassFqn()).thenReturn(clazz.getName());
		when(point.getTargetMethodName()).thenReturn(staticConstructor ? "<clinit>" : "<init>");
		when(point.getReturnType()).thenReturn("void");
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			List<String> params = new ArrayList<String>();
			for (Class<?> paramType : parameterTypes) {
				if (paramType.isArray()) {
					params.add(paramType.getComponentType().getName() + "[]");
				} else {
					params.add(paramType.getName());
				}
			}
			when(point.getParameterTypes()).thenReturn(params);
		}
	}

}
