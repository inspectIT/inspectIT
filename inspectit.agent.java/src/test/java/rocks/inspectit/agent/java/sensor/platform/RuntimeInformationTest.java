package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class RuntimeInformationTest extends TestBase {

	@InjectMocks
	RuntimeInformation runtimeInfo;

	@Mock
	RuntimeInfoProvider runtimeBean;

	@Mock
	IPlatformManager platformManager;

	@Mock
	ICoreService coreService;

	@Mock
	PlatformSensorTypeConfig sensorTypeConfig;

	@Mock
	Logger log;

	@BeforeMethod
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		// we have to replace the real runtimeBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = runtimeInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(runtimeInfo, runtimeBean);
	}

	public class Update extends RuntimeInformationTest {

		@Test
		public void oneDataSet() throws IdNotAvailableException {
			long uptime = 12345L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(runtimeBean.getUptime()).thenReturn(uptime);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			runtimeInfo.update(coreService);

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

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// ------------------------
			// FIRST UPDATE CALL
			// ------------------------
			when(runtimeBean.getUptime()).thenReturn(uptime);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			runtimeInfo.update(coreService);

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

			runtimeInfo.update(coreService);
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
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			runtimeInfo.update(coreService);

			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
		}

	}
}
