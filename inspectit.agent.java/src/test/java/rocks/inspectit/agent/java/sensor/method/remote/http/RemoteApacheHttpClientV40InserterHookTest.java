package rocks.inspectit.agent.java.sensor.method.remote.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.ApacheHttpRequestV40Mock;
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.ApacheHttpResponseV40Mock;
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.ApacheRequestLineMock;
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.ApacheStatusLineMock;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.apache.RemoteApacheHttpClientV40InserterHook;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;

/**
 * Test class is still the old style because the tested methods are called one after the other. To
 * test each method separate has no benefit.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteApacheHttpClientV40InserterHookTest extends AbstractLogSupport {

	@Mock
	private IPlatformManager platformManager;

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
	private ApacheHttpRequestV40Mock httpRequest;

	@Mock
	private ApacheRequestLineMock requestLine;

	private RemoteApacheHttpClientV40InserterHook webrequestHttpHook;

	@BeforeMethod
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sessioncapture", "false");
		webrequestHttpHook = new RemoteApacheHttpClientV40InserterHook(platformManager, remoteIdentificationManager);
	}

	@Test
	public void oneRecordThatIsApacheHttpClientV40() throws IdNotAvailableException {

		Long identification = 250l;
		String uri = "/inspectIT/test";
		int responseCode = 200;
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;

		RemoteHttpCallData sample = Mockito.mock(RemoteHttpCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);
		when(sample.getUrl()).thenReturn(uri);
		when(sample.getResponseCode()).thenReturn(responseCode);

		ArgumentCaptor<RemoteHttpCallData> captor = ArgumentCaptor.forClass(RemoteHttpCallData.class);

		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);

		when(httpRequest.getHeader(RemoteConstants.INSPECTIT_HTTP_HEADER)).thenReturn(null);

		when(httpRequest.getRequestLine()).thenReturn(requestLine);
		when(requestLine.getUri()).thenReturn(uri);

		when(result.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(responseCode);

		Object[] parameters = new Object[] { null, httpRequest, null };

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, null, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, null, parameters, result, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, null, parameters, result, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));
		assertThat(sample.getUrl(), is(equalTo(captor.getValue().getUrl())));
		assertThat(sample.getResponseCode(), is(equalTo(captor.getValue().getResponseCode())));

	}
}
