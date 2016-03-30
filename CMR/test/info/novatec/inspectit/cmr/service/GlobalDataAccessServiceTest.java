package info.novatec.inspectit.cmr.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.security.CmrSecurityManager;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.exception.BusinessException;

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
	
	@Mock
	private CmrSecurityManager securityManager;

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
		globalDataAccessService.securityManager = securityManager;
		globalDataAccessService.log = LoggerFactory.getLogger(GlobalDataAccessService.class);
				
	}

	/**
	 * No delete enabled when platform ident can not be found.
	 */
	@Test(expectedExceptions = { BusinessException.class })
	public void testNonExistingAgentDelete() throws BusinessException {
		long platformId = 10L;
		when(platformIdentDao.load(Long.valueOf(platformId))).thenReturn(null);
		when(securityManager.isPermitted(anyString())).thenReturn(true);

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
		when(securityManager.isPermitted(anyString())).thenReturn(true);

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
		when(securityManager.isPermitted(anyString())).thenReturn(true);

		Map<Long, AgentStatusData> map = new HashMap<Long, AgentStatusData>(1);
		AgentStatusData agentStatusData = new AgentStatusData(AgentConnection.DISCONNECTED);
		map.put(platformId, agentStatusData);
		when(agentStatusProvider.getAgentStatusDataMap()).thenReturn(map);

		globalDataAccessService.deleteAgent(platformId);

		verify(platformIdentDao, times(1)).delete(platformIdent);
		verify(defaultDataDao, times(1)).deleteAll(platformId);
		verify(agentStatusProvider, times(1)).registerDeleted(platformId);
	}
	/**
	 * Check that if there is no permission to delete the agent nothing is done.
	 * 
	 */
	@Test
	public void noAgentDeletePermission() throws BusinessException {
		long platformId = 10L;
		when(securityManager.isPermitted(anyString())).thenReturn(false);
		
		globalDataAccessService.deleteAgent(platformId);
		
		verifyZeroInteractions(platformIdentDao, defaultDataDao, agentStatusProvider);
	}
}
