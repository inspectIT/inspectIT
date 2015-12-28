package info.novatec.inspectit.agent.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.provider.ThreadInfoProvider;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ThreadInformationData;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ThreadInformationTest extends AbstractLogSupport {

	private ThreadInformation threadInfo;

	@Mock
	private ThreadInfoProvider threadBean;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		threadInfo = new ThreadInformation(idManager);
		threadInfo.log = LoggerFactory.getLogger(ThreadInformation.class);

		// we have to replace the real threadBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = threadInfo.getClass().getDeclaredField("threadBean");
		field.setAccessible(true);
		field.set(threadInfo, threadBean);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int threadCount = 13;
		int peakThreadCount = 25;
		long totalStartedThreadCount = 55L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(threadBean.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadBean.getThreadCount()).thenReturn(threadCount);
		when(threadBean.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadBean.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(ThreadInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		ThreadInformationData threadData = (ThreadInformationData) sensorData;
		assertThat(threadData.getCount(), is(equalTo(1)));

		// as there was only one data object min/max/total the values must be the
		// same
		assertThat(threadData.getMinDaemonThreadCount(), is(equalTo(daemonThreadCount)));
		assertThat(threadData.getMaxDaemonThreadCount(), is(equalTo(daemonThreadCount)));
		assertThat(threadData.getTotalDaemonThreadCount(), is(equalTo(daemonThreadCount)));

		assertThat(threadData.getMinPeakThreadCount(), is(equalTo(peakThreadCount)));
		assertThat(threadData.getMaxPeakThreadCount(), is(equalTo(peakThreadCount)));
		assertThat(threadData.getTotalPeakThreadCount(), is(equalTo(peakThreadCount)));

		assertThat(threadData.getMinThreadCount(), is(equalTo(threadCount)));
		assertThat(threadData.getMaxThreadCount(), is(equalTo(threadCount)));
		assertThat(threadData.getTotalThreadCount(), is(equalTo(threadCount)));

		assertThat(threadData.getMinTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
		assertThat(threadData.getMaxTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
		assertThat(threadData.getTotalTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int daemonThreadCount2 = 6;
		int threadCount = 13;
		int threadCount2 = 15;
		int peakThreadCount = 25;
		int peakThreadCount2 = 25;
		long totalStartedThreadCount = 55L;
		long totalStartedThreadCount2 = 60L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(threadBean.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadBean.getThreadCount()).thenReturn(threadCount);
		when(threadBean.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadBean.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(ThreadInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		ThreadInformationData threadData = (ThreadInformationData) sensorData;
		assertThat(threadData.getCount(), is(equalTo(1)));

		// as there was only one data object min/max/total values must be the
		// same
		assertThat(threadData.getMinDaemonThreadCount(), is(equalTo(daemonThreadCount)));
		assertThat(threadData.getMaxDaemonThreadCount(), is(equalTo(daemonThreadCount)));
		assertThat(threadData.getTotalDaemonThreadCount(), is(equalTo(daemonThreadCount)));

		assertThat(threadData.getMinPeakThreadCount(), is(equalTo(peakThreadCount)));
		assertThat(threadData.getMaxPeakThreadCount(), is(equalTo(peakThreadCount)));
		assertThat(threadData.getTotalPeakThreadCount(), is(equalTo(peakThreadCount)));

		assertThat(threadData.getMinThreadCount(), is(equalTo(threadCount)));
		assertThat(threadData.getMaxThreadCount(), is(equalTo(threadCount)));
		assertThat(threadData.getTotalThreadCount(), is(equalTo(threadCount)));

		assertThat(threadData.getMinTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
		assertThat(threadData.getMaxTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
		assertThat(threadData.getTotalTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(threadBean.getDaemonThreadCount()).thenReturn(daemonThreadCount2);
		when(threadBean.getThreadCount()).thenReturn(threadCount2);
		when(threadBean.getPeakThreadCount()).thenReturn(peakThreadCount2);
		when(threadBean.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount2);

		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(threadData);

		threadInfo.update(coreService, sensorTypeIdent);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(ThreadInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		threadData = (ThreadInformationData) sensorData;
		assertThat(threadData.getCount(), is(equalTo(2)));

		assertThat(threadData.getMinDaemonThreadCount(), is(equalTo(daemonThreadCount)));
		assertThat(threadData.getMaxDaemonThreadCount(), is(equalTo(daemonThreadCount2)));
		assertThat(threadData.getTotalDaemonThreadCount(), is(equalTo(daemonThreadCount + daemonThreadCount2)));

		assertThat(threadData.getMinPeakThreadCount(), is(equalTo(peakThreadCount)));
		assertThat(threadData.getMaxPeakThreadCount(), is(equalTo(peakThreadCount2)));
		assertThat(threadData.getTotalPeakThreadCount(), is(equalTo(peakThreadCount + peakThreadCount2)));

		assertThat(threadData.getMinThreadCount(), is(equalTo(threadCount)));
		assertThat(threadData.getMaxThreadCount(), is(equalTo(threadCount2)));
		assertThat(threadData.getTotalThreadCount(), is(equalTo(threadCount + threadCount2)));

		assertThat(threadData.getMinTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount)));
		assertThat(threadData.getMaxTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount2)));
		assertThat(threadData.getTotalTotalStartedThreadCount(), is(equalTo(totalStartedThreadCount + totalStartedThreadCount2)));
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		int daemonThreadCount = 5;
		int threadCount = 13;
		int peakThreadCount = 25;
		long totalStartedThreadCount = 55L;
		long sensorTypeIdent = 13L;

		when(threadBean.getDaemonThreadCount()).thenReturn(daemonThreadCount);
		when(threadBean.getThreadCount()).thenReturn(threadCount);
		when(threadBean.getPeakThreadCount()).thenReturn(peakThreadCount);
		when(threadBean.getTotalStartedThreadCount()).thenReturn(totalStartedThreadCount);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		threadInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
