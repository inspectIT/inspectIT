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
import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class RuntimeInformationTest extends AbstractLogSupport {

	private RuntimeInformation runtimeInfo;

	@Mock
	private RuntimeInfoProvider runtimeBean;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		runtimeInfo = new RuntimeInformation(idManager);
		runtimeInfo.log = LoggerFactory.getLogger(RuntimeInformation.class);

		// we have to replace the real runtimeBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = runtimeInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(runtimeInfo, runtimeBean);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		long uptime = 12345L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(runtimeBean.getUptime()).thenReturn(uptime);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(RuntimeInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		RuntimeInformationData runtimeData = (RuntimeInformationData) sensorData;
		assertThat(runtimeData.getCount(), is(equalTo(1)));

		// as there was only one data object min/max/total the values must be the
		// same
		assertThat(runtimeData.getMinUptime(), is(equalTo(uptime)));
		assertThat(runtimeData.getMaxUptime(), is(equalTo(uptime)));
		assertThat(runtimeData.getTotalUptime(), is(equalTo(uptime)));
	}

	@Test
	public void twoDataSets() throws IdNotAvailableException {
		long uptime = 12345L;
		long uptime2 = 123559L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(runtimeBean.getUptime()).thenReturn(uptime);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		// -> The service must create a new one and add it to the storage
		// We use an argument capturer to further inspect the given argument.
		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		SystemSensorData sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(RuntimeInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		RuntimeInformationData runtimeData = (RuntimeInformationData) sensorData;
		assertThat(runtimeData.getCount(), is(equalTo(1)));

		// as there was only one data object min/max/total the values must be the
		// same
		assertThat(runtimeData.getMinUptime(), is(equalTo(uptime)));
		assertThat(runtimeData.getMaxUptime(), is(equalTo(uptime)));
		assertThat(runtimeData.getTotalUptime(), is(equalTo(uptime)));

		// ------------------------
		// SECOND UPDATE CALL
		// ------------------------
		when(runtimeBean.getUptime()).thenReturn(uptime2);
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(runtimeData);

		runtimeInfo.update(coreService, sensorTypeIdent);
		verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

		sensorData = sensorDataCaptor.getValue();
		assertThat(sensorData, is(instanceOf(RuntimeInformationData.class)));
		assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

		runtimeData = (RuntimeInformationData) sensorData;
		assertThat(runtimeData.getCount(), is(equalTo(2)));

		assertThat(runtimeData.getMinUptime(), is(equalTo(uptime)));
		assertThat(runtimeData.getMaxUptime(), is(equalTo(uptime2)));
		assertThat(runtimeData.getTotalUptime(), is(equalTo(uptime + uptime2)));
	}

	@Test
	public void idNotAvailableTest() throws IdNotAvailableException {
		long uptime = 12345L;
		long sensorTypeIdent = 13L;

		when(runtimeBean.getUptime()).thenReturn(uptime);

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		runtimeInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

	protected Level getLogLevel() {
		return Level.FINEST;
	}

}
