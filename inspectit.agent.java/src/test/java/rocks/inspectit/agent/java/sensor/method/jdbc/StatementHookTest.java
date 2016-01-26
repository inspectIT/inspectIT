package rocks.inspectit.agent.java.sensor.method.jdbc;

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


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IObjectStorage;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.core.impl.PlatformManager;
import rocks.inspectit.agent.java.sensor.method.jdbc.ConnectionMetaDataStorage;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementHook;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementReflectionCache;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;

@SuppressWarnings("PMD")
public class StatementHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private PlatformManager platformManager;

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
		statementHook = new StatementHook(timer, platformManager, connectionMetaDataStorage, statementReflectionCache, parameter);
		statementHook2 = new StatementHook(timer, platformManager, connectionMetaDataStorage, statementReflectionCache, parameter);

		List<String> list = new ArrayList<String>();
		list.add("java.lang.String");
		when(registeredSensorConfig.getParameterTypes()).thenReturn(list);
	}

	@Test
	public void oneStatement() throws IdNotAvailableException {
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
		when(platformManager.getPlatformId()).thenReturn(platformId);

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(platformManager).getPlatformId();

		Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
		SqlStatementData bsqld = new SqlStatementData(timestamp, platformId, sensorTypeId, methodId);
		bsqld.setSql((String) parameters[0]);

		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), (MethodSensorData) Mockito.anyObject());
		verify(coreService).getMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject());
		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);
	}

	@Test
	public void oneStatementDelegatesStatement() throws IdNotAvailableException {
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
		when(platformManager.getPlatformId()).thenReturn(platformId);

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

		verify(platformManager, times(2)).getPlatformId();

		Timestamp timestamp = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
		SqlStatementData bsqld = new SqlStatementData(timestamp, platformId, sensorTypeId, methodId);
		bsqld.setSql((String) parameters[0]);

		verify(coreService, times(2)).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), (MethodSensorData) Mockito.anyObject());
		verify(coreService, times(2)).getMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject());
		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);
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
		doThrow(new IdNotAvailableException("")).when(platformManager).getPlatformId();

		statementHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addObjectStorage(anyLong(), anyLong(), anyString(), (IObjectStorage) isNull());
	}

}
