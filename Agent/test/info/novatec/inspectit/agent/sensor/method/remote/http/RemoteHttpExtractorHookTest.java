package info.novatec.inspectit.agent.sensor.method.remote.http;

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
import info.novatec.inspectit.agent.sensor.method.remote.http.mock.ApacheHttpClientV40Mock;
import info.novatec.inspectit.agent.sensor.method.remote.http.mock.ApacheHttpRequestV40Mock;
import info.novatec.inspectit.agent.sensor.method.remote.http.mock.ApacheHttpResponseV40Mock;
import info.novatec.inspectit.agent.sensor.method.remote.http.mock.ApacheRequestLineMock;
import info.novatec.inspectit.agent.sensor.method.remote.http.mock.ApacheStatusLineMock;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteDefaultHttpInserterHook;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.apache.httpclient.RemoteApacheHttpClientV40InserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;

import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class is still the old style because the tested methods are called one after the other. To test each method separate has no benefit.
 * 
 * @author Thomas Kluge
 *
 */
public class RemoteHttpExtractorHookTest extends AbstractLogSupport {

	@Mock
	private IIdManager idManager;

	@Mock
	private RemoteIdentificationManager remoteIdentificationManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private ApacheHttpResponseV40Mock result;
	
	@Mock
	private ApacheStatusLineMock statusLine;

	@Mock
	private ApacheHttpClientV40Mock apacheHttpClientV40Mock;
	
	@Mock
	private ApacheHttpRequestV40Mock httpRequest;
	
	@Mock
	private ApacheRequestLineMock requestLine;

	@Mock
	private RemoteDefaultHttpInserterHook webrequestHttpHook;

	@BeforeMethod
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sessioncapture", "false");
		webrequestHttpHook = new RemoteApacheHttpClientV40InserterHook(idManager, remoteIdentificationManager);
	}

	@Test
	public void oneRecordThatIsApacheHttpClientV40 () throws IdNotAvailableException {

		Long identification = 250l;
		String uri = "/inspectIT/test";
		int responseCode = 200;
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;
		long registeredMethodId = 13L;
		long registeredSensorTypeId = 7L;

		Map<String, Object> sensorConfig = new HashMap<String, Object>();
		sensorConfig.put("inserter", "tomcat");

		RemoteCallData sample = Mockito.mock(RemoteCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);
		when(sample.getUrl()).thenReturn(uri);
		when(sample.getResponseCode()).thenReturn(responseCode);

		ArgumentCaptor<RemoteCallData> captor = ArgumentCaptor.forClass(RemoteCallData.class);

		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);
		when(registeredSensorConfig.getSettings()).thenReturn(sensorConfig);
		
		when(httpRequest.getRequestLine()).thenReturn(requestLine);
		when(requestLine.getUri()).thenReturn(uri);
		
		when(result.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(responseCode);
		
		Object[] parameters = new Object[] { null, httpRequest, null};

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, apacheHttpClientV40Mock, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, apacheHttpClientV40Mock, parameters, result, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, apacheHttpClientV40Mock, parameters, result, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(registeredSensorTypeId), eq(registeredMethodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));
		assertThat(sample.getUrl(), is(equalTo(captor.getValue().getUrl())));
		assertThat(sample.getResponseCode(), is(equalTo(captor.getValue().getResponseCode())));

	}
}
