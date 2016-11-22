package rocks.inspectit.agent.java.eum.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.communication.data.eum.Beacon;
import rocks.inspectit.shared.all.communication.data.eum.EUMBeaconElement;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings({ "PMD" })
public class DataHandlerTest extends TestBase {

	private static final String MODULES_DEMOVALUE = "m12";
	private static final long TABID_DEMOVALUE = 12345;
	private static final long SESSID_DEMOVALUE = 123456;

	private static final long PLATFORM_ID = 17L;


	@Mock
	Logger inejctedLog;

	@Mock
	IPlatformManager platformManager;

	@Mock
	ICoreService coreService;

	ArgumentCaptor<EUMBeaconElement> sentElements;

	@InjectMocks
	DataHandler dataHandler;


	@BeforeMethod
	public void initMocks() {
		when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);
		sentElements = ArgumentCaptor.forClass(EUMBeaconElement.class);
		doNothing().when(coreService).addEUMData(sentElements.capture());
	}


	public static class InsertBeacon extends DataHandlerTest {

		@Test
		public void testSessionInfoSending() {
			ObjectNode beacon = new ObjectNode(JsonNodeFactory.instance);
			ObjectNode sessInfo = new ObjectNode(JsonNodeFactory.instance);
			sessInfo.put("type", "metaInfo");
			sessInfo.put("browser", "Firefox");
			sessInfo.put("device", "iOS");
			sessInfo.put("language", "de");

			beacon.put("sessionID", "" + Long.toString(SESSID_DEMOVALUE, 16));
			beacon.put("tabID", "" + Long.toString(TABID_DEMOVALUE, 16));
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);
			data.add(sessInfo);

			String beaconJson = beacon.toString();
			dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(1)).addEUMData(any(EUMBeaconElement.class));
			List<EUMBeaconElement> sent = sentElements.getAllValues();

			assertThat(sent.size(), equalTo(1));
			assertThat(sent.get(0), instanceOf(UserSessionInfo.class));
			UserSessionInfo sentInfo = (UserSessionInfo) sent.get(0);
			assertThat(sentInfo.getBrowser(), equalTo("Firefox"));
			assertThat(sentInfo.getDevice(), equalTo("iOS"));
			assertThat(sentInfo.getLanguage(), equalTo("de"));
			assertThat(sentInfo.getSessionId(), equalTo(SESSID_DEMOVALUE));
		}

		@Test
		public void testIDAssignment() throws JsonProcessingException, IOException {
			ObjectNode beacon = new ObjectNode(JsonNodeFactory.instance);

			beacon.put("sessionID", "" + Long.toString(Beacon.REQUEST_NEW_SESSION_ID_MARKER, 16));
			beacon.put("tabID", "" + Long.toString(Beacon.REQUEST_NEW_TAB_ID_MARKER, 16));
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);

			String beaconJson = beacon.toString();
			String responseJson = dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(EUMBeaconElement.class));

			JsonNode response = (new ObjectMapper()).readTree(responseJson);
			assertThat(response, instanceOf(ObjectNode.class));
			assertThat(response.get("sessionID").asText(), notNullValue());
			assertThat(response.get("tabID").asText(), notNullValue());
		}

		@Test
		public void testInvalidBeaconSyntax() {
			dataHandler.insertBeacon("nope { }");
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(EUMBeaconElement.class));

		}

		@Test
		public void testInvalidBeaconContent() {
			ObjectNode beacon = new ObjectNode(JsonNodeFactory.instance);
			ObjectNode sessInfo = new ObjectNode(JsonNodeFactory.instance);
			sessInfo.put("type", "unkownType");
			sessInfo.put("browser", "Firefox");
			sessInfo.put("device", "iOS");
			sessInfo.put("language", "de");

			beacon.put("sessionID", "" + Long.toString(SESSID_DEMOVALUE, 16));
			beacon.put("tabID", "" + Long.toString(TABID_DEMOVALUE, 16));
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);
			data.add(sessInfo);

			String beaconJson = beacon.toString();

			dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(EUMBeaconElement.class));

		}
	}

}
