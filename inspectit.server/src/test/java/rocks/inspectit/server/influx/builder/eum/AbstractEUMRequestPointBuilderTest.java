package rocks.inspectit.server.influx.builder.eum;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.Mockito;

import rocks.inspectit.server.influx.builder.AbstractPointBuilderTest;
import rocks.inspectit.shared.all.communication.data.eum.AbstractRequest;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * @author Jonas Kunz
 *
 */
public abstract class AbstractEUMRequestPointBuilderTest extends AbstractPointBuilderTest {

	@Mock
	protected UserSessionInfo sessionInfo;

	@Mock
	protected PageLoadRequest pageLoadRequest;

	@Mock
	protected EUMSpan pageLoadRequestSpan;

	protected static final String BROWSER = "CoolBrowser";
	protected static final String DEVICE = "CoolOS";
	protected static final String LANGUAGE = "newspeak";

	protected static final String URL = "/somePath/child";
	protected static final String PAGELOAD_URL = "/somePath";

	protected static final long SESSION_ID = 789;
	protected static final long TAB_ID = 1011;


	protected void initMocks(AbstractRequest targetElement) {

		when(pageLoadRequestSpan.getDetails()).thenReturn(pageLoadRequest);
		when(pageLoadRequest.getOwningSpan()).thenReturn(pageLoadRequestSpan);

		when(sessionInfo.getSessionId()).thenReturn(SESSION_ID);

		when(pageLoadRequestSpan.getSessionId()).thenReturn(SESSION_ID);
		when(pageLoadRequestSpan.getTabId()).thenReturn(TAB_ID);

		if (targetElement != null) {
			EUMSpan span = Mockito.mock(EUMSpan.class);
			when(span.getDetails()).thenReturn(targetElement);
			when(targetElement.getOwningSpan()).thenReturn(span);

			when(span.getSessionId()).thenReturn(SESSION_ID);
			when(span.getTabId()).thenReturn(TAB_ID);

			when(targetElement.getUrl()).thenReturn(URL);
		}

		// assign data
		when(sessionInfo.getBrowser()).thenReturn(BROWSER);
		when(sessionInfo.getDevice()).thenReturn(DEVICE);
		when(sessionInfo.getLanguage()).thenReturn(LANGUAGE);

		when(pageLoadRequest.getUrl()).thenReturn(PAGELOAD_URL);
	}

}
