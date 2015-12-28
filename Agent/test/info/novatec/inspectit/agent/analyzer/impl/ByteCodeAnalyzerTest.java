package info.novatec.inspectit.agent.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.classes.TestClass;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.agent.hooking.IHookInstrumenter;
import info.novatec.inspectit.agent.hooking.impl.HookException;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensor;
import info.novatec.inspectit.agent.sensor.exception.IExceptionSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.communication.data.ParameterContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class ByteCodeAnalyzerTest extends AbstractLogSupport {

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IHookInstrumenter hookInstrumenter;

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private ByteCodeAnalyzer byteCodeAnalyzer;

	@BeforeMethod
	public void initTestClass() {
		byteCodeAnalyzer = new ByteCodeAnalyzer(configurationStorage, hookInstrumenter, classPoolAnalyzer);
		byteCodeAnalyzer.log = LoggerFactory.getLogger(ByteCodeAnalyzer.class);
		when(configurationStorage.getClassLoaderDelegationMatchers()).thenReturn(Collections.singleton(mock(IMatcher.class)));
	}

	private byte[] getByteCode(String className) throws NotFoundException, IOException, CannotCompileException {
		CtClass ctClass = ClassPool.getDefault().get(className);
		return ctClass.toBytecode();
	}

	@Test
	public void nothingToDo() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		// as no instrumentation happened, we get a null object
		assertThat(instrumentedByteCode, is(nullValue()));
	}

	@Test
	public void simpleClassAndMethod() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		String methodName = "voidNullParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);
		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);
		unregisteredSensorConfigs.add(unregisteredSensorConfig);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		assertThat(instrumentedByteCode, is(notNullValue()));
		// nothing was really instrumented, thus the byte code has to be the
		// same
		assertThat(instrumentedByteCode, is(equalTo(byteCode)));
	}

	@Test
	public void removeReturnValueCapturingForVoidReturnMethods() throws NotFoundException, IOException, CannotCompileException, HookException {
		String className = TestClass.class.getName();
		String methodName = "voidNullParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");

		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);

		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);

		List<PropertyPathStart> propertyAccessors = new ArrayList<PropertyAccessor.PropertyPathStart>();
		PropertyPathStart path = new PropertyPathStart();
		path.setContentType(ParameterContentType.RETURN);
		path.setName("returnValue");
		propertyAccessors.add(path);
		when(unregisteredSensorConfig.getPropertyAccessorList()).thenReturn(propertyAccessors);
		when(unregisteredSensorConfig.isPropertyAccess()).thenReturn(true);

		unregisteredSensorConfigs.add(unregisteredSensorConfig);
		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		ArgumentCaptor<RegisteredSensorConfig> capturedRegisteredSensorConfig = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
		Mockito.verify(hookInstrumenter).addMethodHook(Mockito.any(CtMethod.class), capturedRegisteredSensorConfig.capture());
		assertThat(capturedRegisteredSensorConfig.getValue().getPropertyAccessorList(), is(empty()));
		assertThat(capturedRegisteredSensorConfig.getValue().isPropertyAccess(), is(false));
	}

	@Test
	public void removeReturnValueCapturingForVoidReturnMethodsButKeepOthers() throws NotFoundException, IOException, CannotCompileException, HookException {
		String className = TestClass.class.getName();
		String methodName = "voidNullParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");

		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);

		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);

		List<PropertyPathStart> propertyAccessors = new ArrayList<PropertyAccessor.PropertyPathStart>();

		PropertyPathStart path = new PropertyPathStart();
		path.setContentType(ParameterContentType.RETURN);
		path.setName("returnValue");
		propertyAccessors.add(path);

		path = new PropertyPathStart();
		path.setContentType(ParameterContentType.PARAM);
		path.setName("returnValue");
		propertyAccessors.add(path);
		when(unregisteredSensorConfig.getPropertyAccessorList()).thenReturn(propertyAccessors);
		when(unregisteredSensorConfig.isPropertyAccess()).thenReturn(true);

		unregisteredSensorConfigs.add(unregisteredSensorConfig);
		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		ArgumentCaptor<RegisteredSensorConfig> capturedRegisteredSensorConfig = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
		Mockito.verify(hookInstrumenter).addMethodHook(Mockito.any(CtMethod.class), capturedRegisteredSensorConfig.capture());
		assertThat(capturedRegisteredSensorConfig.getValue().getPropertyAccessorList(), hasSize(1));
		assertThat(capturedRegisteredSensorConfig.getValue().isPropertyAccess(), is(true));
		assertThat(capturedRegisteredSensorConfig.getValue().getPropertyAccessorList(), contains(path));
	}

	@Test
	public void noRemovalOfReturnValueCapturingForNonVoidReturnMethods() throws NotFoundException, IOException, CannotCompileException, HookException {
		String className = TestClass.class.getName();
		String methodName = "stringNullParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");

		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);

		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);

		List<PropertyPathStart> propertyAccessors = new ArrayList<PropertyAccessor.PropertyPathStart>();

		PropertyPathStart path = new PropertyPathStart();
		path.setContentType(ParameterContentType.RETURN);
		path.setName("returnValue");
		propertyAccessors.add(path);

		path = new PropertyPathStart();
		path.setContentType(ParameterContentType.PARAM);
		path.setName("returnValue");
		propertyAccessors.add(path);
		when(unregisteredSensorConfig.getPropertyAccessorList()).thenReturn(propertyAccessors);
		when(unregisteredSensorConfig.isPropertyAccess()).thenReturn(true);

		unregisteredSensorConfigs.add(unregisteredSensorConfig);
		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		ArgumentCaptor<RegisteredSensorConfig> capturedRegisteredSensorConfig = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
		Mockito.verify(hookInstrumenter).addMethodHook(Mockito.any(CtMethod.class), capturedRegisteredSensorConfig.capture());
		assertThat(capturedRegisteredSensorConfig.getValue().getPropertyAccessorList(), hasSize(2));
		assertThat(capturedRegisteredSensorConfig.getValue().isPropertyAccess(), is(true));
	}

	@Test
	public void methodWithOneParameter() throws NotFoundException, IOException, CannotCompileException {
		String className = TestClass.class.getName();
		String methodName = "voidOneParameter";
		ClassLoader classLoader = TestClass.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig unregisteredSensorConfig = mock(UnregisteredSensorConfig.class);
		when(unregisteredSensorConfig.getTargetClassName()).thenReturn(className);
		when(unregisteredSensorConfig.getTargetMethodName()).thenReturn(methodName);
		MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(methodSensorTypeConfig.getClassName()).thenReturn("");
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(methodSensorTypeConfig.getSensorType()).thenReturn(methodSensor);
		when(unregisteredSensorConfig.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);
		IMatcher matcher = mock(IMatcher.class);
		List<CtMethod> ctMethods = new ArrayList<CtMethod>();
		ctMethods.add(ClassPool.getDefault().getMethod(className, methodName));
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(ctMethods);
		when(unregisteredSensorConfig.getMatcher()).thenReturn(matcher);
		unregisteredSensorConfigs.add(unregisteredSensorConfig);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(unregisteredSensorConfigs);

		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		assertThat(instrumentedByteCode, is(notNullValue()));
		// nothing was really instrumented, thus the byte code has to be the
		// same
		assertThat(instrumentedByteCode, is(equalTo(byteCode)));
	}

	@Test
	public void exceptionSensorNotActivated() throws NotFoundException, IOException, CannotCompileException {
		String className = MyTestException.class.getName();
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		when(configurationStorage.isExceptionSensorActivated()).thenReturn(false);
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		// actual class was not a subclass of Throwable, so nothing to
		// instrument
		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
		assertThat(instrumentedByteCode, is(nullValue()));
	}

	@Test
	public void exceptionSensorActivated() throws NotFoundException, IOException, CannotCompileException {
		String className = MyTestException.class.getName();
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		when(inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool)).thenReturn(true);
		IExceptionSensor exceptionSensor = mock(ExceptionSensor.class);

		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn(exceptionSensor.getClass().getName());
		when(sensorTypeConfig.getClassName()).thenReturn(exceptionSensor.getClass().getName());
		when(sensorTypeConfig.getSensorType()).thenReturn(exceptionSensor);
		List<MethodSensorTypeConfig> exceptionSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		exceptionSensorTypes.add(sensorTypeConfig);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(exceptionSensorTypes);
		when(configurationStorage.getExceptionSensorTypes()).thenReturn(exceptionSensorTypes);
		IMatcher superclassMatcher = mock(IMatcher.class);
		when(superclassMatcher.compareClassName(classLoader, className)).thenReturn(true);

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("superclass", "true");

		List<UnregisteredSensorConfig> exceptionSensorConfigs = new ArrayList<UnregisteredSensorConfig>();
		UnregisteredSensorConfig config = mock(UnregisteredSensorConfig.class);
		when(config.isConstructor()).thenReturn(true);
		when(config.isInterface()).thenReturn(false);
		when(config.isSuperclass()).thenReturn(true);
		when(config.isVirtual()).thenReturn(false);
		when(config.isIgnoreSignature()).thenReturn(true);
		when(config.getSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(config.getSettings()).thenReturn(settings);
		ThrowableMatcher matcher = new ThrowableMatcher(inheritanceAnalyzer, classPoolAnalyzer, config, superclassMatcher);
		when(config.getMatcher()).thenReturn(matcher);
		exceptionSensorConfigs.add(config);

		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(exceptionSensorConfigs);

		// exception sensor was activated, so the current Throwable class was
		// instrumented
		byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
		assertThat(instrumentedByteCode, is(notNullValue()));
	}

	@Test
	public void classLoaderInstumentation() throws NotFoundException, IOException, CannotCompileException, HookException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		Class<?> classLoaderClass = classLoader.getClass();
		String className = classLoaderClass.getName();
		byte[] byteCode = getByteCode(className);

		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);
		when(configurationStorage.getUnregisteredSensorConfigs()).thenReturn(Collections.<UnregisteredSensorConfig> emptyList());
		List<CtMethod> methodList = new ArrayList<CtMethod>();
		methodList.add(classPool.getMethod(className, "loadClass"));

		IMatcher matcher = mock(IMatcher.class);
		when(matcher.compareClassName(classLoader, className)).thenReturn(true);
		when(matcher.getMatchingMethods(classLoader, className)).thenReturn(methodList);
		when(configurationStorage.getClassLoaderDelegationMatchers()).thenReturn(Collections.singleton(matcher));

		byteCodeAnalyzer.classLoaderDelegation = true;
		byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		verify(hookInstrumenter, times(1)).addClassLoaderDelegationHook(methodList.get(0));

		byteCodeAnalyzer.classLoaderDelegation = false;
		byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

		verifyNoMoreInteractions(hookInstrumenter);
	}
}
