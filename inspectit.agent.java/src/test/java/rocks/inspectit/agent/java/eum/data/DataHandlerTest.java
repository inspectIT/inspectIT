package rocks.inspectit.agent.java.eum.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;

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
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings({ "PMD" })
public class DataHandlerTest extends TestBase {

	private static final String MODULES_DEMOVALUE = "m12";
	private static final long TABID_DEMOVALUE = 12345;
	private static final long SESSID_DEMOVALUE = 6789;

	private static final long PLATFORM_ID = 17L;


	@Mock
	Logger inejctedLog;

	@Mock
	IdGenerator idGenerator;

	@Mock
	IPlatformManager platformManager;

	@Mock
	ICoreService coreService;

	ArgumentCaptor<Beacon> sentElements;

	@InjectMocks
	DataHandler dataHandler;


	@BeforeMethod
	public void initMocks() {
		when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);
		sentElements = ArgumentCaptor.forClass(Beacon.class);
		doNothing().when(coreService).addEUMData(sentElements.capture());
		when(idGenerator.generateSessionID()).thenReturn(SESSID_DEMOVALUE);
		when(idGenerator.generateTabID()).thenReturn(TABID_DEMOVALUE);
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

			beacon.put("sessionID", "" + SESSID_DEMOVALUE);
			beacon.put("tabID", "" + TABID_DEMOVALUE);
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);
			data.add(sessInfo);

			String beaconJson = beacon.toString();
			dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(1)).addEUMData(any(Beacon.class));
			Beacon sent = sentElements.getValue();

			assertThat(sent.getData().size(), equalTo(1));
			assertThat(sent.getData().get(0), instanceOf(UserSessionInfo.class));
			UserSessionInfo sentInfo = (UserSessionInfo) sent.getData().get(0);
			assertThat(sentInfo.getBrowser(), equalTo("Firefox"));
			assertThat(sentInfo.getDevice(), equalTo("iOS"));
			assertThat(sentInfo.getLanguage(), equalTo("de"));
			assertThat(sentInfo.getID().getSessionID(), equalTo(SESSID_DEMOVALUE));
		}

		@Test
		public void testIDAssignment() throws JsonProcessingException, IOException {
			ObjectNode beacon = new ObjectNode(JsonNodeFactory.instance);

			beacon.put("sessionID", "" + Beacon.REQUEST_NEW_SESSION_ID_MARKER);
			beacon.put("tabID", "" + Beacon.REQUEST_NEW_TAB_ID_MARKER);
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);

			String beaconJson = beacon.toString();
			String responseJson = dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(Beacon.class));

			JsonNode response = (new ObjectMapper()).readTree(responseJson);
			assertThat(response, instanceOf(ObjectNode.class));
			assertThat(response.get("sessionID").asLong(), equalTo(SESSID_DEMOVALUE));
			assertThat(response.get("tabID").asLong(), equalTo(TABID_DEMOVALUE));
		}

		@Test
		public void testInvalidBeaconSyntax() {
			dataHandler.insertBeacon("nope { }");
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(Beacon.class));

		}

		@Test
		public void testInvalidBeaconContent() {
			ObjectNode beacon = new ObjectNode(JsonNodeFactory.instance);
			ObjectNode sessInfo = new ObjectNode(JsonNodeFactory.instance);
			sessInfo.put("type", "unkownType");
			sessInfo.put("browser", "Firefox");
			sessInfo.put("device", "iOS");
			sessInfo.put("language", "de");

			beacon.put("sessionID", "" + SESSID_DEMOVALUE);
			beacon.put("tabID", "" + TABID_DEMOVALUE);
			beacon.put("activeAgentModules", MODULES_DEMOVALUE);
			ArrayNode data = beacon.arrayNode();
			beacon.put("data", data);
			data.add(sessInfo);

			String beaconJson = beacon.toString();

			dataHandler.insertBeacon(beaconJson);
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(Beacon.class));

		}
	}

}
