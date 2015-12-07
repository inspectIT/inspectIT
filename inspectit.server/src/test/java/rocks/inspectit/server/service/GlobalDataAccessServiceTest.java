package rocks.inspectit.server.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.test.AbstractTestNGLogSupport;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.exception.BusinessException;

@SuppressWarnings("PMD")
public class GlobalDataAccessServiceTest extends AbstractTestNGLogSupport {

	/**
	 * Class under test.
	 */
	private GlobalDataAccessService globalDataAccessService;

	@Mock
	private PlatformIdentDao platformIdentDao;

	@Mock
	private DefaultDataDao defaultDataDao;

	@Mock
	private AgentStatusDataProvider agentStatusProvider;

	/**
	 * Initializes mocks. Has to run before each test so that mocks are clear.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);

		globalDataAccessService = new GlobalDataAccessService();
		globalDataAccessService.platformIdentDao = platformIdentDao;
		globalDataAccessService.agentStatusProvider = agentStatusProvider;
		globalDataAccessService.defaultDataDao = defaultDataDao;
		globalDataAccessService.log = LoggerFactory.getLogger(GlobalDataAccessService.class);
	}

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

		Map<Long, AgentStatusData> map = new HashMap<Long, AgentStatusData>(1);
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

		Map<Long, AgentStatusData> map = new HashMap<Long, AgentStatusData>(1);
		AgentStatusData agentStatusData = new AgentStatusData(AgentConnection.DISCONNECTED);
		map.put(platformId, agentStatusData);
		when(agentStatusProvider.getAgentStatusDataMap()).thenReturn(map);

		globalDataAccessService.deleteAgent(platformId);

		verify(platformIdentDao, times(1)).delete(platformIdent);
		verify(defaultDataDao, times(1)).deleteAll(platformId);
		verify(agentStatusProvider, times(1)).registerDeleted(platformId);
	}
}
