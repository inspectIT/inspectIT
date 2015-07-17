package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the agent storage service.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class AgentStorageServiceTest extends AbstractTestNGLogSupport {

	/**
	 * Service to be tested.
	 */
	private AgentStorageService agentStorageService;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Mock
	private AgentStatusDataProvider agentStatusDataProvider;

	@Mock
	private ICmrManagementService cmrManagementService;

	/**
	 * Initializes the mocks.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		agentStorageService = new AgentStorageService(new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(1));
		agentStorageService.platformIdentDateSaver = agentStatusDataProvider;
		agentStorageService.cmrManagementService = cmrManagementService;
		agentStorageService.log = LoggerFactory.getLogger(AgentStorageService.class);
	}

	/**
	 * Proves that the data will be dropped after the timeout if there is no place in the queue and
	 * amount of dropped data be remembered.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void dropDataAfterTimeout() throws RemoteException {
		List<DefaultData> dataList = new ArrayList<DefaultData>();
		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(1L);
		dataList.add(timerData);

		agentStorageService.addDataObjects(dataList);
		agentStorageService.addDataObjects(dataList);

		Mockito.verify(agentStatusDataProvider, Mockito.times(2)).registerDataSent(1L);
		Mockito.verify(cmrManagementService, Mockito.times(1)).addDroppedDataCount(dataList.size());
	}

	/**
	 * Provides that data will be processed if there is place in the queue.
	 * 
	 * @throws RemoteException
	 *             If {@link RemoteException} occurs.
	 */
	@Test
	public void acceptData() throws RemoteException {
		List<DefaultData> dataList = new ArrayList<DefaultData>();
		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(1L);
		dataList.add(timerData);

		agentStorageService.addDataObjects(dataList);

		Mockito.verify(agentStatusDataProvider, Mockito.times(1)).registerDataSent(1L);
		Mockito.verifyZeroInteractions(cmrManagementService);
	}
}
