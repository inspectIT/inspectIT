/**
 *
 */
package rocks.inspectit.agent.java.sensor.method.remote.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
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
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.JettyHttpExchange;
import rocks.inspectit.agent.java.sensor.method.remote.http.mock.JettyHttpFields;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.http.jetty.RemoteJettyHttpClientV61InserterHook;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;

/**
 * @author Thomas Kluge
 *
 */
public class RemoteJettyHttpClientV61InserterHookTest extends AbstractLogSupport {
	@Mock
	private IPlatformManager platformManager;

	@Mock
	private RemoteIdentificationManager remoteIdentificationManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private JettyHttpExchange httpExchange;

	@Mock
	private JettyHttpFields httpFields;

	private RemoteJettyHttpClientV61InserterHook webrequestHttpHook;

	@BeforeMethod
	public void initTestClass() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("sessioncapture", "false");
		webrequestHttpHook = new RemoteJettyHttpClientV61InserterHook(platformManager, remoteIdentificationManager);
	}

	@Test
	public void oneRecordThatIsJettyHttpClientV61() throws IdNotAvailableException, IOException {

		Long identification = 250l;
		URL uri = new URL("http://inspectIT.de/test");
		int responseCode = 200;
		long platformId = 1L;
		long methodId = 1L;
		long sensorTypeId = 3L;

		RemoteHttpCallData sample = Mockito.mock(RemoteHttpCallData.class);
		when(sample.getIdentification()).thenReturn(identification);
		when(sample.isCalling()).thenReturn(true);
		when(sample.getUrl()).thenReturn(uri.toString());
		// response code 0 because if asynch communication
		when(sample.getResponseCode()).thenReturn(0);

		ArgumentCaptor<RemoteHttpCallData> captor = ArgumentCaptor.forClass(RemoteHttpCallData.class);

		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(remoteIdentificationManager.getNextIdentification()).thenReturn(identification);

		when(httpExchange.getRequestFields()).thenReturn(httpFields);
		when(httpExchange.getURI()).thenReturn(uri.toString());

		Object[] parameters = new Object[] { httpExchange };

		webrequestHttpHook.beforeBody(methodId, sensorTypeId, null, parameters, registeredSensorConfig);

		webrequestHttpHook.firstAfterBody(methodId, sensorTypeId, null, parameters, null, registeredSensorConfig);

		webrequestHttpHook.secondAfterBody(coreService, methodId, sensorTypeId, null, parameters, null, registeredSensorConfig);

		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) Mockito.anyObject(), captor.capture());

		assertThat(sample.getIdentification(), is(equalTo(captor.getValue().getIdentification())));
		assertThat(sample.isCalling(), is(equalTo(captor.getValue().isCalling())));
		assertThat(sample.getUrl(), is(equalTo(captor.getValue().getUrl())));
		assertThat(sample.getResponseCode(), is(equalTo(captor.getValue().getResponseCode())));
	}
}
