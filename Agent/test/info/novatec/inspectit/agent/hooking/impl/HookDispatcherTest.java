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

import info.novatec.inspectit.agent.analyzer.classes.MyTestException;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IHookSupplier;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensorHook;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceHook;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class HookDispatcherTest extends TestBase {

	@InjectMocks
	protected HookDispatcher hookDispatcher;

	@Mock
	protected Logger log;

	@Mock
	protected ICoreService coreService;

	@Mock
	protected IHookSupplier hookSupplier;

	public class MethodHook extends HookDispatcherTest {

		@Test
		public void dispatchNoMethodHooks() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[0]);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneMethodHookWithoutInvocationTrace() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			IMethodHook methodHook = mock(IMethodHook.class);
			long sensorTypeId = 7L;
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeId });
			when(hookSupplier.getMethodHook(sensorTypeId)).thenReturn(methodHook);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(registeredSensorConfig, methodHook);
		}

		@Test
		public void dispatchManyMethodHooksWithoutInvocationTrace() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			IMethodHook methodHookOne = mock(IMethodHook.class);
			IMethodHook methodHookTwo = mock(IMethodHook.class);
			IMethodHook methodHookThree = mock(IMethodHook.class);
			long sensorTypeIdOne = 7L;
			long sensorTypeIdTwo = 13L;
			long sensorTypeIdThree = 15L;
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeIdOne, sensorTypeIdTwo, sensorTypeIdThree });
			when(hookSupplier.getMethodHook(sensorTypeIdOne)).thenReturn(methodHookOne);
			when(hookSupplier.getMethodHook(sensorTypeIdTwo)).thenReturn(methodHookTwo);
			when(hookSupplier.getMethodHook(sensorTypeIdThree)).thenReturn(methodHookThree);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			InOrder inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookThree, times(1)).beforeBody(methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).beforeBody(methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookOne, times(1)).beforeBody(methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
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
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
			IMethodSensor methodSensor = mock(IMethodSensor.class);

			long invocSensorTypeId = 13L;
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			when(methodSensor.getHook()).thenReturn(invocHook);

			when(invocSensorType.getId()).thenReturn(invocSensorTypeId);
			when(hookSupplier.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);
			when(hookSupplier.getMethodHook(invocSensorTypeId)).thenReturn(invocHook);

			// create method hooks map
			IMethodHook methodHook = mock(IMethodHook.class);
			long methodSensorTypeId = 7L;

			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { methodSensorTypeId, invocSensorTypeId });
			when(hookSupplier.getMethodHook(methodSensorTypeId)).thenReturn(methodHook);
			when(hookSupplier.getMethodHook(invocSensorTypeId)).thenReturn(invocHook);

			long methodId = 3L;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			// map the first method
			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfigTwo.getSensorIds()).thenReturn(new long[] { methodSensorTypeId });

			long methodIdTwo = 15L;
			// map the second method
			hookDispatcher.addMapping(methodIdTwo, registeredSensorConfigTwo);

			// ////////////////////////////////////////////////////////
			// FIRST METHOD DISPATCHER

			// dispatch the first method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(hookSupplier, times(1)).getInvocationSequenceSensorTypeConfig();
			verify(invocSensorType, times(1)).getId();
			verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// SECOND METHOD DISPATCHER

			// dispatch the second method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(invocSensorType, times(1)).getId();
			verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			// dispatch the second method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

			// dispatch the second method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
			verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));

			// END SECOND METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// dispatch the first method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the first method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END FIRST METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// verify that no further interactions happened
			verifyZeroInteractions(object, coreService, returnValue, invocHook);
			verifyNoMoreInteractions(registeredSensorConfig, methodHook, methodSensor, invocSensorType);
		}
	}

	public class ConstructorHook extends HookDispatcherTest {

		@Test
		public void dispatchNoConstructorHooks() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[0]);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();

			verifyZeroInteractions(object, coreService);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneConstructorHookWithoutInvocationTrace() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			IConstructorHook constructorHook = mock(IConstructorHook.class);
			long sensorTypeId = 7L;
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeId });
			when(hookSupplier.getMethodHook(sensorTypeId)).thenReturn(constructorHook);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(constructorHook, times(1)).beforeConstructor(methodId, sensorTypeId, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(constructorHook, times(1)).afterConstructor(coreService, methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			verifyZeroInteractions(object, coreService);
			verifyNoMoreInteractions(registeredSensorConfig, constructorHook);
		}

		@Test
		public void dispatchManyConstructorHooksWithoutInvocationTrace() {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			IConstructorHook constructorHookOne = mock(IConstructorHook.class);
			IConstructorHook constructorHookTwo = mock(IConstructorHook.class);
			IConstructorHook constructorHookThree = mock(IConstructorHook.class);
			long sensorTypeIdOne = 7L;
			long sensorTypeIdTwo = 13L;
			long sensorTypeIdThree = 15L;

			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeIdOne, sensorTypeIdTwo, sensorTypeIdThree });
			when(hookSupplier.getMethodHook(sensorTypeIdOne)).thenReturn(constructorHookOne);
			when(hookSupplier.getMethodHook(sensorTypeIdTwo)).thenReturn(constructorHookTwo);
			when(hookSupplier.getMethodHook(sensorTypeIdThree)).thenReturn(constructorHookThree);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			InOrder inOrder = inOrder(constructorHookOne, constructorHookTwo, constructorHookThree);
			inOrder.verify(constructorHookThree, times(1)).beforeConstructor(methodId, sensorTypeIdThree, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookTwo, times(1)).beforeConstructor(methodId, sensorTypeIdTwo, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookOne, times(1)).beforeConstructor(methodId, sensorTypeIdOne, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
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
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			long invocSensorTypeId = 13L;
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			when(invocSensorType.getId()).thenReturn(invocSensorTypeId);
			when(hookSupplier.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);
			when(hookSupplier.getMethodHook(invocSensorTypeId)).thenReturn(invocHook);

			// create method hooks
			IConstructorHook constructorHook = mock(IConstructorHook.class);
			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { invocSensorTypeId });
			when(hookSupplier.getMethodHook(invocSensorTypeId)).thenReturn(invocHook);

			long methodId = 3L;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			// map the first method
			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			long methodSensorTypeId = 7L;
			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfigTwo.getSensorIds()).thenReturn(new long[] { methodSensorTypeId });
			when(hookSupplier.getMethodHook(methodSensorTypeId)).thenReturn(constructorHook);

			long methodIdTwo = 15L;
			// map the second method
			hookDispatcher.addMapping(methodIdTwo, registeredSensorConfigTwo);

			// ////////////////////////////////////////////////////////
			// METHOD DISPATCHER

			// dispatch the first method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(hookSupplier, times(1)).getInvocationSequenceSensorTypeConfig();
			verify(invocSensorType, times(1)).getId();
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// CONSTRUCTOR DISPATCHER

			// dispatch the constructor - before constructor
			hookDispatcher.dispatchConstructorBeforeBody(methodIdTwo, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(invocSensorType, times(1)).getId();
			verify(constructorHook, times(1)).beforeConstructor(methodIdTwo, methodSensorTypeId, parameters, registeredSensorConfigTwo);
			verify((IConstructorHook) invocHook, times(1)).beforeConstructor(eq(methodIdTwo), anyLong(), eq(parameters), eq(registeredSensorConfigTwo));

			// dispatch the constructor - after constructor
			hookDispatcher.dispatchConstructorAfterBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(2)).getSensorIds();
			verify(constructorHook, times(1)).afterConstructor(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify((IConstructorHook) invocHook, times(1)).afterConstructor(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			// END CONSTRUCTOR DISPATCHER
			// ////////////////////////////////////////////////////////

			// dispatch the method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// verify that no further interactions happened
			verifyZeroInteractions(object, coreService, returnValue, invocHook);
			verifyNoMoreInteractions(registeredSensorConfig, constructorHook, methodSensor, invocSensorType);
		}
	}

	public class ExceptionHook extends HookDispatcherTest {

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
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);

			// the map for the method hooks
			IMethodHook methodHook = mock(IMethodHook.class);

			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeId });
			when(hookSupplier.getMethodHook(sensorTypeId)).thenReturn(methodHook);
			when(registeredConstructorSensorConfig.getSensorIds()).thenReturn(new long[] { exceptionSensorTypeId });
			when(hookSupplier.getMethodHook(exceptionSensorTypeId)).thenReturn(exceptionHook);
			when(hookSupplier.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);
			when(hookSupplier.getMethodHook(exceptionSensorTypeId)).thenReturn(exceptionHook);

			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			Object exceptionObject = mock(MyTestException.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(constructorId, registeredConstructorSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
			verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

			// first method of exception sensor
			hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
			verify(exceptionHook, times(1)).afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

			// second method of exception sensor
			hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
			verify(hookSupplier, times(2)).getExceptionSensorTypeConfig();
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(hookSupplier, times(4)).getExceptionSensorTypeConfig();
			verify(exceptionHook, times(1)).dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHook, exceptionHook, registeredSensorConfig);
		}

		@Test
		public void dispatchExceptionSensorWithManyMethodHooksWithoutInvocationTrace() throws Exception {
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);

			// the map for the method hooks
			IMethodHook methodHookOne = mock(IMethodHook.class);
			IMethodHook methodHookTwo = mock(IMethodHook.class);
			IMethodHook methodHookThree = mock(IMethodHook.class);

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
			when(hookSupplier.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);

			// the exception sensor hook
			IMethodSensor exceptionSensor = mock(IMethodSensor.class);
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);

			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { sensorTypeIdOne, sensorTypeIdTwo, sensorTypeIdThree });
			when(registeredConstructorSensorConfig.getSensorIds()).thenReturn(new long[] { exceptionSensorTypeId });
			when(hookSupplier.getMethodHook(sensorTypeIdOne)).thenReturn(methodHookOne);
			when(hookSupplier.getMethodHook(sensorTypeIdTwo)).thenReturn(methodHookTwo);
			when(hookSupplier.getMethodHook(sensorTypeIdThree)).thenReturn(methodHookThree);
			when(hookSupplier.getMethodHook(exceptionSensorTypeId)).thenReturn(exceptionHook);

			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			Object exceptionObject = mock(MyTestException.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(constructorId, registeredConstructorSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
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
			verify(hookSupplier, times(2)).getExceptionSensorTypeConfig();
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(hookSupplier, times(4)).getExceptionSensorTypeConfig();
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
			when(hookSupplier.getExceptionSensorTypeConfig()).thenReturn(sensorTypeConfig);

			// the exception sensor hook
			IMethodSensor exceptionSensor = mock(IMethodSensor.class);
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);

			// the invocation sequence sensor type config
			MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
			when(invocSensorType.getId()).thenReturn(invocSensorTypeId);

			IMethodSensor methodSensor = mock(IMethodSensor.class);
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			when(methodSensor.getHook()).thenReturn(invocHook);

			// the map for the method hooks
			IMethodHook methodHook = mock(IMethodHook.class);
			// the map for the constructor hooks
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			when(hookSupplier.getInvocationSequenceSensorTypeConfig()).thenReturn(invocSensorType);

			when(registeredSensorConfig.getSensorIds()).thenReturn(new long[] { methodSensorTypeId, invocSensorTypeId });
			when(hookSupplier.getMethodHook(methodSensorTypeId)).thenReturn(methodHook);
			when(hookSupplier.getMethodHook(invocSensorTypeId)).thenReturn(invocHook);
			when(registeredConstructorSensorConfig.getSensorIds()).thenReturn(new long[] { exceptionSensorTypeId, });
			when(hookSupplier.getMethodHook(exceptionSensorTypeId)).thenReturn(exceptionHook);

			// second method
			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfigTwo.getSensorIds()).thenReturn(new long[] { methodSensorTypeId });
			when(hookSupplier.getMethodHook(methodSensorTypeId)).thenReturn(methodHook);

			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			Object exceptionObject = mock(MyTestException.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(methodIdTwo, registeredSensorConfigTwo);
			hookDispatcher.addMapping(constructorId, registeredConstructorSensorConfig);

			// ////////////////////////////////////////////////////////
			// FIRST METHOD DISPATCHER

			// dispatch the first method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(hookSupplier, times(1)).getInvocationSequenceSensorTypeConfig();
			verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// SECOND METHOD DISPATCHER

			// dispatch the second method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getSensorIds();
			verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
			verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

			// /////////////////////////////////////////////////////////
			// ///////////// EXCEPTION SENSOR STARTS HERE

			// first method of exception sensor
			hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
			verify(exceptionHook, times(1)).afterConstructor(invocHook, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

			// second method of exception sensor
			hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
			verify(hookSupplier, times(2)).getExceptionSensorTypeConfig();
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(invocHook, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
			// ///////////// EXCEPTION SENSOR SECOND METHOD ENDS HERE
			// /////////////////////////////////////////////////////////

			// dispatch the second method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

			// dispatch the second method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
			verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));
			// END SECOND METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(hookSupplier, times(4)).getExceptionSensorTypeConfig();
			verify(exceptionHook, times(1)).dispatchBeforeCatchBody(invocHook, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

			// dispatch the first method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).getSensorIds();
			verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the first method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(3)).getSensorIds();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END FIRST METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHook, exceptionHook, registeredSensorConfig);
		}
	}

}
