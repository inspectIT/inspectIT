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
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.Timer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
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

}
