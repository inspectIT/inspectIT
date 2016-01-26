package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.testbase.TestBase;

import java.lang.reflect.Method;

@SuppressWarnings("PMD")
public abstract class AbstractInstrumentationTest extends TestBase {

	protected Object createInstance(String className, byte[] bytes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return createClass(className, bytes).newInstance();
	}

	protected Class<?> createClass(String className, byte[] bytes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return new InstrumentingClassLoader().findClass(className, bytes);
	}

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
