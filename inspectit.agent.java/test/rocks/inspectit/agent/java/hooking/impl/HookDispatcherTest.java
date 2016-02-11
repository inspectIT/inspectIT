package info.novatec.inspectit.agent.hooking.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.analyzer.classes.MyTestException;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensorHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceHook;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class HookDispatcherTest extends AbstractLogSupport {

	@Mock
	private ICoreService coreService;

	private HookDispatcher hookDispatcher;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		hookDispatcher = new HookDispatcher(coreService);
		hookDispatcher.log = LoggerFactory.getLogger(HookDispatcher.class);
	}

	@Test
	public void dispatchNoMethodHooks() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();

		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();

		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(registeredSensorConfig);
	}

	@Test
	public void dispatchOneMethodHookWithoutInvocationTrace() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHook = mock(IMethodHook.class);
		long sensorTypeId = 7L;
		methodHooks.put(sensorTypeId, methodHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(methodHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);

		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(registeredSensorConfig, methodHook);
	}

	@Test
	public void dispatchManyMethodHooksWithoutInvocationTrace() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		Map<Long, IHook> reverseMethodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHookOne = mock(IMethodHook.class);
		IMethodHook methodHookTwo = mock(IMethodHook.class);
		IMethodHook methodHookThree = mock(IMethodHook.class);
		long sensorTypeIdOne = 7L;
		long sensorTypeIdTwo = 13L;
		long sensorTypeIdThree = 15L;
		methodHooks.put(sensorTypeIdOne, methodHookOne);
		methodHooks.put(sensorTypeIdTwo, methodHookTwo);
		methodHooks.put(sensorTypeIdThree, methodHookThree);
		reverseMethodHooks.put(sensorTypeIdThree, methodHookThree);
		reverseMethodHooks.put(sensorTypeIdTwo, methodHookTwo);
		reverseMethodHooks.put(sensorTypeIdOne, methodHookOne);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(reverseMethodHooks);

		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		InOrder inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookThree, times(1)).beforeBody(methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).beforeBody(methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
		inOrder.verify(methodHookOne, times(1)).beforeBody(methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);

		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookOne, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookThree, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(registeredSensorConfig, methodHookOne);
	}

	@Test
	public void dispatchOneMethodHookWithInvocationTrace() {
		// create registered sensor configuration which starts an invocation
		// sequence
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.startsInvocationSequence()).thenReturn(true);
		MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
		when(registeredSensorConfig.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(invocSensorType.getSensorType()).thenReturn(methodSensor);
		long invocSensorTypeId = 13L;
		InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
		when(methodSensor.getHook()).thenReturn(invocHook);

		// create method hooks map
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHook = mock(IMethodHook.class);
		long methodSensorTypeId = 7L;
		methodHooks.put(methodSensorTypeId, methodHook);
		methodHooks.put(invocSensorTypeId, invocHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(methodHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);

		long methodId = 3L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);

		// map the first method
		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);

		RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> methodHooksTwo = new LinkedHashMap<Long, IHook>();
		methodHooksTwo.put(methodSensorTypeId, methodHook);
		when(registeredSensorConfigTwo.getReverseMethodHooks()).thenReturn(methodHooksTwo);
		when(registeredSensorConfigTwo.getMethodHooks()).thenReturn(methodHooksTwo);
		long methodIdTwo = 15L;
		// map the second method
		hookDispatcher.addMethodMapping(methodIdTwo, registeredSensorConfigTwo);

		// ////////////////////////////////////////////////////////
		// FIRST METHOD DISPATCHER

		// dispatch the first method - before body
		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(registeredSensorConfig, times(1)).getInvocationSequenceSensorTypeConfig();
		verify(methodSensor, times(1)).getHook();
		verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
		verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);
		verify(invocSensorType, times(1)).getSensorType();

		// ////////////////////////////////////////////////////////
		// SECOND METHOD DISPATCHER

		// dispatch the second method - before body
		hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(methodSensor, times(1)).getHook();
		verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
		verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));
		verify(invocSensorType, times(1)).getSensorType();

		// dispatch the second method - first after body
		hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
		verify(registeredSensorConfigTwo, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

		// dispatch the second method - second after body
		hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
		verify(registeredSensorConfigTwo, times(2)).startsInvocationSequence();
		verify(registeredSensorConfigTwo, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
		verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));

		// END SECOND METHOD DISPATCHER
		// ////////////////////////////////////////////////////////

		// dispatch the first method - first after body
		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
		verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// dispatch the first method - second after body
		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
		verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// END FIRST METHOD DISPATCHER
		// ////////////////////////////////////////////////////////

		// verify that no further interactions happened
		verifyZeroInteractions(object, coreService, returnValue, invocHook);
		verifyNoMoreInteractions(registeredSensorConfig, methodHook, methodSensor, invocSensorType);
	}

	@Test
	public void dispatchNoConstructorHooks() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];

		hookDispatcher.addConstructorMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();

		hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getMethodHooks();

		verifyZeroInteractions(object, coreService);
		verifyNoMoreInteractions(registeredSensorConfig);
	}

	@Test
	public void dispatchOneConstructorHookWithoutInvocationTrace() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> constructorHooks = new LinkedHashMap<Long, IHook>();
		IConstructorHook constructorHook = mock(IConstructorHook.class);
		long sensorTypeId = 7L;
		constructorHooks.put(sensorTypeId, constructorHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(constructorHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(constructorHooks);

		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];

		hookDispatcher.addConstructorMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(constructorHook, times(1)).beforeConstructor(methodId, sensorTypeId, parameters, registeredSensorConfig);

		hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(constructorHook, times(1)).afterConstructor(coreService, methodId, sensorTypeId, object, parameters, registeredSensorConfig);

		verifyZeroInteractions(object, coreService);
		verifyNoMoreInteractions(registeredSensorConfig, constructorHook);
	}

	@Test
	public void dispatchManyConstructorHooksWithoutInvocationTrace() {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> constructorHooks = new LinkedHashMap<Long, IHook>();
		Map<Long, IHook> reverseConstructorHooks = new LinkedHashMap<Long, IHook>();
		IConstructorHook constructorHookOne = mock(IConstructorHook.class);
		IConstructorHook constructorHookTwo = mock(IConstructorHook.class);
		IConstructorHook constructorHookThree = mock(IConstructorHook.class);
		long sensorTypeIdOne = 7L;
		long sensorTypeIdTwo = 13L;
		long sensorTypeIdThree = 15L;
		constructorHooks.put(sensorTypeIdOne, constructorHookOne);
		constructorHooks.put(sensorTypeIdTwo, constructorHookTwo);
		constructorHooks.put(sensorTypeIdThree, constructorHookThree);
		reverseConstructorHooks.put(sensorTypeIdThree, constructorHookThree);
		reverseConstructorHooks.put(sensorTypeIdTwo, constructorHookTwo);
		reverseConstructorHooks.put(sensorTypeIdOne, constructorHookOne);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(constructorHooks);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(reverseConstructorHooks);

		int methodId = 3;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];

		hookDispatcher.addConstructorMapping(methodId, registeredSensorConfig);

		hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		InOrder inOrder = inOrder(constructorHookOne, constructorHookTwo, constructorHookThree);
		inOrder.verify(constructorHookThree, times(1)).beforeConstructor(methodId, sensorTypeIdThree, parameters, registeredSensorConfig);
		inOrder.verify(constructorHookTwo, times(1)).beforeConstructor(methodId, sensorTypeIdTwo, parameters, registeredSensorConfig);
		inOrder.verify(constructorHookOne, times(1)).beforeConstructor(methodId, sensorTypeIdOne, parameters, registeredSensorConfig);

		hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		inOrder = inOrder(constructorHookOne, constructorHookTwo, constructorHookThree);
		inOrder.verify(constructorHookOne, times(1)).afterConstructor(coreService, methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);
		inOrder.verify(constructorHookTwo, times(1)).afterConstructor(coreService, methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
		inOrder.verify(constructorHookThree, times(1)).afterConstructor(coreService, methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);

		verifyZeroInteractions(object, coreService);
		verifyNoMoreInteractions(registeredSensorConfig, constructorHookOne);
	}

	@Test
	public void dispatchOneConstructorHookWithInvocationTrace() {
		// create registered sensor configuration which starts an invocation
		// sequence
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		when(registeredSensorConfig.startsInvocationSequence()).thenReturn(true);
		MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
		when(registeredSensorConfig.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);
		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(invocSensorType.getSensorType()).thenReturn(methodSensor);
		long invocSensorTypeId = 13L;
		InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
		when(methodSensor.getHook()).thenReturn(invocHook);

		// create method hooks map
		Map<Long, IHook> hooks = new LinkedHashMap<Long, IHook>();
		IConstructorHook constructorHook = mock(IConstructorHook.class);
		long methodSensorTypeId = 7L;
		hooks.put(invocSensorTypeId, invocHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(hooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(hooks);

		long methodId = 3L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);

		// map the first method
		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);

		RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> methodHooksTwo = new LinkedHashMap<Long, IHook>();
		methodHooksTwo.put(methodSensorTypeId, constructorHook);
		when(registeredSensorConfigTwo.getReverseMethodHooks()).thenReturn(methodHooksTwo);
		when(registeredSensorConfigTwo.getMethodHooks()).thenReturn(methodHooksTwo);
		long methodIdTwo = 15L;
		// map the second method
		hookDispatcher.addConstructorMapping(methodIdTwo, registeredSensorConfigTwo);

		// ////////////////////////////////////////////////////////
		// METHOD DISPATCHER

		// dispatch the first method - before body
		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(registeredSensorConfig, times(1)).getInvocationSequenceSensorTypeConfig();
		verify(methodSensor, times(1)).getHook();
		verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);
		verify(invocSensorType, times(1)).getSensorType();

		// ////////////////////////////////////////////////////////
		// CONSTRUCTOR DISPATCHER

		// dispatch the constructor - before constructor
		hookDispatcher.dispatchConstructorBeforeBody(methodIdTwo, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(methodSensor, times(1)).getHook();
		verify(constructorHook, times(1)).beforeConstructor(methodIdTwo, methodSensorTypeId, parameters, registeredSensorConfigTwo);
		verify((IConstructorHook) invocHook, times(1)).beforeConstructor(eq(methodIdTwo), anyLong(), eq(parameters), eq(registeredSensorConfigTwo));
		verify(invocSensorType, times(1)).getSensorType();

		// dispatch the constructor - after constructor
		hookDispatcher.dispatchConstructorAfterBody(methodIdTwo, object, parameters);
		verify(registeredSensorConfigTwo, times(2)).startsInvocationSequence();
		verify(registeredSensorConfigTwo, times(1)).getMethodHooks();
		verify(constructorHook, times(1)).afterConstructor(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
		verify((IConstructorHook) invocHook, times(1)).afterConstructor(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

		// END CONSTRUCTOR DISPATCHER
		// ////////////////////////////////////////////////////////

		// dispatch the method - first after body
		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// dispatch the method - second after body
		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// END METHOD DISPATCHER
		// ////////////////////////////////////////////////////////

		// verify that no further interactions happened
		verifyZeroInteractions(object, coreService, returnValue, invocHook);
		verifyNoMoreInteractions(registeredSensorConfig, constructorHook, methodSensor, invocSensorType);
	}

	@Test
	public void dispatchExceptionSensorWithOneMethodHookWithoutInvocationTrace() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);

		long sensorTypeId = 7L;
		long exceptionSensorTypeId = 10L;
		long methodId = 3L;
		long constructorId = 7L;

		// the exception sensor type config
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);

		// the exception sensor hook
		IMethodSensor exceptionSensor = mock(IMethodSensor.class);
		when(sensorTypeConfig.getSensorType()).thenReturn(exceptionSensor);
		ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
		when(exceptionSensor.getHook()).thenReturn(exceptionHook);

		// the map for the method hooks
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHook = mock(IMethodHook.class);
		// the map for the constructor hooks
		Map<Long, IHook> constructorHooks = new LinkedHashMap<Long, IHook>();
		methodHooks.put(sensorTypeId, methodHook);
		constructorHooks.put(exceptionSensorTypeId, exceptionHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(methodHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);

		when(registeredSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(registeredConstructorSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(registeredConstructorSensorConfig.getReverseMethodHooks()).thenReturn(constructorHooks);
		when(registeredConstructorSensorConfig.getMethodHooks()).thenReturn(constructorHooks);

		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);
		Object exceptionObject = mock(MyTestException.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);
		hookDispatcher.addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

		hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
		verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

		// first method of exception sensor
		hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
		verify(exceptionHook, times(1)).afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

		// second method of exception sensor
		hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
		verify(registeredSensorConfig, times(2)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// third method of exception sensor
		hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
		verify(registeredSensorConfig, times(4)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(methodHook, exceptionHook, registeredSensorConfig);
	}

	@Test
	public void dispatchExceptionSensorWithManyMethodHooksWithoutInvocationTrace() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);

		// the map for the method hooks
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		Map<Long, IHook> reverseMethodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHookOne = mock(IMethodHook.class);
		IMethodHook methodHookTwo = mock(IMethodHook.class);
		IMethodHook methodHookThree = mock(IMethodHook.class);

		// the map for the constructor hooks
		Map<Long, IHook> constructorHooks = new LinkedHashMap<Long, IHook>();

		long sensorTypeIdOne = 7L;
		long sensorTypeIdTwo = 13L;
		long sensorTypeIdThree = 15L;
		long exceptionSensorTypeId = 10L;
		long methodId = 3L;
		long constructorId = 1L;

		// the exception sensor type config
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);
		when(registeredSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(registeredConstructorSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);

		// the exception sensor hook
		IMethodSensor exceptionSensor = mock(IMethodSensor.class);
		when(sensorTypeConfig.getSensorType()).thenReturn(exceptionSensor);
		ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
		when(exceptionSensor.getHook()).thenReturn(exceptionHook);

		// putting the hooks into the maps
		constructorHooks.put(exceptionSensorTypeId, exceptionHook);
		methodHooks.put(sensorTypeIdOne, methodHookOne);
		methodHooks.put(sensorTypeIdTwo, methodHookTwo);
		methodHooks.put(sensorTypeIdThree, methodHookThree);
		reverseMethodHooks.put(sensorTypeIdThree, methodHookThree);
		reverseMethodHooks.put(sensorTypeIdTwo, methodHookTwo);
		reverseMethodHooks.put(sensorTypeIdOne, methodHookOne);

		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(reverseMethodHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);
		when(registeredConstructorSensorConfig.getReverseMethodHooks()).thenReturn(constructorHooks);
		when(registeredConstructorSensorConfig.getMethodHooks()).thenReturn(constructorHooks);

		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);
		Object exceptionObject = mock(MyTestException.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);
		hookDispatcher.addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		InOrder inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookThree, times(1)).beforeBody(methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).beforeBody(methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
		inOrder.verify(methodHookOne, times(1)).beforeBody(methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);

		hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
		verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

		// first method of exception sensor
		hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
		verify(exceptionHook, times(1)).afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

		// second method of exception sensor
		hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
		verify(registeredSensorConfig, times(2)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
		inOrder.verify(methodHookOne, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookTwo, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
		inOrder.verify(methodHookThree, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

		// third method of exception sensor
		hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
		verify(registeredSensorConfig, times(4)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(methodHookOne, methodHookTwo, methodHookThree, exceptionHook, registeredSensorConfig);
	}

	@Test
	public void dispatchExceptionSensorWithOneMethodHookWithInvocationTrace() throws Exception {
		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);

		long methodSensorTypeId = 23L;
		long exceptionSensorTypeId = 10L;
		long invocSensorTypeId = 13L;
		long methodId = 3L;
		long methodIdTwo = 15L;
		long constructorId = 7L;

		// the exception sensor type config
		MethodSensorTypeConfig sensorTypeConfig = mock(MethodSensorTypeConfig.class);
		when(sensorTypeConfig.getName()).thenReturn("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor");
		when(sensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);
		when(registeredSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
		when(registeredConstructorSensorConfig.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);

		// the exception sensor hook
		IMethodSensor exceptionSensor = mock(IMethodSensor.class);
		when(sensorTypeConfig.getSensorType()).thenReturn(exceptionSensor);
		ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
		when(exceptionSensor.getHook()).thenReturn(exceptionHook);

		// the invocation sequence sensor type config
		MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);

		IMethodSensor methodSensor = mock(IMethodSensor.class);
		when(invocSensorType.getSensorType()).thenReturn(methodSensor);
		InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
		when(methodSensor.getHook()).thenReturn(invocHook);

		// the map for the method hooks
		Map<Long, IHook> methodHooks = new LinkedHashMap<Long, IHook>();
		IMethodHook methodHook = mock(IMethodHook.class);
		// the map for the constructor hooks
		Map<Long, IHook> constructorHooks = new LinkedHashMap<Long, IHook>();
		methodHooks.put(methodSensorTypeId, methodHook);
		methodHooks.put(invocSensorTypeId, invocHook);
		constructorHooks.put(exceptionSensorTypeId, exceptionHook);
		when(registeredSensorConfig.getReverseMethodHooks()).thenReturn(methodHooks);
		when(registeredSensorConfig.getMethodHooks()).thenReturn(methodHooks);
		when(registeredConstructorSensorConfig.getReverseMethodHooks()).thenReturn(constructorHooks);
		when(registeredConstructorSensorConfig.getMethodHooks()).thenReturn(constructorHooks);
		when(registeredSensorConfig.startsInvocationSequence()).thenReturn(true);
		when(registeredSensorConfig.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);

		// second method
		RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
		Map<Long, IHook> methodHooksTwo = new LinkedHashMap<Long, IHook>();
		methodHooksTwo.put(methodSensorTypeId, methodHook);
		when(registeredSensorConfigTwo.getReverseMethodHooks()).thenReturn(methodHooksTwo);
		when(registeredSensorConfigTwo.getMethodHooks()).thenReturn(methodHooksTwo);

		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object returnValue = mock(Object.class);
		Object exceptionObject = mock(MyTestException.class);

		hookDispatcher.addMethodMapping(methodId, registeredSensorConfig);
		hookDispatcher.addMethodMapping(methodIdTwo, registeredSensorConfigTwo);
		hookDispatcher.addConstructorMapping(constructorId, registeredConstructorSensorConfig);

		// ////////////////////////////////////////////////////////
		// FIRST METHOD DISPATCHER

		// dispatch the first method - before body
		hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(registeredSensorConfig, times(1)).getInvocationSequenceSensorTypeConfig();
		verify(methodSensor, times(1)).getHook();
		verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
		verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);
		verify(invocSensorType, times(1)).getSensorType();

		// ////////////////////////////////////////////////////////
		// SECOND METHOD DISPATCHER

		// dispatch the second method - before body
		hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
		verify(registeredSensorConfig, times(1)).startsInvocationSequence();
		verify(registeredSensorConfig, times(1)).getReverseMethodHooks();
		verify(methodSensor, times(1)).getHook();
		verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
		verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));
		verify(invocSensorType, times(1)).getSensorType();

		hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
		verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

		// /////////////////////////////////////////////////////////
		// ///////////// EXCEPTION SENSOR STARTS HERE

		// first method of exception sensor
		hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
		verify(exceptionHook, times(1)).afterConstructor(invocHook, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

		// second method of exception sensor
		hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
		verify(registeredSensorConfig, times(2)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchOnThrowInBody(invocHook, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
		// ///////////// EXCEPTION SENSOR SECOND METHOD ENDS HERE
		// /////////////////////////////////////////////////////////

		// dispatch the second method - first after body
		hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
		verify(registeredSensorConfigTwo, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

		// dispatch the second method - second after body
		hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
		verify(registeredSensorConfigTwo, times(2)).startsInvocationSequence();
		verify(registeredSensorConfigTwo, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
		verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));
		// END SECOND METHOD DISPATCHER
		// ////////////////////////////////////////////////////////

		// third method of exception sensor
		hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
		verify(registeredSensorConfig, times(4)).getExceptionSensorTypeConfig();
		verify(exceptionHook, times(1)).dispatchBeforeCatchBody(invocHook, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

		// dispatch the first method - first after body
		hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(1)).getMethodHooks();
		verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
		verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// dispatch the first method - second after body
		hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
		verify(registeredSensorConfig, times(2)).startsInvocationSequence();
		verify(registeredSensorConfig, times(2)).getMethodHooks();
		verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
		verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

		// END FIRST METHOD DISPATCHER
		// ////////////////////////////////////////////////////////

		verifyZeroInteractions(object, coreService, returnValue);
		verifyNoMoreInteractions(methodHook, exceptionHook, registeredSensorConfig);
	}

}
