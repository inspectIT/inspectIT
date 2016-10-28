package rocks.inspectit.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class GlobalDataAccessServiceTest extends TestBase {

	/**
	 * Class under test.
	 */
	@InjectMocks
	GlobalDataAccessService globalDataAccessService;

	@Mock
	Logger log;

	@Mock
	PlatformIdentDao platformIdentDao;

	@Mock
	DefaultDataDao defaultDataDao;

	@Mock
	AgentStatusDataProvider agentStatusProvider;

	@Mock
	ApplicationEventPublisher eventPublisher;

	/**
	 * Tests the {@link GlobalDataAccessService#deleteAgent(long)} method.
	 */
	public static class DeleteAgent extends GlobalDataAccessServiceTest {

		/**
		 * No delete enabled when platform ident can not be found.
		 */
		@Test(expectedExceptions = { BusinessException.class })
		public void testNonExistingAgentDelete() throws BusinessException {
			long platformId = 10L;
			when(platformIdentDao.load(Long.valueOf(platformId))).thenReturn(null);

			globalDataAccessService.deleteAgent(platformId);
		}

		/**
		 * No delete enabled when agent is connected.
		 */
		@Test(expectedExceptions = { BusinessException.class })
		public void testConnectedAgentDelete() throws BusinessException {
			long platformId = 10L;
			PlatformIdent platformIdent = new PlatformIdent();
			platformIdent.setId(platformId);
			when(platformIdentDao.load(Long.valueOf(platformId))).thenReturn(platformIdent);

			Map<Long, AgentStatusData> map = new HashMap<>(1);
			AgentStatusData agentStatusData = new AgentStatusData(AgentConnection.CONNECTED);
			map.put(platformId, agentStatusData);
			when(agentStatusProvider.getAgentStatusDataMap()).thenReturn(map);

			globalDataAccessService.deleteAgent(platformId);
		}

		/**
		 * Delete enabled when Agent is not connected.
		 */
		@Test
		public void testAgentDelete() throws BusinessException {
			long platformId = 10L;
			PlatformIdent platformIdent = new PlatformIdent();
			platformIdent.setId(platformId);
			when(platformIdentDao.load(Long.valueOf(platformId))).thenReturn(platformIdent);

			Map<Long, AgentStatusData> map = new HashMap<>(1);
			AgentStatusData agentStatusData = new AgentStatusData(AgentConnection.DISCONNECTED);
			map.put(platformId, agentStatusData);
			when(agentStatusProvider.getAgentStatusDataMap()).thenReturn(map);

			globalDataAccessService.deleteAgent(platformId);

			verify(platformIdentDao, times(1)).delete(platformIdent);
			verify(defaultDataDao, times(1)).deleteAll(platformId);
			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher, times(1)).publishEvent(captor.capture());

			AgentDeletedEvent event = (AgentDeletedEvent) captor.getValue();
			assertThat(event.getPlatformIdent(), is(platformIdent));
		}
	}
}
