package rocks.inspectit.agent.java.hooking.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.analyzer.classes.MyTestException;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IConstructorHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.exception.ExceptionSensor;
import rocks.inspectit.agent.java.sensor.exception.ExceptionSensorHook;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceHook;
import rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceSensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class HookDispatcherTest extends TestBase {

	@InjectMocks
	HookDispatcher hookDispatcher;

	@Mock
	Logger log;

	@Mock
	ICoreService coreService;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	InvocationSequenceSensor invocationSequenceSensor;

	@Mock
	ExceptionSensor exceptionSensor;

	public class MethodHook extends HookDispatcherTest {

		@Test
		public void dispatchNoMethodHooks() {
			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> emptyList());
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> emptyList());

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneMethodHookWithoutInvocationTrace() {
			long sensorTypeId = 7L;
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			IMethodHook methodHook = mock(IMethodHook.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getHook()).thenReturn(methodHook);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensorConfig.getId()).thenReturn(sensorTypeId);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.singletonList(methodSensor));
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.singletonList(methodSensor));

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(registeredSensorConfig, methodHook);
		}

		@Test
		public void dispatchManyMethodHooksWithoutInvocationTrace() {
			IMethodHook methodHookOne = mock(IMethodHook.class);
			IMethodHook methodHookTwo = mock(IMethodHook.class);
			IMethodHook methodHookThree = mock(IMethodHook.class);
			IMethodSensor methodSensorOne = mock(IMethodSensor.class);
			IMethodSensor methodSensorTwo = mock(IMethodSensor.class);
			IMethodSensor methodSensorThree = mock(IMethodSensor.class);
			MethodSensorTypeConfig methodSensorConfigOne = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigTwo = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigThree = mock(MethodSensorTypeConfig.class);
			long sensorTypeIdOne = 7L;
			long sensorTypeIdTwo = 13L;
			long sensorTypeIdThree = 15L;
			when(methodSensorConfigOne.getId()).thenReturn(sensorTypeIdOne);
			when(methodSensorConfigTwo.getId()).thenReturn(sensorTypeIdTwo);
			when(methodSensorConfigThree.getId()).thenReturn(sensorTypeIdThree);
			when(methodSensorOne.getHook()).thenReturn(methodHookOne);
			when(methodSensorTwo.getHook()).thenReturn(methodHookTwo);
			when(methodSensorThree.getHook()).thenReturn(methodHookThree);
			when(methodSensorOne.getSensorTypeConfig()).thenReturn(methodSensorConfigOne);
			when(methodSensorTwo.getSensorTypeConfig()).thenReturn(methodSensorConfigTwo);
			when(methodSensorThree.getSensorTypeConfig()).thenReturn(methodSensorConfigThree);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			List<IMethodSensor> sensors = Arrays.<IMethodSensor> asList(methodSensorOne, methodSensorTwo, methodSensorThree);
			List<IMethodSensor> sensorsReverse = Arrays.<IMethodSensor> asList(methodSensorThree, methodSensorTwo, methodSensorOne);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(sensors);
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(sensorsReverse);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			InOrder inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookThree, times(1)).beforeBody(methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).beforeBody(methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookOne, times(1)).beforeBody(methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHookOne, methodHookTwo, methodHookThree);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneMethodHookWithInvocationTrace() {
			long methodSensorTypeId = 7L;
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			IMethodHook methodHook = mock(IMethodHook.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getHook()).thenReturn(methodHook);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensorConfig.getId()).thenReturn(methodSensorTypeId);

			long invocSensorTypeId = 13L;
			MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			when(invocationSequenceSensor.getSensorTypeConfig()).thenReturn(invocSensorType);
			when(invocationSequenceSensor.getHook()).thenReturn(invocHook);
			when(invocSensorType.getId()).thenReturn(invocSensorTypeId);

			// create registered sensor configuration which starts an invocation
			// sequence
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			List<IMethodSensor> sensors = Arrays.<IMethodSensor> asList(methodSensor, invocationSequenceSensor);
			List<IMethodSensor> sensorsReverse = Arrays.<IMethodSensor> asList(invocationSequenceSensor, methodSensor);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(sensors);
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(sensorsReverse);

			long methodId = 3L;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			// map the first method
			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			List<IMethodSensor> sensorsTwo = Arrays.<IMethodSensor> asList(methodSensor);
			when(registeredSensorConfigTwo.getMethodSensors()).thenReturn(sensorsTwo);
			when(registeredSensorConfigTwo.getMethodSensorsReverse()).thenReturn(sensorsTwo);

			long methodIdTwo = 15L;
			// map the second method
			hookDispatcher.addMapping(methodIdTwo, registeredSensorConfigTwo);

			// ////////////////////////////////////////////////////////
			// FIRST METHOD DISPATCHER

			// dispatch the first method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(methodSensor, times(1)).getHook();
			verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// SECOND METHOD DISPATCHER

			// dispatch the second method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfigTwo, times(1)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(1)).getMethodSensorsReverse();
			verify(methodSensor, times(2)).getHook();
			verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			// dispatch the second method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(1)).getMethodSensors();
			verify(methodSensor, times(3)).getHook();
			verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

			// dispatch the second method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(2)).getMethodSensors();
			verify(methodSensor, times(4)).getHook();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
			verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));

			// END SECOND METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// dispatch the first method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(methodSensor, times(5)).getHook();
			verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the first method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			verify(methodSensor, times(6)).getHook();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END FIRST METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// verify that no further interactions happened
			verifyZeroInteractions(object, coreService, returnValue, invocHook);
			verifyNoMoreInteractions(registeredSensorConfig, registeredSensorConfigTwo);
			verifyNoMoreInteractions(methodHook, invocHook);
		}
	}

	public class ConstructorHook extends HookDispatcherTest {

		@Test
		public void dispatchNoConstructorHooks() {
			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> emptyList());
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> emptyList());

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensors();

			verifyZeroInteractions(object, coreService);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneConstructorHookWithoutInvocationTrace() {
			long sensorTypeId = 7L;
			IConstructorHook constructorHook = mock(IConstructorHook.class);
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getHook()).thenReturn(constructorHook);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensorConfig.getId()).thenReturn(sensorTypeId);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.singletonList(methodSensor));
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.singletonList(methodSensor));


			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(constructorHook, times(1)).beforeConstructor(methodId, sensorTypeId, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(constructorHook, times(1)).afterConstructor(coreService, methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			verifyZeroInteractions(object, coreService);
			verifyNoMoreInteractions(registeredSensorConfig, constructorHook);
		}

		@Test
		public void dispatchManyConstructorHooksWithoutInvocationTrace() {
			IConstructorHook constructorHookOne = mock(IConstructorHook.class);
			IConstructorHook constructorHookTwo = mock(IConstructorHook.class);
			IConstructorHook constructorHookThree = mock(IConstructorHook.class);
			IMethodSensor methodSensorOne = mock(IMethodSensor.class);
			IMethodSensor methodSensorTwo = mock(IMethodSensor.class);
			IMethodSensor methodSensorThree = mock(IMethodSensor.class);
			MethodSensorTypeConfig methodSensorConfigOne = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigTwo = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigThree = mock(MethodSensorTypeConfig.class);
			long sensorTypeIdOne = 7L;
			long sensorTypeIdTwo = 13L;
			long sensorTypeIdThree = 15L;
			when(methodSensorConfigOne.getId()).thenReturn(sensorTypeIdOne);
			when(methodSensorConfigTwo.getId()).thenReturn(sensorTypeIdTwo);
			when(methodSensorConfigThree.getId()).thenReturn(sensorTypeIdThree);
			when(methodSensorOne.getHook()).thenReturn(constructorHookOne);
			when(methodSensorTwo.getHook()).thenReturn(constructorHookTwo);
			when(methodSensorThree.getHook()).thenReturn(constructorHookThree);
			when(methodSensorOne.getSensorTypeConfig()).thenReturn(methodSensorConfigOne);
			when(methodSensorTwo.getSensorTypeConfig()).thenReturn(methodSensorConfigTwo);
			when(methodSensorThree.getSensorTypeConfig()).thenReturn(methodSensorConfigThree);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			List<IMethodSensor> sensors = Arrays.<IMethodSensor> asList(methodSensorOne, methodSensorTwo, methodSensorThree);
			List<IMethodSensor> sensorsReverse = Arrays.<IMethodSensor> asList(methodSensorThree, methodSensorTwo, methodSensorOne);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(sensors);
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(sensorsReverse);

			int methodId = 3;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];

			hookDispatcher.addMapping(methodId, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(methodId, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			InOrder inOrder = inOrder(constructorHookOne, constructorHookTwo, constructorHookThree);
			inOrder.verify(constructorHookThree, times(1)).beforeConstructor(methodId, sensorTypeIdThree, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookTwo, times(1)).beforeConstructor(methodId, sensorTypeIdTwo, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookOne, times(1)).beforeConstructor(methodId, sensorTypeIdOne, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorAfterBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			inOrder = inOrder(constructorHookOne, constructorHookTwo, constructorHookThree);
			inOrder.verify(constructorHookOne, times(1)).afterConstructor(coreService, methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookTwo, times(1)).afterConstructor(coreService, methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
			inOrder.verify(constructorHookThree, times(1)).afterConstructor(coreService, methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);

			verifyZeroInteractions(object, coreService);
			verifyNoMoreInteractions(constructorHookOne, constructorHookTwo, constructorHookThree);
			verifyNoMoreInteractions(registeredSensorConfig);
		}

		@Test
		public void dispatchOneConstructorHookWithInvocationTrace() {
			long invocSensorTypeId = 13L;
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			MethodSensorTypeConfig invocationSensorConfig = mock(MethodSensorTypeConfig.class);
			when(invocationSequenceSensor.getSensorTypeConfig()).thenReturn(invocationSensorConfig);
			when(invocationSequenceSensor.getHook()).thenReturn(invocHook);
			when(invocationSensorConfig.getId()).thenReturn(invocSensorTypeId);

			// create registered sensor configuration which starts an invocation
			// sequence
			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(invocationSequenceSensor));
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(invocationSequenceSensor));

			// create method hooks
			long methodSensorTypeId = 7L;
			IConstructorHook constructorHook = mock(IConstructorHook.class);
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getHook()).thenReturn(constructorHook);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensorConfig.getId()).thenReturn(methodSensorTypeId);

			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfigTwo.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));
			when(registeredSensorConfigTwo.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));


			long methodId = 3L;
			long methodIdTwo = 15L;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);

			// map the methods
			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(methodIdTwo, registeredSensorConfigTwo);

			// ////////////////////////////////////////////////////////
			// METHOD DISPATCHER

			// dispatch the first method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// CONSTRUCTOR DISPATCHER

			// dispatch the constructor - before constructor
			hookDispatcher.dispatchConstructorBeforeBody(methodIdTwo, parameters);
			verify(registeredSensorConfigTwo, times(1)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(1)).getMethodSensorsReverse();
			verify(methodSensor, times(1)).getHook();
			verify(constructorHook, times(1)).beforeConstructor(methodIdTwo, methodSensorTypeId, parameters, registeredSensorConfigTwo);
			verify((IConstructorHook) invocHook, times(1)).beforeConstructor(eq(methodIdTwo), anyLong(), eq(parameters), eq(registeredSensorConfigTwo));

			// dispatch the constructor - after constructor
			hookDispatcher.dispatchConstructorAfterBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(1)).getMethodSensors();
			verify(methodSensor, times(2)).getHook();
			verify(constructorHook, times(1)).afterConstructor(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify((IConstructorHook) invocHook, times(1)).afterConstructor(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			// END CONSTRUCTOR DISPATCHER
			// ////////////////////////////////////////////////////////

			// dispatch the method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// verify that no further interactions happened
			verifyZeroInteractions(object, coreService, returnValue, invocHook);
			verifyNoMoreInteractions(registeredSensorConfig, registeredSensorConfigTwo);
			verifyNoMoreInteractions(constructorHook, invocHook);
		}
	}

	public class ExceptionHook extends HookDispatcherTest {

		@Test
		public void dispatchExceptionSensorWithOneMethodHookWithoutInvocationTrace() throws Exception {
			long sensorTypeId = 7L;
			long exceptionSensorTypeId = 10L;
			long methodId = 3L;
			long constructorId = 7L;

			// the exception sensor type config
			MethodSensorTypeConfig exceptionSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(exceptionSensorTypeConfig.getName()).thenReturn("rocks.inspectit.agent.java.sensor.exception.ExceptionSensor");
			when(exceptionSensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);

			// the exception sensor hook
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);

			// the map for the method hooks
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			IMethodHook methodHook = mock(IMethodHook.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensor.getHook()).thenReturn(methodHook);
			when(methodSensorConfig.getId()).thenReturn(sensorTypeId);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));
			RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredConstructorSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));
			when(registeredConstructorSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));

			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			Object exceptionObject = mock(MyTestException.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(constructorId, registeredConstructorSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(methodHook, times(1)).beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
			verify(registeredConstructorSensorConfig, times(1)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensorsReverse();
			verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

			// first method of exception sensor
			hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
			verify(registeredConstructorSensorConfig, times(2)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensors();
			verify(exceptionHook, times(1)).afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

			// second method of exception sensor
			hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(methodHook, times(1)).firstAfterBody(methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			verify(methodHook, times(1)).secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(exceptionHook, times(1)).dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHook, exceptionHook);
			verifyNoMoreInteractions(registeredSensorConfig, registeredConstructorSensorConfig);
		}

		@Test
		public void dispatchExceptionSensorWithManyMethodHooksWithoutInvocationTrace() throws Exception {
			// the map for the method hooks
			IMethodHook methodHookOne = mock(IMethodHook.class);
			IMethodHook methodHookTwo = mock(IMethodHook.class);
			IMethodHook methodHookThree = mock(IMethodHook.class);
			IMethodSensor methodSensorOne = mock(IMethodSensor.class);
			IMethodSensor methodSensorTwo = mock(IMethodSensor.class);
			IMethodSensor methodSensorThree = mock(IMethodSensor.class);
			MethodSensorTypeConfig methodSensorConfigOne = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigTwo = mock(MethodSensorTypeConfig.class);
			MethodSensorTypeConfig methodSensorConfigThree = mock(MethodSensorTypeConfig.class);
			long sensorTypeIdOne = 7L;
			long sensorTypeIdTwo = 13L;
			long sensorTypeIdThree = 15L;
			when(methodSensorConfigOne.getId()).thenReturn(sensorTypeIdOne);
			when(methodSensorConfigTwo.getId()).thenReturn(sensorTypeIdTwo);
			when(methodSensorConfigThree.getId()).thenReturn(sensorTypeIdThree);
			when(methodSensorOne.getHook()).thenReturn(methodHookOne);
			when(methodSensorTwo.getHook()).thenReturn(methodHookTwo);
			when(methodSensorThree.getHook()).thenReturn(methodHookThree);
			when(methodSensorOne.getSensorTypeConfig()).thenReturn(methodSensorConfigOne);
			when(methodSensorTwo.getSensorTypeConfig()).thenReturn(methodSensorConfigTwo);
			when(methodSensorThree.getSensorTypeConfig()).thenReturn(methodSensorConfigThree);


			long exceptionSensorTypeId = 10L;
			// the exception sensor type config
			MethodSensorTypeConfig exceptionSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(exceptionSensorTypeConfig.getName()).thenReturn("rocks.inspectit.agent.java.sensor.exception.ExceptionSensor");
			when(exceptionSensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);
			// the exception sensor hook
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			List<IMethodSensor> sensors = Arrays.<IMethodSensor> asList(methodSensorOne, methodSensorTwo, methodSensorThree);
			List<IMethodSensor> sensorsReverse = Arrays.<IMethodSensor> asList(methodSensorThree, methodSensorTwo, methodSensorOne);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(sensors);
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(sensorsReverse);
			RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredConstructorSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));
			when(registeredConstructorSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));

			long methodId = 3L;
			long constructorId = 1L;
			Object object = mock(Object.class);
			Object[] parameters = new Object[0];
			Object returnValue = mock(Object.class);
			Object exceptionObject = mock(MyTestException.class);

			hookDispatcher.addMapping(methodId, registeredSensorConfig);
			hookDispatcher.addMapping(constructorId, registeredConstructorSensorConfig);

			hookDispatcher.dispatchMethodBeforeBody(methodId, object, parameters);
			verify(registeredSensorConfig, times(1)).isStartsInvocation();
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			InOrder inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookThree, times(1)).beforeBody(methodId, sensorTypeIdThree, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).beforeBody(methodId, sensorTypeIdTwo, object, parameters, registeredSensorConfig);
			inOrder.verify(methodHookOne, times(1)).beforeBody(methodId, sensorTypeIdOne, object, parameters, registeredSensorConfig);

			hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
			verify(registeredConstructorSensorConfig, times(1)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensorsReverse();
			verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);

			// first method of exception sensor
			hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
			verify(registeredConstructorSensorConfig, times(2)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensors();
			verify(exceptionHook, times(1)).afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);

			// second method of exception sensor
			hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);

			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).firstAfterBody(methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).firstAfterBody(methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).firstAfterBody(methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			inOrder = inOrder(methodHookOne, methodHookTwo, methodHookThree);
			inOrder.verify(methodHookOne, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdOne, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookTwo, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdTwo, object, parameters, returnValue, registeredSensorConfig);
			inOrder.verify(methodHookThree, times(1)).secondAfterBody(coreService, methodId, sensorTypeIdThree, object, parameters, returnValue, registeredSensorConfig);

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(exceptionHook, times(1)).dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHookOne, methodHookTwo, methodHookThree, exceptionHook);
			verifyNoMoreInteractions(registeredConstructorSensorConfig, registeredSensorConfig);
		}

		@Test
		public void dispatchExceptionSensorWithOneMethodHookWithInvocationTrace() throws Exception {


			long methodSensorTypeId = 23L;
			long exceptionSensorTypeId = 10L;
			long invocSensorTypeId = 13L;
			long methodId = 3L;
			long methodIdTwo = 15L;
			long constructorId = 7L;

			// the exception sensor type config
			MethodSensorTypeConfig exceptionSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(exceptionSensorTypeConfig.getName()).thenReturn("rocks.inspectit.agent.java.sensor.exception.ExceptionSensor");
			when(exceptionSensorTypeConfig.getId()).thenReturn(exceptionSensorTypeId);
			// the exception sensor hook
			ExceptionSensorHook exceptionHook = mock(ExceptionSensorHook.class);
			when(exceptionSensor.getHook()).thenReturn(exceptionHook);
			when(exceptionSensor.getSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			// the invocation sequence sensor type config
			MethodSensorTypeConfig invocSensorType = mock(MethodSensorTypeConfig.class);
			when(invocSensorType.getId()).thenReturn(invocSensorTypeId);
			InvocationSequenceHook invocHook = mock(InvocationSequenceHook.class);
			when(invocationSequenceSensor.getSensorTypeConfig()).thenReturn(invocSensorType);
			when(invocationSequenceSensor.getHook()).thenReturn(invocHook);

			// the map for the method hooks
			IMethodSensor methodSensor = mock(IMethodSensor.class);
			IMethodHook methodHook = mock(IMethodHook.class);
			MethodSensorTypeConfig methodSensorConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorConfig);
			when(methodSensor.getHook()).thenReturn(methodHook);
			when(methodSensorConfig.getId()).thenReturn(methodSensorTypeId);

			RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfig.isStartsInvocation()).thenReturn(true);
			List<IMethodSensor> sensors = Arrays.<IMethodSensor> asList(methodSensor, invocationSequenceSensor);
			List<IMethodSensor> sensorsReverse = Arrays.<IMethodSensor> asList(invocationSequenceSensor, methodSensor);
			when(registeredSensorConfig.getMethodSensors()).thenReturn(sensors);
			when(registeredSensorConfig.getMethodSensorsReverse()).thenReturn(sensorsReverse);

			RegisteredSensorConfig registeredConstructorSensorConfig = mock(RegisteredSensorConfig.class);
			when(registeredConstructorSensorConfig.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));
			when(registeredConstructorSensorConfig.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(exceptionSensor));

			// second method
			RegisteredSensorConfig registeredSensorConfigTwo = mock(RegisteredSensorConfig.class);
			when(registeredSensorConfigTwo.getMethodSensors()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));
			when(registeredSensorConfigTwo.getMethodSensorsReverse()).thenReturn(Collections.<IMethodSensor> singletonList(methodSensor));

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
			verify(registeredSensorConfig, times(1)).getMethodSensorsReverse();
			verify(methodHook, times(1)).beforeBody(methodId, methodSensorTypeId, object, parameters, registeredSensorConfig);
			verify(invocHook, times(1)).beforeBody(methodId, invocSensorTypeId, object, parameters, registeredSensorConfig);

			// ////////////////////////////////////////////////////////
			// SECOND METHOD DISPATCHER

			// dispatch the second method - before body
			hookDispatcher.dispatchMethodBeforeBody(methodIdTwo, object, parameters);
			verify(registeredSensorConfigTwo, times(1)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(1)).getMethodSensorsReverse();
			verify(methodHook, times(1)).beforeBody(methodIdTwo, methodSensorTypeId, object, parameters, registeredSensorConfigTwo);
			verify(invocHook, times(1)).beforeBody(eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(registeredSensorConfigTwo));

			hookDispatcher.dispatchConstructorBeforeBody(constructorId, parameters);
			verify(registeredConstructorSensorConfig, times(1)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensorsReverse();
			verify(exceptionHook, times(1)).beforeConstructor(constructorId, exceptionSensorTypeId, parameters, registeredConstructorSensorConfig);
			verify(invocHook, times(1)).beforeConstructor(eq(constructorId), anyLong(), eq(parameters), eq(registeredConstructorSensorConfig));

			// /////////////////////////////////////////////////////////
			// ///////////// EXCEPTION SENSOR STARTS HERE

			// first method of exception sensor
			hookDispatcher.dispatchConstructorAfterBody(constructorId, exceptionObject, parameters);
			verify(registeredConstructorSensorConfig, times(2)).isStartsInvocation();
			verify(registeredConstructorSensorConfig, times(1)).getMethodSensors();
			verify(exceptionHook, times(1)).afterConstructor(invocHook, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredConstructorSensorConfig);
			verify(invocHook, times(1)).afterConstructor(eq(coreService), eq(constructorId), anyLong(), eq(exceptionObject), eq(parameters), eq(registeredConstructorSensorConfig));

			// second method of exception sensor
			hookDispatcher.dispatchOnThrowInBody(methodId, object, parameters, exceptionObject);
			verify(exceptionHook, times(1)).dispatchOnThrowInBody(invocHook, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
			// ///////////// EXCEPTION SENSOR SECOND METHOD ENDS HERE
			// /////////////////////////////////////////////////////////

			// dispatch the second method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(1)).getMethodSensors();
			verify(methodHook, times(1)).firstAfterBody(methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);

			// dispatch the second method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodIdTwo, object, parameters, returnValue);
			verify(registeredSensorConfigTwo, times(2)).isStartsInvocation();
			verify(registeredSensorConfigTwo, times(2)).getMethodSensors();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodIdTwo, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfigTwo);
			verify(invocHook, times(1)).secondAfterBody(eq(coreService), eq(methodIdTwo), anyLong(), eq(object), eq(parameters), eq(returnValue), eq(registeredSensorConfigTwo));
			// END SECOND METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			// third method of exception sensor
			hookDispatcher.dispatchBeforeCatch(methodId, exceptionObject);
			verify(exceptionHook, times(1)).dispatchBeforeCatchBody(invocHook, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

			// dispatch the first method - first after body
			hookDispatcher.dispatchFirstMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(1)).getMethodSensors();
			verify(methodHook, times(1)).firstAfterBody(methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).firstAfterBody(methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// dispatch the first method - second after body
			hookDispatcher.dispatchSecondMethodAfterBody(methodId, object, parameters, returnValue);
			verify(registeredSensorConfig, times(2)).isStartsInvocation();
			verify(registeredSensorConfig, times(2)).getMethodSensors();
			verify(methodHook, times(1)).secondAfterBody(invocHook, methodId, methodSensorTypeId, object, parameters, returnValue, registeredSensorConfig);
			verify(invocHook, times(1)).secondAfterBody(coreService, methodId, invocSensorTypeId, object, parameters, returnValue, registeredSensorConfig);

			// END FIRST METHOD DISPATCHER
			// ////////////////////////////////////////////////////////

			verifyZeroInteractions(object, coreService, returnValue);
			verifyNoMoreInteractions(methodHook, exceptionHook, invocHook);
			verifyNoMoreInteractions(registeredSensorConfig, registeredSensorConfigTwo, registeredConstructorSensorConfig);
		}
	}

}
