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
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteDefaultHttpInserterHook;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.apache.httpclient.RemoteApacheHttpClientV43InserterHook;
import info.novatec.inspectit.communication.data.RemoteCallData;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	private HttpResponse result;

	@Mock
	private CloseableHttpClient closeableHttpClient;

	private HttpHost httpHost;
	
	@Mock
	private HttpRequest httpRequest;
	
	private HttpContext httpContext;

	@Mock
	private RemoteDefaultHttpInserterHook webrequestHttpHook;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		Map<String, Object> map = new HashMap<String, Object>();
		MapUtils.putAll(map, new String[][] { { "sessioncapture", "true" } });
		webrequestHttpHook = new RemoteApacheHttpClientV43InserterHook(idManager, remoteIdentificationManager);
	}

	@Test
	public void oneRecordThatIsApacheHttpClientV43() throws IdNotAvailableException {

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
		
		ProtocolVersion protocolVersion = new ProtocolVersion("http", 1, 1);
		
		RequestLine requestLine = new BasicRequestLine("GET", uri, protocolVersion);
		when(httpRequest.getRequestLine()).thenReturn(requestLine);
		
		StatusLine statusLine = new BasicStatusLine(protocolVersion, responseCode, null);
		when(result.getStatusLine()).thenReturn(statusLine);
		
		Object[] parameters = new Object[] { httpHost, httpRequest, httpContext};

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, closeableHttpClient, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, closeableHttpClient, parameters, result, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, closeableHttpClient, parameters, result, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(registeredSensorTypeId), eq(registeredMethodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));
		assertThat(sample.getUrl(), is(equalTo(captor.getValue().getUrl())));
		assertThat(sample.getResponseCode(), is(equalTo(captor.getValue().getResponseCode())));

	}
}
