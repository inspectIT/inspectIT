package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IObjectStorage;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class StatementHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IdManager idManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private Map<String, Object> parameter;

	@Mock
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	@Mock
	private StatementReflectionCache statementReflectionCache;

	private StatementHook statementHook;

	private StatementHook statementHook2;

	@BeforeMethod
	public void initTestClass() {
		statementHook = new StatementHook(timer, idManager, connectionMetaDataStorage, statementReflectionCache, parameter);
		statementHook2 = new StatementHook(timer, idManager, connectionMetaDataStorage, statementReflectionCache, parameter);

		List<String> list = new ArrayList<String>();
		list.add("java.lang.String");
		when(registeredSensorConfig.getParameterTypes()).thenReturn(list);
	}

	@Test
	public void oneStatement() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[1];
		parameters[0] = "SELECT * FROM TEST";
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(idManager).getPlatformId();
		verify(idManager).getRegisteredMethodId(methodId);
		verify(idManager).getRegisteredSensorTypeId(sensorTypeId);

		Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
		SqlStatementData bsqld = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
		bsqld.setSql((String) parameters[0]);

		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), (MethodSensorData) Mockito.anyObject());
		verify(coreService).getMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject());
		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
	}

	@Test
	public void oneStatementDelegatesStatement() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[1];
		parameters[0] = "SELECT * FROM TEST";
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		statementHook2.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();
		statementHook2.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();
		statementHook2.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(idManager, times(2)).getPlatformId();
		verify(idManager, times(2)).getRegisteredMethodId(methodId);
		verify(idManager, times(2)).getRegisteredSensorTypeId(sensorTypeId);

		Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
		SqlStatementData bsqld = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
		bsqld.setSql((String) parameters[0]);

		verify(coreService, times(2)).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), (MethodSensorData) Mockito.anyObject());
		verify(coreService, times(2)).getMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject());
		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
	}

	@Test
	public void platformIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[1];
		parameters[0] = "SELECT * FROM TEST";
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		doThrow(new IdNotAvailableException("")).when(idManager).getPlatformId();

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

	@Test
	public void methodIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[1];
		parameters[0] = "SELECT * FROM TEST";
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredMethodId(methodId);

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

	@Test
	public void sensorTypeIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[1];
		parameters[0] = "SELECT * FROM TEST";
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredSensorTypeId(sensorTypeId);

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

}
