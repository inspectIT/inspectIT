package info.novatec.inspectit.agent.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
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
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.CpuInformationData;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class CpuInformationTest extends AbstractLogSupport {

	private CpuInformation cpuInfo;

	@Mock
	private OperatingSystemInfoProvider osBean;

	@Mock
	private RuntimeInfoProvider runtimeBean;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		cpuInfo = new CpuInformation(idManager);
		cpuInfo.log = LoggerFactory.getLogger(CpuInformation.class);

		// we have to replace the real osBean by the mocked one, so that we
		// don't retrieve the info from the underlying JVM
		Field field = cpuInfo.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(cpuInfo, osBean);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		int availableProc = 1;
		long processCpuTime = 2L;
		long sensorType = 13L;
		long platformIdent = 11L;
		float cpuUsage = 0.0f;

		when(osBean.getAvailableProcessors()).thenReturn(availableProc);
		when(osBean.getProcessCpuTime()).thenReturn(processCpuTime);
		when(osBean.retrieveCpuUsage()).thenReturn(cpuUsage);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenReturn(sensorType);

		// no current data object is available
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);

		cpuInfo.update(coreService, sensorType);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());

		// Cast the parameter to the expected concrete class:
		SystemSensorData parameter = sensorDataCaptor.getValue();
		assertThat(parameter, is(instanceOf(CpuInformationData.class)));
		assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorType)));

		CpuInformationData data = (CpuInformationData) parameter;
		assertThat(data.getCount(), is(1));

		// CPU usage can only be deduced after two sets of data are captured
		assertThat((double) data.getMaxCpuUsage(), is(closeTo(0d, 0.01d)));
		assertThat((double) data.getMinCpuUsage(), is(closeTo(0d, 0.01d)));
		assertThat((double) data.getTotalCpuUsage(), is(closeTo(0d, 0.01d)));

		assertThat(data.getProcessCpuTime(), is(equalTo(processCpuTime)));
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		int availableProc = 1;

		// process cpu time is provided as nanoseconds
		long processCpuTime1 = 200L * 1000 * 1000; // ns representation of 200ms
		long processCpuTime2 = 500L * 1000 * 1000; // ns representation of 500ms

		// uptime is provided in milliseconds
		long uptime1 = 500L; // 500ms
		long uptime2 = 1100L; // 1100ms
		long sensorType = 13L;
		long platformIdent = 11L;
		float cpuUsage1 = 0.0f;
		float cpuUsage2 = 50.0f;

		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		SystemSensorData parameter = null;

		when(runtimeBean.getUptime()).thenReturn(uptime1).thenReturn(uptime2);
		when(osBean.getAvailableProcessors()).thenReturn(availableProc);
		when(osBean.getProcessCpuTime()).thenReturn(processCpuTime1).thenReturn(processCpuTime2);
		when(osBean.retrieveCpuUsage()).thenReturn(cpuUsage1).thenReturn(cpuUsage2);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenReturn(sensorType);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		// no current data object is available, second call provides an
		// initialized version. The second call provides the parameter that was
		// internally registered.
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);
		cpuInfo.update(coreService, sensorType);

		// -> The service must create a new one and add it to the storage
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());

		// Cast the parameter to the expected concrete class:
		parameter = sensorDataCaptor.getValue();
		assertThat(parameter, is(instanceOf(CpuInformationData.class)));
		assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorType)));

		CpuInformationData data = (CpuInformationData) parameter;
		assertThat(data.getCount(), is(1));

		// CPU usage can only be deduced after two sets of data are captured
		assertThat((double) data.getMaxCpuUsage(), is(closeTo(0d, 0.01d)));
		assertThat((double) data.getMinCpuUsage(), is(closeTo(0d, 0.01d)));
		assertThat((double) data.getTotalCpuUsage(), is(closeTo(0d, 0.01d)));

		assertThat(data.getProcessCpuTime(), is(equalTo(processCpuTime1)));

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(parameter);
		cpuInfo.update(coreService, sensorType);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());

		// Cast the parameter to the expected concrete class:
		parameter = sensorDataCaptor.getValue();
		assertThat(parameter, is(instanceOf(CpuInformationData.class)));
		assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorType)));

		data = (CpuInformationData) parameter;
		assertThat(data.getCount(), is(2));

		// CPU usage can only be deduced after two sets of data are captured
		assertThat((double) data.getMaxCpuUsage(), is(closeTo(cpuUsage2, 0.01d)));

		// the first data set was 0
		assertThat((double) data.getMinCpuUsage(), is(closeTo(0d, 0.01d)));
		assertThat((double) data.getTotalCpuUsage(), is(closeTo(cpuUsage2, 0.01d)));

		assertThat(data.getProcessCpuTime(), is(equalTo(processCpuTime2)));
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		int availableProc = 1;
		long processCpuTime = 2L;
		long uptime = 5L;
		long sensorType = 13L;

		when(runtimeBean.getUptime()).thenReturn(uptime);
		when(osBean.getAvailableProcessors()).thenReturn(availableProc);
		when(osBean.getProcessCpuTime()).thenReturn(processCpuTime);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorType)).thenThrow(new IdNotAvailableException("expected"));

		// no current data object is available
		when(coreService.getPlatformSensorData(sensorType)).thenReturn(null);
		cpuInfo.update(coreService, sensorType);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorType), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}
}
