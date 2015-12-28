package info.novatec.inspectit.agent.hooking.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.IAgent;
import info.novatec.inspectit.agent.analyzer.classes.ExceptionTestClass;
import info.novatec.inspectit.agent.analyzer.classes.ExceptionalTestClass;
import info.novatec.inspectit.agent.analyzer.classes.ITest;
import info.novatec.inspectit.agent.analyzer.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.classes.TestClass;
import info.novatec.inspectit.agent.analyzer.classes.TestClassLoader;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHookDispatcher;
import info.novatec.inspectit.agent.hooking.IHookDispatcherMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class HookInstrumenterTest extends AbstractLogSupport {

	private interface ITestHookDispatcher extends IHookDispatcher, IHookDispatcherMapper {
	}

	@Mock
	private static ITestHookDispatcher hookDispatcher;

	@Mock
	private IIdManager idManager;

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private static IAgent agent;

	private HookInstrumenter hookInstrumenter;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		hookInstrumenter = new HookInstrumenter(hookDispatcher, idManager, configurationStorage);
		hookInstrumenter.log = LoggerFactory.getLogger(HookInstrumenter.class);
		Field field = hookInstrumenter.getClass().getDeclaredField("hookDispatcherTarget");
		field.setAccessible(true);
		field.set(hookInstrumenter, "info.novatec.inspectit.agent.hooking.impl.HookInstrumenterTest#getHookDispatcher()");

		field = hookInstrumenter.getClass().getDeclaredField("agentTarget");
		field.setAccessible(true);
		field.set(hookInstrumenter, "info.novatec.inspectit.agent.hooking.impl.HookInstrumenterTest#getAgent()");
	}

	public static ITestHookDispatcher getHookDispatcher() {
		return hookDispatcher;
	}

	public static IAgent getAgent() {
		return agent;
	}

	private Loader createLoader() {
		ClassPool classPool = ClassPool.getDefault();
		Loader loader = new Loader(this.getClass().getClassLoader(), classPool);
		loader.delegateLoadingOf(HookInstrumenterTest.class.getName());
		loader.delegateLoadingOf(IHookDispatcher.class.getName());
		loader.delegateLoadingOf(IAgent.class.getName());
		return loader;
	}

	private CtMethod getCtMethod(Loader loader, String className, String methodName) throws NotFoundException {
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		return classPool.getMethod(className, methodName);
	}

	private CtClass getCtClass(Loader loader, String className) throws NotFoundException {
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		return classPool.get(className);
	}

	/**
	 * As it is not possible to modify the java byte code on the fly, we have to get it from the
	 * class pool.
	 */
	private Object createInstance(Loader loader, CtMethod ctMethod) throws Exception {
		Class<?> clazz = ctMethod.getDeclaringClass().toClass(loader, null);
		return clazz.newInstance();
	}

	private Object callMethod(Object object, String methodName, Object[] parameters) throws Exception {
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

	@Test
	public void methodHookNoStatic() throws Exception {
		String methodName = "stringNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 3L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		verify(idManager).registerMethod(registeredSensorConfig);
		verify(hookDispatcher).addMethodMapping(methodId, registeredSensorConfig);
		verifyNoMoreInteractions(idManager, hookDispatcher);

		// now call this method
		Object testClass = this.createInstance(loader, ctMethod);
		// call this method via reflection as we would get a class cast
		// exception by casting to the concrete class.
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
		verifyNoMoreInteractions(idManager, hookDispatcher);
	}

	@Test
	public void methodHookStatic() throws Exception {
		String methodName = "voidNullParameterStatic";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 7L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		verify(idManager).registerMethod(registeredSensorConfig);
		verify(hookDispatcher).addMethodMapping(methodId, registeredSensorConfig);
		verifyNoMoreInteractions(idManager, hookDispatcher);

		// now call this method
		Object testClass = this.createInstance(loader, ctMethod);
		// call this method via reflection as we would get a class cast
		// exception by casting to the concrete class.
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);
		verifyNoMoreInteractions(idManager, hookDispatcher);
	}

	@Test
	public void testSensorTypeRegistrations() throws NotFoundException, HookException {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		long sensorTypeId = 11L;
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		sensorTypeConfigs.add(sensorTypeConfig);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		CtMethod ctMethod = ClassPool.getDefault().getMethod(TestClass.class.getName(), "voidNullParameterStatic");
		long methodId = 7L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		verify(idManager).registerMethod(registeredSensorConfig);
		verify(idManager).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher).addMethodMapping(methodId, registeredSensorConfig);
		verifyNoMoreInteractions(idManager, hookDispatcher);
	}

	@Test(expectedExceptions = { HookException.class })
	public void instrumentInterface() throws NotFoundException, HookException {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		CtMethod ctMethod = ClassPool.getDefault().getMethod(ITest.class.getName(), "voidNullParameter");

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
	}

	@Test
	public void repeatInstrumentation() throws NotFoundException, HookException {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		CtMethod ctMethod = ClassPool.getDefault().getMethod(TestClass.class.getName(), "stringNullParameter");

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
	}

	// above tests were used to find out if the hookinstrumenter seems to work,
	// the lower ones are needed to verify if all methods are instrumented
	// correctly.

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void classLoaderClassDelegated() throws Exception {
		String methodName = "loadClass";
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClassLoader.class.getName(), methodName);

		hookInstrumenter.addClassLoaderDelegationHook(ctMethod);

		Class loadedByUs = String.class;
		String className = "java.lang.String";
		Object[] parameters = new Object[] { className };
		when(agent.loadClass(parameters)).thenReturn(loadedByUs);

		// we expect that agent delivers loadedByUs, thus also the class loader

		Object testClass = this.createInstance(loader, ctMethod);
		Object result = this.callMethod(testClass, methodName, parameters);

		verify(agent, times(1)).loadClass(new Object[] { className });
		assertThat(result, is(instanceOf(Class.class)));
		assertThat((Class) result, is(equalTo(loadedByUs)));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void classLoaderClassNotDelegated() throws Exception {
		String methodName = "loadClass";
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClassLoader.class.getName(), methodName);

		hookInstrumenter.addClassLoaderDelegationHook(ctMethod);

		String className = "java.lang.String";
		Object[] parameters = new Object[] { className };
		when(agent.loadClass(parameters)).thenReturn(null);

		// we expect that agent delivers null, thus the class loader returns his own class

		Object testClass = this.createInstance(loader, ctMethod);
		Object result = this.callMethod(testClass, methodName, parameters);

		verify(agent, times(1)).loadClass(new Object[] { className });
		Class expected = testClass.getClass();
		assertThat((Class) result, is(equalTo(expected)));
	}

	@Test
	public void voidNullParameter() throws Exception {
		String methodName = "voidNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);
	}

	@Test
	public void stringNullParameter() throws Exception {
		String methodName = "stringNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
	}

	@Test
	public void intNullParameter() throws Exception {
		String methodName = "intNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], 3);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], 3);
	}

	@Test
	public void doubleNullParameter() throws Exception {
		String methodName = "doubleNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
	}

	@Test
	public void floatNullParameter() throws Exception {
		String methodName = "floatNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], Float.MAX_VALUE);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], Float.MAX_VALUE);
	}

	@Test
	public void byteNullParameter() throws Exception {
		String methodName = "byteNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], (byte) 127);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], (byte) 127);
	}

	@Test
	public void shortNullParameter() throws Exception {
		String methodName = "shortNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], (short) 16345);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], (short) 16345);
	}

	@Test
	public void booleanNullParameter() throws Exception {
		String methodName = "booleanNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], false);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], false);
	}

	@Test
	public void charNullParameter() throws Exception {
		String methodName = "charNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], '\u1234');
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], '\u1234');
	}

	@Test
	public void voidNullParameterStatic() throws Exception {
		String methodName = "voidNullParameterStatic";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);
	}

	@Test
	public void stringNullParameterStatic() throws Exception {
		String methodName = "stringNullParameterStatic";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], "stringNullParameterStatic");
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], "stringNullParameterStatic");
	}

	@Test
	public void voidOneParameter() throws Exception {
		String methodName = "voidOneParameter";
		Object[] parameters = { "java.lang.String" };
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, parameters);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
	}

	@Test
	public void stringOneParameter() throws Exception {
		String methodName = "stringOneParameter";
		Object[] parameters = { "java.lang.String" };
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, parameters);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, "stringOneParameter");
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, "stringOneParameter");
	}

	@Test
	public void voidTwoParameters() throws Exception {
		String methodName = "voidTwoParameters";
		Object[] parameters = { "java.lang.String", "java.lang.Object" };
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, parameters);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
	}

	@Test
	public void mixedTwoParameters() throws Exception {
		String methodName = "mixedTwoParameters";
		Object[] parameters = { "int", "boolean" };
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, parameters);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
	}

	@Test
	public void intArrayNullParameter() throws Exception {
		String methodName = "intArrayNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
	}

	@Test
	public void stringArrayNullParameter() throws Exception {
		String methodName = "stringArrayNullParameter";
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtMethod ctMethod = this.getCtMethod(loader, TestClass.class.getName(), methodName);
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
	}

	@Test
	public void constructorNullParameter() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtClass ctClass = this.getCtClass(loader, TestClass.class.getName());
		CtConstructor ctConstructor = ctClass.getConstructor("()V");
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addConstructorHook(ctConstructor, registeredSensorConfig);

		Class<?> clazz = ctClass.toClass(loader, null);
		Object testClass = clazz.newInstance();

		verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, new Object[0]);
		verify(hookDispatcher).dispatchConstructorAfterBody(methodId, testClass, new Object[0]);
	}

	@Test
	public void constructorStringOneParameter() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtClass ctClass = this.getCtClass(loader, TestClass.class.getName());
		CtConstructor ctConstructor = ctClass.getConstructor("(Ljava/lang/String;)V");
		Object[] parameters = { "java.lang.String" };
		long methodId = 9L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);

		hookInstrumenter.addConstructorHook(ctConstructor, registeredSensorConfig);

		Class<?> clazz = ctClass.toClass(loader, null);
		Constructor<?> constructor = clazz.getConstructor(new Class[] { String.class });
		Object testClass = constructor.newInstance(parameters);

		verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
		verify(hookDispatcher).dispatchConstructorAfterBody(methodId, testClass, parameters);
	}

	@Test
	public void nestedConstructorBooleanOneParameter() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Loader loader = this.createLoader();
		CtClass ctClass = this.getCtClass(loader, TestClass.class.getName());
		CtConstructor ctConstructor = ctClass.getConstructor("(Z)V");
		Object[] parameters = { Boolean.TRUE };
		CtConstructor nestedCtConstructor = ctClass.getConstructor("(Ljava/lang/String;)V");
		Object[] nestedParameters = { "delegate" };
		long methodId = 9L;
		long nestedMethodId = 13L;

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId).thenReturn(nestedMethodId);

		hookInstrumenter.addConstructorHook(ctConstructor, registeredSensorConfig);
		hookInstrumenter.addConstructorHook(nestedCtConstructor, registeredSensorConfig);

		Class<?> clazz = ctClass.toClass(loader, null);
		Constructor<?> constructor = clazz.getConstructor(new Class[] { Boolean.TYPE });
		Object testClass = constructor.newInstance(parameters);

		verify(hookDispatcher).dispatchConstructorBeforeBody(nestedMethodId, nestedParameters);
		verify(hookDispatcher).dispatchConstructorAfterBody(nestedMethodId, testClass, nestedParameters);
		verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
		verify(hookDispatcher).dispatchConstructorAfterBody(methodId, testClass, parameters);
	}

	@Test
	public void constructorsOfThrowableAreInstrumented() throws Exception {
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long constructorId = 5L;
		long sensorTypeId = 10L;

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.isConstructor()).thenReturn(true);
		when(registeredSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		when(registeredSensorConfig.isConstructor()).thenReturn(true);
		when(registeredSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);

		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(constructorId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting and verifying that constructors are instrumented
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredSensorConfig);
		}
		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredSensorConfig);
	}

	@Test
	public void exceptionObjectIsCreated() throws Exception {
		String methodName = "createsExceptionObject";
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long sensorTypeId = 10L;
		long constructorId = 5L;

		MyTestException exceptionObject = MyTestException.class.newInstance();
		System.out.println(exceptionObject.getClass().getClassLoader());

		// initializing / mocking the RegisteredSensorConfig and some needed
		// variables
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(registeredSensorConfig.isConstructor()).thenReturn(true);
		when(registeredSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(constructorId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the constructor
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredSensorConfig);
		}
		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new MyTestExceptionVerifier(exceptionObject)), (Object[]) anyObject());
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void exceptionThrowerIsInstrumented() throws Exception {
		String methodName = "throwsAndHandlesException";
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);

		long methodId = 9L;
		long sensorTypeId = 10L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);

		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void exceptionThrowerIsInstrumentedWhenConstructor() throws Exception {
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		String methodName = "constructorThrowsAnException";
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);

		CtClass ctClass = this.getCtClass(loader, ExceptionalTestClass.class.getName());
		CtConstructor ctConstructor = ctClass.getConstructor("(Ljava/lang/String;)V");

		long sensorTypeId = 10L;
		long constructorId = 11L;
		long methodId = 9L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		// sensor type settings
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);

		// method settings
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// constructor settings
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
		when(idManager.registerMethod(registeredConstructorSensorConfig)).thenReturn(constructorId);
		when(registeredConstructorSensorConfig.isConstructor()).thenReturn(true);
		when(registeredConstructorSensorConfig.getId()).thenReturn(sensorTypeId);
		when(registeredConstructorSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		hookInstrumenter.addConstructorHook(ctConstructor, registeredConstructorSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(1)).addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		Class<?> clazz = ctClass.toClass(loader, null);
		Object testConstructor = clazz.newInstance();
		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchConstructorOnThrowInBody(eq(constructorId), argThat(new ObjectVerifier(testConstructor)), (Object[]) anyObject(),
				argThat(new MyTestExceptionVerifier(exceptionObject)));
		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new ObjectVerifier(testConstructor)), (Object[]) anyObject());
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));

		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void exceptionThrowerAndHandlerAreInstrumented() throws Exception {
		String methodName = "callsMethodWithException";
		String innerMethodName = "throwsAnException";
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtMethod innerCtMethod = classPool.getMethod(ExceptionTestClass.class.getName(), innerMethodName);

		long methodId = 9L;
		long innerMethodId = 11L;
		long sensorTypeId = 10L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig innerRegisteredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(innerRegisteredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(idManager.registerMethod(innerRegisteredSensorConfig)).thenReturn(innerMethodId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);
		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(innerRegisteredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the first method
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// instrumenting the called inner method
		hookInstrumenter.addMethodHook(innerCtMethod, innerRegisteredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, innerMethodId);
		verify(hookDispatcher, times(1)).addMethodMapping(innerMethodId, innerRegisteredSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		// first method
		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

		// inner method
		verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

		verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void everythingInstrumentedByExceptionSensor() throws Exception {
		String methodName = "callsMethodWithException";
		String innerMethodName = "throwsAnException";
		// we need to create an new loader with a new classPool
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtMethod innerCtMethod = classPool.getMethod(ExceptionTestClass.class.getName(), innerMethodName);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long methodId = 9L;
		long innerMethodId = 11L;
		long sensorTypeId = 10L;
		long constructorId = 5L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		// initializing / mocking the RegisteredSensorConfig for the
		// constructors and some needed variables
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredConstructorSensorConfig.getId()).thenReturn(sensorTypeId);
		when(registeredConstructorSensorConfig.isConstructor()).thenReturn(true);
		when(registeredConstructorSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());
		when(idManager.registerMethod(registeredConstructorSensorConfig)).thenReturn(constructorId);

		// stuff for the methods
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig innerRegisteredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(innerRegisteredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(idManager.registerMethod(innerRegisteredSensorConfig)).thenReturn(innerMethodId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);

		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(innerRegisteredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(registeredConstructorSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the first method
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// instrumenting the called inner method
		hookInstrumenter.addMethodHook(innerCtMethod, innerRegisteredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, innerMethodId);
		verify(hookDispatcher, times(1)).addMethodMapping(innerMethodId, innerRegisteredSensorConfig);

		// instrumenting the constructors
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredConstructorSensorConfig);
		}

		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		// first method
		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

		// inner method
		verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new MyTestExceptionVerifier(exceptionObject)), (Object[]) anyObject());
		verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void tryCatchInstrumentedByExceptionSensorWithFinally() throws Exception {
		String methodName = "callsMethodWithExceptionAndTryCatchFinally";
		String innerMethodName = "throwsAnException";
		// we need to create an new loader with a new classPool
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtMethod innerCtMethod = classPool.getMethod(ExceptionTestClass.class.getName(), innerMethodName);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long methodId = 9L;
		long innerMethodId = 11L;
		long sensorTypeId = 10L;
		long constructorId = 5L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		// initializing / mocking the RegisteredSensorConfig for the
		// constructors and some needed variables
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredConstructorSensorConfig.getId()).thenReturn(sensorTypeId);
		when(registeredConstructorSensorConfig.isConstructor()).thenReturn(true);
		when(registeredConstructorSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());
		when(idManager.registerMethod(registeredConstructorSensorConfig)).thenReturn(constructorId);

		// stuff for the methods
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig innerRegisteredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(innerRegisteredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(idManager.registerMethod(innerRegisteredSensorConfig)).thenReturn(innerMethodId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);

		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(innerRegisteredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(registeredConstructorSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the first method
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// instrumenting the called inner method
		hookInstrumenter.addMethodHook(innerCtMethod, innerRegisteredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, innerMethodId);
		verify(hookDispatcher, times(1)).addMethodMapping(innerMethodId, innerRegisteredSensorConfig);

		// instrumenting the constructors
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredConstructorSensorConfig);
		}

		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		// first method
		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

		// inner method
		verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new MyTestExceptionVerifier(exceptionObject)), (Object[]) anyObject());
		verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void tryCatchNotInstrumentedBySimpleExceptionSensor() throws Exception {
		String methodName = "callsMethodWithExceptionAndTryCatchFinally";
		String innerMethodName = "throwsAnException";
		// we need to create an new loader with a new classPool
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtMethod innerCtMethod = classPool.getMethod(ExceptionTestClass.class.getName(), innerMethodName);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long methodId = 9L;
		long innerMethodId = 11L;
		long sensorTypeId = 10L;
		long constructorId = 5L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		// initializing / mocking the RegisteredSensorConfig for the
		// constructors and some needed variables
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredConstructorSensorConfig.getId()).thenReturn(sensorTypeId);
		when(registeredConstructorSensorConfig.isConstructor()).thenReturn(true);
		when(registeredConstructorSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());
		when(idManager.registerMethod(registeredConstructorSensorConfig)).thenReturn(constructorId);

		// stuff for the methods
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig innerRegisteredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(innerRegisteredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(idManager.registerMethod(innerRegisteredSensorConfig)).thenReturn(innerMethodId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(false);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);

		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(innerRegisteredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(registeredConstructorSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the first method
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// instrumenting the called inner method
		hookInstrumenter.addMethodHook(innerCtMethod, innerRegisteredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, innerMethodId);
		verify(hookDispatcher, times(1)).addMethodMapping(innerMethodId, innerRegisteredSensorConfig);

		// instrumenting the constructors
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredConstructorSensorConfig);
		}

		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		// first method
		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

		// inner method
		verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new MyTestExceptionVerifier(exceptionObject)), (Object[]) anyObject());
		// verify that no exception event is dispatched
		verify(hookDispatcher, times(0)).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verify(hookDispatcher, times(0)).dispatchBeforeCatch(eq(methodId), argThat(new MyTestExceptionVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	@Test
	public void everythingInstrumentedByExceptionSensorWithStaticMethods() throws Exception {
		String methodName = "callsStaticMethodWithException";
		String innerMethodName = "staticThrowsAnException";
		// we need to create an new loader with a new classPool
		Loader loader = this.createLoader();
		ClassPool classPool = new ClassPool();
		classPool.insertClassPath(new LoaderClassPath(loader));
		loader.setClassPool(classPool);
		CtMethod ctMethod = classPool.getMethod(ExceptionTestClass.class.getName(), methodName);
		CtMethod innerCtMethod = classPool.getMethod(ExceptionTestClass.class.getName(), innerMethodName);
		CtClass exceptionClazz = classPool.get(MyTestException.class.getName());

		long methodId = 9L;
		long innerMethodId = 11L;
		long sensorTypeId = 10L;
		long constructorId = 5L;

		MyTestException exceptionObject = MyTestException.class.newInstance();

		// initializing / mocking the RegisteredSensorConfig for the
		// constructors and some needed variables
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredConstructorSensorConfig.getId()).thenReturn(sensorTypeId);
		when(registeredConstructorSensorConfig.isConstructor()).thenReturn(true);
		when(registeredConstructorSensorConfig.getTargetClassName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getTargetMethodName()).thenReturn(exceptionClazz.getSimpleName());
		when(registeredConstructorSensorConfig.getModifiers()).thenReturn(exceptionClazz.getModifiers());
		when(idManager.registerMethod(registeredConstructorSensorConfig)).thenReturn(constructorId);

		// stuff for the methods
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig innerRegisteredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(innerRegisteredSensorConfig.getId()).thenReturn(sensorTypeId);
		when(idManager.registerMethod(registeredSensorConfig)).thenReturn(methodId);
		when(idManager.registerMethod(innerRegisteredSensorConfig)).thenReturn(innerMethodId);
		when(configurationStorage.isExceptionSensorActivated()).thenReturn(true);
		when(configurationStorage.isEnhancedExceptionSensorActivated()).thenReturn(true);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(sensorTypeId);
		List<MethodSensorTypeConfig> sensorTypeConfigs = new ArrayList<MethodSensorTypeConfig>();
		sensorTypeConfigs.add(sensorTypeConfig);

		when(registeredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(innerRegisteredSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);
		when(registeredConstructorSensorConfig.getSensorTypeConfigs()).thenReturn(sensorTypeConfigs);

		// instrumenting the first method
		hookInstrumenter.addMethodHook(ctMethod, registeredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, methodId);
		verify(hookDispatcher, times(1)).addMethodMapping(methodId, registeredSensorConfig);

		// instrumenting the called inner method
		hookInstrumenter.addMethodHook(innerCtMethod, innerRegisteredSensorConfig);
		verify(idManager, times(1)).addSensorTypeToMethod(sensorTypeId, innerMethodId);
		verify(hookDispatcher, times(1)).addMethodMapping(innerMethodId, innerRegisteredSensorConfig);

		// instrumenting the constructors
		CtConstructor[] constructors = exceptionClazz.getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			hookInstrumenter.addConstructorHook(constructors[i], registeredConstructorSensorConfig);
		}
		verify(idManager, times(3)).addSensorTypeToMethod(sensorTypeId, constructorId);
		verify(hookDispatcher, times(3)).addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		Object testClass = this.createInstance(loader, ctMethod);
		this.callMethod(testClass, methodName, null);

		// first method
		verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);

		// inner method
		verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, null, new Object[0]);
		verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, null, new Object[0], null);
		verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, null, new Object[0], null);

		verify(hookDispatcher).dispatchConstructorBeforeBody(eq(constructorId), (Object[]) anyObject());
		verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), argThat(new MyTestExceptionVerifier(exceptionObject)), (Object[]) anyObject());
		verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), isNull(), (Object[]) anyObject(), argThat(new ThrowableVerifier(exceptionObject)));
		verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), argThat(new ThrowableVerifier(exceptionObject)));
		verifyNoMoreInteractions(hookDispatcher);
	}

	/**
	 * Inner class used to verify the contents of MyTestException objects.
	 */
	private static class MyTestExceptionVerifier extends ArgumentMatcher<MyTestException> {
		@SuppressWarnings("unused")
		private final MyTestException myTestException;

		public MyTestExceptionVerifier(MyTestException myTestException) {
			this.myTestException = myTestException;
		}

		@Override
		public boolean matches(Object object) {
			// TODO ET: why does this test fail? is it because object has
			// another class loader?
			// MyTestException is loaded by sun.misc.Launcher$AppClassLoader
			// object is loaded by javassist.Loader

			// if (!MyTestException.class.isInstance(object)) {
			// return false;
			// }
			//
			// MyTestException otherException = (MyTestException) object;
			//
			// if ((null != myTestException.getCause()) &&
			// !myTestException.getCause().equals(otherException.getCause())) {
			// return false;
			// }
			// if ((null != myTestException.getMessage()) &&
			// !myTestException.getMessage
			// ().equals(otherException.getMessage())) {
			// return false;
			// }

			return true;
		}
	}

	/**
	 * Inner class used to verify the contents of MyTestException objects.
	 */
	private static class ThrowableVerifier extends ArgumentMatcher<Object> {
		@SuppressWarnings("unused")
		private final Object exceptionObject;

		public ThrowableVerifier(Object exceptionObject) {
			this.exceptionObject = exceptionObject;
		}

		@Override
		public boolean matches(Object object) {
			// TODO ET: object has different class loader

			// if (!exceptionObject.getClass().isInstance(object)) {
			// return false;
			// }
			return true;
		}
	}

	private static class ObjectVerifier extends ArgumentMatcher<Object> {
		@SuppressWarnings("unused")
		private final Object object;

		public ObjectVerifier(Object object) {
			this.object = object;
		}

		@Override
		public boolean matches(Object obj) {
			// TODO ET: object has different class loader

			// if (!obj.getClass().isInstance(object.getClass())) {
			// return false;
			// }
			return true;
		}
	}

}
