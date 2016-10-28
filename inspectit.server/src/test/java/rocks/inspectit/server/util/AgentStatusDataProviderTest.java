package rocks.inspectit.server.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentStatusDataProvider}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class AgentStatusDataProviderTest extends TestBase {

	/**
	 * Class under test.
	 */
	@InjectMocks
	AgentStatusDataProvider agentStatusDataProvider;

	@Mock
	ScheduledExecutorService executorService;

	@Mock
	Logger log;

	public class RegisterConnected extends AgentStatusDataProviderTest {

		@Test
		public void connectFirstTime() {
			long platformIdent = 10L;

			agentStatusDataProvider.registerConnected(platformIdent);

			assertThat(agentStatusDataProvider.getAgentStatusDataMap().entrySet(), hasSize(1));
			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
			assertThat(agentStatusData.getConnectionTimestamp(), is(greaterThan(0L)));
			assertThat(agentStatusData.getLastKeepAliveTimestamp(), is(greaterThan(0L)));
			assertThat(agentStatusData.getMillisSinceLastData(), is(nullValue()));
			assertThat(agentStatusData.getInstrumentationStatus(), is(InstrumentationStatus.UP_TO_DATE));
		}

		@Test
		public void connectTwice() {
			long platformIdent = 10L;

			agentStatusDataProvider.registerConnected(platformIdent);
			long currentTimeMillis = System.currentTimeMillis();
			agentStatusDataProvider.registerConnected(platformIdent);

			assertThat(agentStatusDataProvider.getAgentStatusDataMap().entrySet(), hasSize(1));
			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
			assertThat(agentStatusData.getConnectionTimestamp(), is(greaterThanOrEqualTo(currentTimeMillis)));
			assertThat(agentStatusData.getLastKeepAliveTimestamp(), is(greaterThanOrEqualTo(currentTimeMillis)));
			assertThat(agentStatusData.getMillisSinceLastData(), is(nullValue()));
			assertThat(agentStatusData.getInstrumentationStatus(), is(InstrumentationStatus.UP_TO_DATE));
		}

	}

	public class registerDisconnected extends AgentStatusDataProviderTest {

		@Test
		public void neverConnected() {
			long platformIdent = 10L;

			boolean disconnected = agentStatusDataProvider.registerDisconnected(platformIdent);

			assertThat(disconnected, is(false));
		}

		@Test
		public void disconnected() {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);

			boolean disconnected = agentStatusDataProvider.registerDisconnected(platformIdent);

			assertThat(disconnected, is(true));
			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.DISCONNECTED));
		}
	}

	public class RegisterDataSent extends AgentStatusDataProviderTest {

		@Test
		public void neverConnected() {
			long platformIdent = 10L;

			agentStatusDataProvider.registerDataSent(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(nullValue()));
		}

		@Test
		public void connected() {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);

			agentStatusDataProvider.registerDataSent(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getMillisSinceLastData(), is(notNullValue()));
		}
	}

	public class HandleKeepAliveSignal extends AgentStatusDataProviderTest {

		@Test
		public void neverConnected() {
			long platformIdent = 10L;

			agentStatusDataProvider.handleKeepAliveSignal(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(nullValue()));
		}

		@Test
		public void connected() {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);
			long currentTimeMillis = System.currentTimeMillis();

			agentStatusDataProvider.handleKeepAliveSignal(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
			assertThat(agentStatusData.getLastKeepAliveTimestamp(), is(greaterThanOrEqualTo(currentTimeMillis)));
		}

		@Test
		public void afterTimeout() {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);
			agentStatusDataProvider.registerKeepAliveTimeout(platformIdent);
			long currentTimeMillis = System.currentTimeMillis();

			agentStatusDataProvider.handleKeepAliveSignal(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
			assertThat(agentStatusData.getLastKeepAliveTimestamp(), is(greaterThanOrEqualTo(currentTimeMillis)));
		}
	}

	public class RegisterKeepAliveTimeout extends AgentStatusDataProviderTest {

		@Test
		public void neverConnected() {
			long platformIdent = 10L;

			agentStatusDataProvider.registerKeepAliveTimeout(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(nullValue()));
		}

		@Test
		public void connected() {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);

			agentStatusDataProvider.registerKeepAliveTimeout(platformIdent);

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.NO_KEEP_ALIVE));
		}
	}

	// tests runnable in fact
	public class AfterPropertiesSet extends AgentStatusDataProviderTest {

		@BeforeMethod
		public void init() {
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Runnable runnable = (Runnable) invocation.getArguments()[0];
					runnable.run();
					return null;
				}
			}).when(executorService).scheduleAtFixedRate(Mockito.<Runnable> any(), anyLong(), anyLong(), Mockito.<TimeUnit> any());
		}

		@Test
		public void noAgent() throws Exception {
			agentStatusDataProvider.afterPropertiesSet();

			assertThat(agentStatusDataProvider.getAgentStatusDataMap().entrySet(), is(empty()));
		}

		@Test
		public void noTimeout() throws Exception {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);

			agentStatusDataProvider.afterPropertiesSet();

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
		}

		@Test
		public void timeout() throws Exception {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);
			Thread.sleep(IKeepAliveService.KA_TIMEOUT + 1);

			agentStatusDataProvider.afterPropertiesSet();

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.NO_KEEP_ALIVE));
		}

		@Test
		public void disconnected() throws Exception {
			long platformIdent = 10L;
			agentStatusDataProvider.registerConnected(platformIdent);
			agentStatusDataProvider.registerDisconnected(platformIdent);

			agentStatusDataProvider.afterPropertiesSet();

			AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
			assertThat(agentStatusData, is(notNullValue()));
			assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.DISCONNECTED));
		}
	}

}
