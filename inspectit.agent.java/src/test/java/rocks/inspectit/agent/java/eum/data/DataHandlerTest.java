package rocks.inspectit.agent.java.eum.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.codehaus.jackson.map.ObjectMapper;
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
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMData;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings({ "PMD" })
public class DataHandlerTest extends TestBase {

	private static final String SESSID_DEMOVALUE = "12345";

	private static final long PLATFORM_ID = 17L;

	private ObjectMapper mapper = new ObjectMapper();

	@Mock
	Logger inejctedLog;

	@Mock
	IPlatformManager platformManager;

	@Mock
	ICoreService coreService;

	ArgumentCaptor<AbstractEUMData> sentElements;

	@InjectMocks
	DataHandler dataHandler;


	@BeforeMethod
	public void initMocks() throws IdNotAvailableException {
		when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);
		sentElements = ArgumentCaptor.forClass(AbstractEUMData.class);
		Mockito.doNothing().when(coreService).addEUMData(sentElements.capture());
	}

	public String buildBeaconJson(String typeName, AbstractEUMData data) {
		ObjectNode tree = mapper.valueToTree(data);
		tree.put("type", typeName);
		tree.remove("id");
		tree.remove("platformIdent");
		tree.remove("sensorTypeIdent");
		tree.remove("timeStamp");
		return tree.toString();

	}

	public static class InsertBeacon extends DataHandlerTest {

		@Test
		public void testSessionInfoSending() {
			UserSessionInfo r = new UserSessionInfo();
			r.setSessionId(SESSID_DEMOVALUE);
			r.setBrowser("Firefox");
			r.setDevice("iOS");
			r.setLanguage("de");

			String beacon = buildBeaconJson("userSession", r);
			dataHandler.insertBeacon(beacon);
			Mockito.verify(coreService, Mockito.times(1)).addEUMData(any(UserSessionInfo.class));
			UserSessionInfo sent = (UserSessionInfo) sentElements.getValue();

			assertThat(sent.getBrowser(), is(r.getBrowser()));
			assertThat(sent.getSessionId(), is(r.getSessionId()));
			assertThat(sent.getDevice(), is(r.getDevice()));
			assertThat(sent.getLanguage(), is(r.getLanguage()));
			assertThat(sent.getPlatformIdent(), is(PLATFORM_ID));
		}

		@Test
		public void testInvalidBeaconSyntax() {
			dataHandler.insertBeacon("a{\"type\":\"userSession\",\"device\":\"Windows\",\"browser\":\"Firefox\",\"language\":\"en-US\",\"sessionId\":\"eum_agent2_1476445972407_2\"}");
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(AbstractEUMData.class));

		}

		@Test
		public void testInvalidBeaconContent() {
			dataHandler.insertBeacon("{\"type\":\"nothing\",\"desdfdsfice\":\"sdfsdf\"}");
			Mockito.verify(coreService, Mockito.times(0)).addEUMData(any(AbstractEUMData.class));

		}
	}

}
