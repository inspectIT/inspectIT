package info.novatec.inspectit.agent.sensor.method.invocationsequence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.sensor.ISensor;
import info.novatec.inspectit.agent.sensor.exception.ExceptionSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementParameterSensor;
import info.novatec.inspectit.agent.sensor.method.jdbc.PreparedStatementSensor;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Testing the {@link InvocationSequenceHook}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class InvocationSequenceHookTest extends AbstractLogSupport {

	/**
	 * Class under test.
	 */
	private InvocationSequenceHook invocationSequenceHook;

	@Mock
	private Timer timer;

	@Mock
	private IIdManager idManager;

	@Mock
	private IPropertyAccessor propertyAccessor;

	@Mock
	private RegisteredSensorConfig rsc;

	@Mock
	private CoreService coreService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		invocationSequenceHook = new InvocationSequenceHook(timer, idManager, propertyAccessor, Collections.<String, Object> emptyMap(), false);
	}

	/**
	 * Tests that the correct time and ids will be set on the invocation.
	 * 
	 * @throws IdNotAvailableException
	 */
	@Test
	public void startEndInvocationWithDataSaving() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue);

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);

		// save two objects
		TimerData timerData = new TimerData();
		SqlStatementData sqlStatementData = new SqlStatementData();
		invocationSequenceHook.addMethodSensorData(0, 0, "", timerData);
		invocationSequenceHook.addMethodSensorData(0, 0, "", sqlStatementData);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(2)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), is(empty()));
		assertThat(invocation.getChildCount(), is(0L));
		assertThat(invocation.getTimerData(), is(timerData));
		assertThat(invocation.getSqlStatementData(), is(sqlStatementData));
	}

	/**
	 * Tests that the invocation and child will have correct times and ids.
	 * 
	 * @throws IdNotAvailableException
	 */
	@Test
	public void twoInvocationsParentChild() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		double fourthTimerValue = 2562.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue, fourthTimerValue);

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(4)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(fourthTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(1));
		assertThat(invocation.getChildCount(), is(1L));
		InvocationSequenceData child = invocation.getNestedSequences().iterator().next();
		assertThat(child.getPlatformIdent(), is(platformId));
		assertThat(child.getMethodIdent(), is(registeredMethodId2));
		assertThat(child.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(child.getDuration(), is(thirdTimerValue - secondTimerValue));
		assertThat(child.getNestedSequences(), is(empty()));
		assertThat(child.getParentSequence(), is(invocation));
		assertThat(child.getChildCount(), is(0L));
	}

	/**
	 * Tests that the invocation and child will have correct times and ids when there is a recursive
	 * invocation (same method twice).
	 * 
	 * @throws IdNotAvailableException
	 */
	@Test
	public void twoRecursiveInvocations() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		double fourthTimerValue = 2562.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue, fourthTimerValue);

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(4)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(fourthTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(1));
		assertThat(invocation.getChildCount(), is(1L));
		InvocationSequenceData child = invocation.getNestedSequences().iterator().next();
		assertThat(child.getPlatformIdent(), is(platformId));
		assertThat(child.getMethodIdent(), is(registeredMethodId1));
		assertThat(child.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(child.getDuration(), is(thirdTimerValue - secondTimerValue));
		assertThat(child.getNestedSequences(), is(empty()));
		assertThat(child.getParentSequence(), is(invocation));
		assertThat(child.getChildCount(), is(0L));
	}

	/**
	 * Tests that invocation will not be saved if the duration is below min duration specified in
	 * the rsc settings.
	 * 
	 * @throws IdNotAvailableException
	 */
	@Test
	public void minDuration() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1200.0d;
		String minDuration = "201";
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("minduration", minDuration);
		when(rsc.getSettings()).thenReturn(map);

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(2)).getCurrentTime();
		verifyZeroInteractions(coreService);

		secondTimerValue = 1202.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue);

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(4)).getCurrentTime();
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId), Mockito.<String> anyObject(), Mockito.<InvocationSequenceData> anyObject());
	}

	/**
	 * Tests that when Id is not available (sensor) on start of invocation no data will be captured.
	 */
	@Test
	public void idNotAvailableSensor() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenThrow(new IdNotAvailableException("test"));

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verifyZeroInteractions(timer, coreService);
	}

	/**
	 * Tests that when Id is not available (platform) on start of invocation no data will be
	 * captured.
	 */
	@Test
	public void idNotAvailablePlatform() throws IdNotAvailableException {
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("test"));

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verifyZeroInteractions(timer, coreService);
	}

	/**
	 * Tests that when Id is not available (method) on child of invocation no data will be captured
	 * for that child.
	 */
	@Test
	public void idNotAvailableChildMethod() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenThrow(new IdNotAvailableException("test"));

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue);

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(2)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), is(empty()));
		assertThat(invocation.getChildCount(), is(0L));
	}

	/**
	 * Tests that when Id is not available (method) on child of invocation no data will be captured
	 * for that child even if the method is registered in the meantime.
	 */
	@Test
	public void idNotAvailableThenRegisteredChildMethod() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenThrow(new IdNotAvailableException("test")).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue);

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(2)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), is(empty()));
		assertThat(invocation.getChildCount(), is(0L));
	}

	/**
	 * Checks if there is a correct order of children when one in the middle is removed.
	 */
	@Test
	public void fixChildrenOnRemoval() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		long methodId3 = 31L;
		long registeredMethodId3 = 37L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);
		when(idManager.getRegisteredMethodId(methodId3)).thenReturn(registeredMethodId3);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		double fourthTimerValue = 2562.0d;
		double fifthTimerValue = 3221.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue, fourthTimerValue, fifthTimerValue);

		RegisteredSensorConfig removingRsc = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(exceptionSensorConfig.getClassName()).thenReturn(ExceptionSensor.class.getCanonicalName());
		when(removingRsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(exceptionSensorConfig));

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, removingRsc);
		invocationSequenceHook.beforeBody(methodId3, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId3, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId3, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(5)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(fifthTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(1));
		assertThat(invocation.getChildCount(), is(1L));
		InvocationSequenceData child = invocation.getNestedSequences().iterator().next();
		assertThat(child.getPlatformIdent(), is(platformId));
		assertThat(child.getMethodIdent(), is(registeredMethodId3));
		assertThat(child.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(child.getDuration(), is(fourthTimerValue - thirdTimerValue));
		assertThat(child.getNestedSequences(), is(empty()));
		assertThat(child.getParentSequence(), is(invocation));
		assertThat(child.getChildCount(), is(0L));
	}

	/**
	 * Removing done due to the exception delegation.
	 */
	@Test
	public void removeExceptionDelegation() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue);

		RegisteredSensorConfig removingRsc = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(exceptionSensorConfig.getClassName()).thenReturn(ExceptionSensor.class.getCanonicalName());
		when(removingRsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(exceptionSensorConfig));

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(3)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(thirdTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(0));
		assertThat(invocation.getChildCount(), is(0L));
	}

	/**
	 * No removing exception delegation done cause there is exception object.
	 */
	@Test
	public void noRemoveExceptionDelegation() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		double fourthTimerValue = 2562.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue, fourthTimerValue);

		RegisteredSensorConfig removingRsc = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(exceptionSensorConfig.getClassName()).thenReturn(ExceptionSensor.class.getCanonicalName());
		when(removingRsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(exceptionSensorConfig));

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, removingRsc);

		ExceptionSensorData exceptionData = new ExceptionSensorData();
		invocationSequenceHook.addExceptionSensorData(0, 0, exceptionData);

		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(4)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(fourthTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(1));
		assertThat(invocation.getChildCount(), is(1L));
		InvocationSequenceData child = invocation.getNestedSequences().iterator().next();
		assertThat(child.getPlatformIdent(), is(platformId));
		assertThat(child.getMethodIdent(), is(registeredMethodId2));
		assertThat(child.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(child.getDuration(), is(thirdTimerValue - secondTimerValue));
		assertThat(child.getNestedSequences(), is(empty()));
		assertThat(child.getParentSequence(), is(invocation));
		assertThat(child.getChildCount(), is(0L));
		assertThat(child.getExceptionSensorDataObjects(), is(Collections.singletonList(exceptionData)));
	}

	/**
	 * Removing done due to the wrapped SQLs.
	 */
	@Test
	public void removeWrappedSql() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue);

		RegisteredSensorConfig removingRsc = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(exceptionSensorConfig.getClassName()).thenReturn(PreparedStatementSensor.class.getCanonicalName());
		when(removingRsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(exceptionSensorConfig));

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(3)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(thirdTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(0));
		assertThat(invocation.getChildCount(), is(0L));
	}

	/**
	 * No removing done due to the wrapped SQLs, because there is SQL object.
	 */
	@Test
	public void noRemoveWrappedSql() throws IdNotAvailableException {
		long platformId = 1L;
		long methodId1 = 3L;
		long registeredMethodId1 = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		long methodId2 = 23L;
		long registeredMethodId2 = 27L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(idManager.getRegisteredMethodId(methodId1)).thenReturn(registeredMethodId1);
		when(idManager.getRegisteredMethodId(methodId2)).thenReturn(registeredMethodId2);

		double firstTimerValue = 1000.0d;
		double secondTimerValue = 1323.0d;
		double thirdTimerValue = 1881.0d;
		double fourthTimerValue = 2562.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue, secondTimerValue, thirdTimerValue, fourthTimerValue);

		RegisteredSensorConfig removingRsc = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(exceptionSensorConfig.getClassName()).thenReturn(PreparedStatementSensor.class.getCanonicalName());
		when(removingRsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(exceptionSensorConfig));

		invocationSequenceHook.beforeBody(methodId1, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.beforeBody(methodId2, sensorTypeId, object, parameters, removingRsc);

		SqlStatementData sqlStatementData = new SqlStatementData();
		sqlStatementData.setCount(1L);
		invocationSequenceHook.addMethodSensorData(0, 0, "", sqlStatementData);

		invocationSequenceHook.firstAfterBody(methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId2, sensorTypeId, object, parameters, result, removingRsc);
		invocationSequenceHook.firstAfterBody(methodId1, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId1, sensorTypeId, object, parameters, result, rsc);

		verify(timer, times(4)).getCurrentTime();
		ArgumentCaptor<InvocationSequenceData> captor = ArgumentCaptor.forClass(InvocationSequenceData.class);
		verify(coreService, times(1)).addMethodSensorData(eq(sensorTypeId), eq(methodId1), Mockito.<String> anyObject(), captor.capture());

		InvocationSequenceData invocation = captor.getValue();
		assertThat(invocation.getPlatformIdent(), is(platformId));
		assertThat(invocation.getMethodIdent(), is(registeredMethodId1));
		assertThat(invocation.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(invocation.getDuration(), is(fourthTimerValue - firstTimerValue));
		assertThat(invocation.getNestedSequences(), hasSize(1));
		assertThat(invocation.getChildCount(), is(1L));
		InvocationSequenceData child = invocation.getNestedSequences().iterator().next();
		assertThat(child.getPlatformIdent(), is(platformId));
		assertThat(child.getMethodIdent(), is(registeredMethodId2));
		assertThat(child.getSensorTypeIdent(), is(registeredSensorTypeId));
		assertThat(child.getDuration(), is(thirdTimerValue - secondTimerValue));
		assertThat(child.getNestedSequences(), is(empty()));
		assertThat(child.getParentSequence(), is(invocation));
		assertThat(child.getChildCount(), is(0L));
		assertThat(child.getSqlStatementData(), is(sqlStatementData));
	}

	/**
	 * Tests that skip is activated when certain sensor is only defined in the
	 * {@link RegisteredSensorConfig}.
	 * 
	 * @see #skippingSensors()
	 */
	@Test(dataProvider = "skippingSensors")
	public void skipSingleSensor(Class<? extends ISensor> sensorClass) throws IdNotAvailableException {
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		MethodSensorTypeConfig sensorConfig = mock(MethodSensorTypeConfig.class);
		when(sensorConfig.getClassName()).thenReturn(sensorClass.getCanonicalName());
		when(rsc.getSensorTypeConfigs()).thenReturn(Collections.singletonList(sensorConfig));

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verifyZeroInteractions(timer, coreService);
	}

	/**
	 * Tests that skip is activated when certain sensor is defined in the
	 * {@link RegisteredSensorConfig} together with exception sensor config.
	 * 
	 * @see #skippingSensors()
	 */
	@Test(dataProvider = "skippingSensors")
	public void skipSensorWithEnchancedExceptionSensor(Class<? extends ISensor> sensorClass) throws IdNotAvailableException {
		invocationSequenceHook = new InvocationSequenceHook(timer, idManager, propertyAccessor, Collections.<String, Object> emptyMap(), true);

		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		MethodSensorTypeConfig sensorConfig = mock(MethodSensorTypeConfig.class);
		MethodSensorTypeConfig exceptionSensorConfig = mock(MethodSensorTypeConfig.class);
		when(sensorConfig.getClassName()).thenReturn(sensorClass.getCanonicalName());
		when(exceptionSensorConfig.getClassName()).thenReturn(ExceptionSensor.class.getCanonicalName());

		List<MethodSensorTypeConfig> configs = new ArrayList<MethodSensorTypeConfig>();
		configs.add(exceptionSensorConfig);
		configs.add(sensorConfig);
		when(rsc.getSensorTypeConfigs()).thenReturn(configs);

		invocationSequenceHook.beforeBody(methodId, sensorTypeId, object, parameters, rsc);
		invocationSequenceHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, rsc);
		invocationSequenceHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, rsc);

		verifyZeroInteractions(timer, coreService);
	}

	@DataProvider(name = "skippingSensors")
	public Object[][] skippingSensors() {
		return new Object[][] { { ConnectionSensor.class }, { PreparedStatementParameterSensor.class } };
	}
}
