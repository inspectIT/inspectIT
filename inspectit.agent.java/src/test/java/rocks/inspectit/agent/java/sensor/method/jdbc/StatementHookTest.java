package rocks.inspectit.agent.java.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.impl.PlatformManager;
import rocks.inspectit.agent.java.util.Timer;
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
	public void oneStatement() {
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

		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(platformManager).getPlatformId();

		ArgumentCaptor<SqlStatementData> captor = ArgumentCaptor.forClass(SqlStatementData.class);
		verify(coreService).addDefaultData(captor.capture());
		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);

		SqlStatementData sqlData = captor.getValue();
		assertThat(sqlData.getPlatformIdent(), is(platformId));
		assertThat(sqlData.getMethodIdent(), is(methodId));
		assertThat(sqlData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(sqlData.getTimeStamp(), is(not(nullValue())));
		assertThat(sqlData.getCount(), is(1L));
		assertThat(sqlData.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getMin(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getMax(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getSql(), is((String) parameters[0]));
		assertThat(sqlData.isCharting(), is(false));
	}

	@Test
	public void oneStatementDelegatesStatement() {
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

		statementHook2.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();
		statementHook2.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(platformManager).getPlatformId();

		statementHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		statementHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(platformManager, times(2)).getPlatformId();

		ArgumentCaptor<SqlStatementData> captor = ArgumentCaptor.forClass(SqlStatementData.class);
		verify(coreService, times(2)).addDefaultData(captor.capture());
		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);

		SqlStatementData sqlData = captor.getValue();
		assertThat(sqlData.getPlatformIdent(), is(platformId));
		assertThat(sqlData.getMethodIdent(), is(methodId));
		assertThat(sqlData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(sqlData.getTimeStamp(), is(not(nullValue())));
		assertThat(sqlData.getCount(), is(1L));
		assertThat(sqlData.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getMin(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getMax(), is(secondTimerValue - firstTimerValue));
		assertThat(sqlData.getSql(), is((String) parameters[0]));
		assertThat(sqlData.isCharting(), is(false));
	}

}
