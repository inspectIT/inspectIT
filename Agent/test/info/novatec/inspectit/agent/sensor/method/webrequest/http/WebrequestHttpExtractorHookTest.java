package info.novatec.inspectit.agent.sensor.method.webrequest.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.WebrequestDefaultHttpInserterHook;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.apache.WebrequestApacheHttpInserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WebrequestHttpExtractorHookTest extends AbstractLogSupport {

	/**
	 * Name of the InspectItHeader.
	 */
	protected static final String INSPECTIT_HEADER = "InspectITHeader";

	@Mock
	private Timer timer;

	@Mock
	private IIdManager idManager;

	@Mock
	private RemoteIdentificationManager remoteIdentificationManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private ThreadMXBean threadMXBean;

	@Mock
	private Object result;

	@Mock
	private HttpServlet servlet;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpURLConnection httpURLConnection;

	@Mock
	private ServletRequest servletRequest;

	@Mock
	private HttpSession session;

	private WebrequestDefaultHttpInserterHook webrequestHttpHook;

	private final long platformId = 1L;
	private final long methodId = 1L;
	private final long sensorTypeId = 3L;
	private final long registeredMethodId = 13L;
	private final long registeredSensorTypeId = 7L;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		when(threadMXBean.isThreadCpuTimeEnabled()).thenReturn(true);
		when(threadMXBean.isThreadCpuTimeSupported()).thenReturn(true);

		Map<String, Object> map = new HashMap<String, Object>();
		MapUtils.putAll(map, new String[][] { { "sessioncapture", "true" } });
		webrequestHttpHook = new WebrequestApacheHttpInserterHook(idManager, timer, remoteIdentificationManager, threadMXBean);
	}

	@Test
	public void oneRecordThatIsApacheTomcatHttp() throws IdNotAvailableException {

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		Long identification = 250l;

		Map<String, Object> sensorConfig = new HashMap<String, Object>();
		sensorConfig.put("inserter", "tomcat");

		RemoteCallData sample = Mockito.mock(RemoteCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);

		ArgumentCaptor<RemoteCallData> captor = ArgumentCaptor.forClass(RemoteCallData.class);

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);

		when(threadMXBean.getCurrentThreadCpuTime()).thenReturn(firstCpuTimerValue).thenReturn(secondCpuTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);
		when(registeredSensorConfig.getSettings()).thenReturn(sensorConfig);
		Object[] parameters = new Object[] { servletRequest };

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, httpURLConnection, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, httpURLConnection, parameters, result, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, httpURLConnection, parameters, result, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(registeredSensorTypeId), eq(registeredMethodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));

		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void oneRecordThatIsJBossHttp() throws IdNotAvailableException {

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		Long identification = 250l;

		Map<String, Object> sensorConfig = new HashMap<String, Object>();
		sensorConfig.put("inserter", "jboss");

		RemoteCallData sample = Mockito.mock(RemoteCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);

		ArgumentCaptor<RemoteCallData> captor = ArgumentCaptor.forClass(RemoteCallData.class);

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);

		when(threadMXBean.getCurrentThreadCpuTime()).thenReturn(firstCpuTimerValue).thenReturn(secondCpuTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);
		when(registeredSensorConfig.getSettings()).thenReturn(sensorConfig);

		Map<String, Object> additinalHeader = new HashMap<String, Object>();
		// just need the additinalHeader Parameter for InspectIT Header, other parameter are not
		// used
		Object[] parameters = new Object[] { null, null, null, additinalHeader };

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, servlet, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, servlet, parameters, result, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(registeredSensorTypeId), eq(registeredMethodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));

		Mockito.verifyZeroInteractions(result);
	}
}
