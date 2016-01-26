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
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class CompilationInformationTest extends TestBase {

	@InjectMocks
	CompilationInformation compilationInfo;

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
		Field field = compilationInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(compilationInfo, runtimeBean);
	}

	public class Update extends CompilationInformationTest {

		@Test
		public void oneDataSet() throws IdNotAvailableException {
			long totalCompilationTime = 12345L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			compilationInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(CompilationInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			CompilationInformationData compilationData = (CompilationInformationData) sensorData;
			assertThat(compilationData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(compilationData.getMinTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getMaxTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getTotalTotalCompilationTime(), is(equalTo(totalCompilationTime)));
		}

		@Test
		public void twoDataSets() throws IdNotAvailableException {
			long totalCompilationTime = 12345L;
			long totalCompilationTime2 = 12359L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			// ------------------------
			// FIRST UPDATE CALL
			// ------------------------
			when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime);
			compilationInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(CompilationInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			CompilationInformationData compilationData = (CompilationInformationData) sensorData;
			assertThat(compilationData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(compilationData.getMinTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getMaxTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getTotalTotalCompilationTime(), is(equalTo(totalCompilationTime)));

			// ------------------------
			// SECOND UPDATE CALL
			// ------------------------
			when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime2);
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(compilationData);
			compilationInfo.update(coreService);

			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(CompilationInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			compilationData = (CompilationInformationData) sensorData;
			assertThat(compilationData.getCount(), is(equalTo(2)));

			assertThat(compilationData.getMinTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getMaxTotalCompilationTime(), is(equalTo(totalCompilationTime2)));
			assertThat(compilationData.getTotalTotalCompilationTime(), is(equalTo(totalCompilationTime + totalCompilationTime2)));
		}

		/**
		 * Maybe this test is obsolete because we don't expect an exception to be thrown directly in
		 * {@link CompilationInformation#getTotalCompilationTime()} but only in
		 * {@link DefaultRuntimeMXBean#getTotalCompilationTime()}
		 *
		 * @throws IdNotAvailableException
		 */
		@Test
		public void compilationTimeNotAvailable() throws IdNotAvailableException {
			long totalCompilationTime = -1L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(runtimeBean.getTotalCompilationTime()).thenReturn(-1L);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			compilationInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(CompilationInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			CompilationInformationData compilationData = (CompilationInformationData) sensorData;
			assertThat(compilationData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(compilationData.getMinTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getMaxTotalCompilationTime(), is(equalTo(totalCompilationTime)));
			assertThat(compilationData.getTotalTotalCompilationTime(), is(equalTo(totalCompilationTime)));
		}

		@Test
		public void idNotAvailableTest() throws IdNotAvailableException {
			long totalCompilationTime = 12345L;
			long sensorTypeIdent = 13L;

			when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			compilationInfo.update(coreService);

			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
		}
	}

}
