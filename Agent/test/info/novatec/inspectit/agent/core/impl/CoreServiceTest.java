package info.novatec.inspectit.agent.core.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IObjectStorage;
import info.novatec.inspectit.agent.core.ListListener;
import info.novatec.inspectit.agent.sending.ISendingStrategy;
import info.novatec.inspectit.agent.sensor.method.timer.PlainTimerStorage;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class CoreServiceTest extends AbstractLogSupport {

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IConnection connection;

	@SuppressWarnings("rawtypes")
	@Mock
	private IBufferStrategy bufferStrategy;

	@Mock
	private ISendingStrategy sendingStrategy;

	@Mock
	private IIdManager idManager;

	private CoreService coreService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void initTestClass() {
		List<ISendingStrategy> sendingStrategies = new ArrayList<ISendingStrategy>();
		sendingStrategies.add(sendingStrategy);

		coreService = new CoreService(configurationStorage, connection, bufferStrategy, sendingStrategies, idManager);
		coreService.log = LoggerFactory.getLogger(CoreService.class);
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under heavy load. There is
	 * no reliable way to make this test always successful.
	 */
	@Test(enabled = false)
	public void startStop() throws InterruptedException {
		coreService.start();
		verify(sendingStrategy, times(1)).start(coreService);

		// have to wait one second to be sure that the getPlatformSensorTypes
		// method should be called at least once
		synchronized (this) {
			wait(3000);
		}

		coreService.stop();
		verify(sendingStrategy, times(1)).stop();
		verify(configurationStorage, atLeastOnce()).getPlatformSensorTypes();

		verifyNoMoreInteractions(sendingStrategy, configurationStorage);
		verifyZeroInteractions(connection, bufferStrategy, idManager);
	}

	/**
	 * This method could also <b>fail</b> due to race conditions.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = { "startStop" }, enabled = false)
	public void sendOneMethodSensorData() throws InterruptedException, ServerUnavailableException {
		coreService.start();

		long sensorTypeId = 1;
		long methodId = 5;
		TimerData timerData = new TimerData();
		when(bufferStrategy.hasNext()).thenReturn(true).thenReturn(false);
		List<TimerData> dataList = new ArrayList<TimerData>();
		dataList.add(timerData);
		when(bufferStrategy.next()).thenReturn(dataList);

		coreService.addMethodSensorData(sensorTypeId, methodId, null, timerData);
		coreService.sendData();

		synchronized (this) {
			wait(3000);
		}

		verify(bufferStrategy, times(1)).addMeasurements(dataList);
		verify(bufferStrategy, times(2)).hasNext();
		verify(bufferStrategy, times(1)).next();
		verify(bufferStrategy, times(1)).remove();

		verify(connection, times(1)).sendDataObjects(dataList);

		verifyNoMoreInteractions(bufferStrategy, connection);
		verifyZeroInteractions(idManager);
	}

	/**
	 * This method could also <b>fail</b> due to race conditions.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = { "startStop" }, enabled = false)
	public void sendOnePlatformSensorData() throws InterruptedException, ServerUnavailableException {
		coreService.start();

		long sensorTypeId = 1;
		CpuInformationData cpuInformationData = new CpuInformationData();
		when(bufferStrategy.hasNext()).thenReturn(true).thenReturn(false);
		List<SystemSensorData> dataList = new ArrayList<SystemSensorData>();
		dataList.add(cpuInformationData);
		when(bufferStrategy.next()).thenReturn(dataList);

		coreService.addPlatformSensorData(sensorTypeId, cpuInformationData);
		coreService.sendData();

		synchronized (this) {
			wait(3000);
		}

		verify(bufferStrategy, times(1)).addMeasurements(dataList);
		verify(bufferStrategy, times(2)).hasNext();
		verify(bufferStrategy, times(1)).next();
		verify(bufferStrategy, times(1)).remove();

		verify(connection, times(1)).sendDataObjects(dataList);

		verifyNoMoreInteractions(bufferStrategy, connection);
		verifyZeroInteractions(idManager);
	}

	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = { "startStop" }, enabled = false)
	public void sendOneExceptionSensorData() throws InterruptedException, ServerUnavailableException {
		coreService.start();

		long sensorTypeId = 1;
		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setThrowableIdentityHashCode(123456);
		when(bufferStrategy.hasNext()).thenReturn(true).thenReturn(false);
		List<MethodSensorData> dataList = new ArrayList<MethodSensorData>();
		dataList.add(exceptionSensorData);
		when(bufferStrategy.next()).thenReturn(dataList);

		coreService.addExceptionSensorData(sensorTypeId, exceptionSensorData.getThrowableIdentityHashCode(), exceptionSensorData);
		coreService.sendData();

		synchronized (this) {
			wait(3000);
		}

		verify(bufferStrategy, times(1)).addMeasurements(dataList);
		verify(bufferStrategy, times(2)).hasNext();
		verify(bufferStrategy, times(1)).next();
		verify(bufferStrategy, times(1)).remove();

		verify(connection, times(1)).sendDataObjects(dataList);

		verifyNoMoreInteractions(bufferStrategy, connection);
		verifyZeroInteractions(idManager);
	}

	/**
	 * This method could also <b>fail</b> due to race conditions.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = { "startStop" }, enabled = false)
	public void sendOneObjectStorageData() throws InterruptedException, ServerUnavailableException {
		coreService.start();

		long sensorTypeId = 1;
		long methodId = 5;
		PlainTimerStorage timerStorage = new PlainTimerStorage(null, 0, sensorTypeId, methodId, Collections.<ParameterContentData> emptyList(), false);
		when(bufferStrategy.hasNext()).thenReturn(true).thenReturn(false);
		List<DefaultData> storageList = new ArrayList<DefaultData>();
		storageList.add(timerStorage.finalizeDataObject());
		when(bufferStrategy.next()).thenReturn(storageList);

		coreService.addObjectStorage(sensorTypeId, methodId, null, timerStorage);
		coreService.sendData();

		synchronized (this) {
			wait(3000);
		}

		verify(bufferStrategy, times(1)).addMeasurements(storageList);
		verify(bufferStrategy, times(2)).hasNext();
		verify(bufferStrategy, times(1)).next();
		verify(bufferStrategy, times(1)).remove();

		verify(connection, times(1)).sendDataObjects(storageList);

		verifyNoMoreInteractions(bufferStrategy, connection);
		verifyZeroInteractions(idManager);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyListListenerMethodData() {
		ListListener<TimerData> listener = mock(ListListener.class);
		TimerData timerData = new TimerData();
		List<TimerData> dataList = new ArrayList<TimerData>();
		dataList.add(timerData);

		coreService.addListListener(listener);
		coreService.addMethodSensorData(0, 0, null, timerData);

		verify(listener, times(1)).contentChanged(dataList);

		coreService.removeListListener(listener);

		verifyNoMoreInteractions(listener, bufferStrategy, connection, sendingStrategy);
		verifyZeroInteractions(idManager);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyListListenerPlatformData() {
		ListListener<SystemSensorData> listener = mock(ListListener.class);
		CpuInformationData cpuInformationData = new CpuInformationData();
		List<SystemSensorData> dataList = new ArrayList<SystemSensorData>();
		dataList.add(cpuInformationData);

		coreService.addListListener(listener);
		coreService.addPlatformSensorData(0, cpuInformationData);

		verify(listener, times(1)).contentChanged(dataList);

		coreService.removeListListener(listener);

		verifyNoMoreInteractions(listener, bufferStrategy, connection, sendingStrategy);
		verifyZeroInteractions(idManager);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyListListenerExceptionData() {
		ListListener<ExceptionSensorData> listener = mock(ListListener.class);
		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setThrowableType("MyException");
		exceptionSensorData.setThrowableIdentityHashCode(1234);
		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);
		List<ExceptionSensorData> dataList = new ArrayList<ExceptionSensorData>();
		dataList.add(exceptionSensorData);

		coreService.addListListener(listener);
		coreService.addExceptionSensorData(0, exceptionSensorData.getThrowableIdentityHashCode(), exceptionSensorData);

		verify(listener, times(1)).contentChanged(dataList);

		coreService.removeListListener(listener);

		verifyNoMoreInteractions(listener, bufferStrategy, connection, sendingStrategy);
		verifyZeroInteractions(idManager);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyListListenerObjectStorageData() {
		ListListener<IObjectStorage> listener = mock(ListListener.class);
		PlainTimerStorage timerStorage = new PlainTimerStorage(null, 0, 0, 0, Collections.<ParameterContentData> emptyList(), false);
		List<IObjectStorage> storageList = new ArrayList<IObjectStorage>();
		storageList.add(timerStorage);

		coreService.addListListener(listener);
		coreService.addObjectStorage(0, 0, null, timerStorage);

		verify(listener, times(1)).contentChanged(storageList);

		coreService.removeListListener(listener);

		verifyNoMoreInteractions(listener, bufferStrategy, connection, sendingStrategy);
		verifyZeroInteractions(idManager);
	}

	@Test
	public void addAndRetrieveMethodSensorDataNoPrefix() {
		long sensorTypeId = 2;
		long methodId = 5;
		String prefix = null;
		TimerData timerData = new TimerData();

		coreService.addMethodSensorData(sensorTypeId, methodId, prefix, timerData);

		MethodSensorData methodSensorData = coreService.getMethodSensorData(sensorTypeId, methodId, prefix);
		assertThat(methodSensorData, is(equalTo(((MethodSensorData) timerData))));
	}

	@Test
	public void addAndRetrieveMethodSensorDataWithPrefix() {
		long sensorTypeId = 2;
		long methodId = 5;
		String prefix = "prefix";
		TimerData timerData = new TimerData();

		coreService.addMethodSensorData(sensorTypeId, methodId, prefix, timerData);

		MethodSensorData methodSensorData = coreService.getMethodSensorData(sensorTypeId, methodId, prefix);
		assertThat(methodSensorData, is(equalTo(((MethodSensorData) timerData))));
	}

	@Test
	public void addAndRetrievePlatformSensorData() {
		long sensorTypeId = 4;
		CpuInformationData cpuInformationData = new CpuInformationData();

		coreService.addPlatformSensorData(sensorTypeId, cpuInformationData);

		SystemSensorData systemSensorData = coreService.getPlatformSensorData(sensorTypeId);
		assertThat(systemSensorData, is(equalTo(((SystemSensorData) cpuInformationData))));
	}

	@Test
	public void addAndRetrieveExceptionSensorData() {
		long sensorTypeId = 10;
		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setThrowableType("MyException");
		exceptionSensorData.setThrowableIdentityHashCode(1234);
		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);

		coreService.addExceptionSensorData(sensorTypeId, exceptionSensorData.getThrowableIdentityHashCode(), exceptionSensorData);

		MethodSensorData methodSensorData = coreService.getExceptionSensorData(sensorTypeId, exceptionSensorData.getThrowableIdentityHashCode());
		assertThat(methodSensorData, is(equalTo(((MethodSensorData) exceptionSensorData))));
	}

	@Test
	public void addAndRetrieveObjectStorageDataNoPrefix() {
		long sensorTypeId = 7;
		long methodId = 10;
		String prefix = null;
		PlainTimerStorage timerStorage = new PlainTimerStorage(null, 0, 0, 0, Collections.<ParameterContentData> emptyList(), false);

		coreService.addObjectStorage(sensorTypeId, methodId, prefix, timerStorage);

		IObjectStorage objectStorage = coreService.getObjectStorage(sensorTypeId, methodId, prefix);
		assertThat(objectStorage, is(equalTo(((IObjectStorage) timerStorage))));
	}

	@Test
	public void addAndRetrieveObjectStorageDataWithPrefix() {
		long sensorTypeId = 7;
		long methodId = 10;
		String prefix = "prefiXX";
		PlainTimerStorage timerStorage = new PlainTimerStorage(null, 0, 0, 0, Collections.<ParameterContentData> emptyList(), false);

		coreService.addObjectStorage(sensorTypeId, methodId, prefix, timerStorage);

		IObjectStorage objectStorage = coreService.getObjectStorage(sensorTypeId, methodId, prefix);
		assertThat(objectStorage, is(equalTo(((IObjectStorage) timerStorage))));
	}

}
