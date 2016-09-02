package rocks.inspectit.server.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the agent storage service.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class AgentStorageServiceTest extends TestBase {

	/**
	 * Service to be tested.
	 */
	@InjectMocks
	AgentStorageService agentStorageService;

	@Mock
	AgentStatusDataProvider agentStatusDataProvider;

	@Mock
	Logger log;

	@Mock
	DefaultDataDao defaultDataDao;

	public class AddDataObjects extends AgentStorageServiceTest {

		/**
		 * Provides that data will be processed always.
		 */
		@Test
		public void acceptData() {
			List<DefaultData> dataList = new ArrayList<>();
			TimerData timerData = new TimerData();
			timerData.setPlatformIdent(1L);
			dataList.add(timerData);

			agentStorageService.addDataObjects(dataList);

			verify(agentStatusDataProvider).registerDataSent(1L);
			verify(defaultDataDao).saveAll(dataList);
		}

		/**
		 * Provides that no exception occurs when data is null.
		 */
		@Test
		public void nullData() {
			agentStorageService.addDataObjects(null);

			verifyZeroInteractions(agentStatusDataProvider, defaultDataDao);
		}

		@Test
		public void debugLog() {
			List<DefaultData> dataList = new ArrayList<>();
			TimerData timerData = new TimerData();
			timerData.setPlatformIdent(1L);
			dataList.add(timerData);
			when(log.isDebugEnabled()).thenReturn(true);

			agentStorageService.addDataObjects(dataList);

			verify(log, times(2)).isDebugEnabled();
			verify(log).debug(anyString());
			verifyNoMoreInteractions(log);
		}

	}

}
