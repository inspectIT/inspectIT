package rocks.inspectit.server.influx.builder.eum;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import rocks.inspectit.server.influx.builder.AbstractPointBuilderTest;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.eum.AbstractRequest;
import rocks.inspectit.shared.all.communication.data.eum.EUMElementID;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * @author Jonas Kunz
 *
 */
public abstract class AbstractEUMRequestPointBuilderTest extends AbstractPointBuilderTest {

	@Mock
	protected ICachedDataService cachedDataService;

	@Mock
	protected UserSessionInfo sessionInfo;

	@Mock
	protected PageLoadRequest pageLoadRequest;

	protected static final long PLATFORM_IDENT = 123;
	protected static final String PLATFORM_NAME = "MyAgent";

	protected static final String BROWSER = "CoolBrowser";
	protected static final String DEVICE = "CoolOS";
	protected static final String LANGUAGE = "newspeak";

	protected static final String URL = "/somePath/child";
	protected static final String PAGELOAD_URL = "/somePath";

	protected static final long SESSION_ID = 789;
	protected static final long TAB_ID = 1011;

	protected void initMocks(AbstractRequest targetElement) {

		// assign platformIdent

		when(sessionInfo.getPlatformIdent()).thenReturn(PLATFORM_IDENT);
		when(pageLoadRequest.getPlatformIdent()).thenReturn(PLATFORM_IDENT);

		PlatformIdent pid = mock(PlatformIdent.class);
		when(pid.getAgentName()).thenReturn(PLATFORM_NAME);
		when(cachedDataService.getPlatformIdentForId(PLATFORM_IDENT)).thenReturn(pid);

		// assign IDs

		EUMElementID sessInfoId = new EUMElementID();
		sessInfoId.setSessionID(SESSION_ID);
		sessInfoId.setTabID(TAB_ID);
		sessInfoId.setLocalID(1);
		when(sessionInfo.getID()).thenReturn(sessInfoId);

		EUMElementID plrId = new EUMElementID();
		plrId.setSessionID(SESSION_ID);
		plrId.setTabID(TAB_ID);
		plrId.setLocalID(2);
		when(pageLoadRequest.getID()).thenReturn(plrId);

		if (targetElement != null) {
			EUMElementID targetElemId = new EUMElementID();
			targetElemId.setSessionID(SESSION_ID);
			targetElemId.setTabID(TAB_ID);
			targetElemId.setLocalID(3);
			when(targetElement.getID()).thenReturn(targetElemId);
			when(targetElement.getUrl()).thenReturn(URL);
			when(targetElement.getPlatformIdent()).thenReturn(PLATFORM_IDENT);
		}

		// assign data
		when(sessionInfo.getBrowser()).thenReturn(BROWSER);
		when(sessionInfo.getDevice()).thenReturn(DEVICE);
		when(sessionInfo.getLanguage()).thenReturn(LANGUAGE);

		when(pageLoadRequest.getUrl()).thenReturn(PAGELOAD_URL);
	}

}
