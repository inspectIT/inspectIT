package info.novatec.inspectit.cmr.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link AgentStatusDataProvider}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class AgentStatusDataProviderTest {

	/**
	 * Class under test.
	 */
	private AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void init() {
		agentStatusDataProvider = new AgentStatusDataProvider();
	}

	/**
	 * Test the correct change of statuses.
	 */
	@Test
	public void statuses() {
		long platformIdent = 10L;
		agentStatusDataProvider.registerConnected(platformIdent);
		assertThat(agentStatusDataProvider.getAgentStatusDataMap().size(), is(1));

		AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
		assertThat(agentStatusData, is(notNullValue()));
		assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
		assertThat(agentStatusData.getMillisSinceLastData(), is(nullValue()));

		long millis = System.currentTimeMillis();
		agentStatusDataProvider.registerDataSent(platformIdent);
		assertThat(agentStatusDataProvider.getAgentStatusDataMap().size(), is(1));

		agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
		assertThat(agentStatusData, is(notNullValue()));
		assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.CONNECTED));
		assertThat(agentStatusData.getMillisSinceLastData(), is(lessThanOrEqualTo(System.currentTimeMillis() - millis)));

		agentStatusDataProvider.registerDisconnected(platformIdent);
		assertThat(agentStatusDataProvider.getAgentStatusDataMap().size(), is(1));

		agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformIdent);
		assertThat(agentStatusData, is(notNullValue()));
		assertThat(agentStatusData.getAgentConnection(), is(AgentConnection.DISCONNECTED));

		agentStatusDataProvider.registerDeleted(platformIdent);
		assertThat(agentStatusDataProvider.getAgentStatusDataMap().size(), is(0));
	}

	/**
	 * Test that initially there is not information.
	 */
	@Test
	public void noStatusAvailable() {
		assertThat(agentStatusDataProvider.getAgentStatusDataMap().size(), is(0));
	}
}
