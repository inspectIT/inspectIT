package info.novatec.inspectit.agent.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.instrumentation.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.testbase.TestBase;

import java.lang.reflect.Field;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ClassLoadingInformationTest extends TestBase {

	@InjectMocks
	ClassLoadingInformation classLoadingInfo;

	@Mock
	RuntimeInfoProvider runtimeBean;

	@Mock
	IIdManager idManager;

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
		Field field = classLoadingInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(classLoadingInfo, runtimeBean);
	}

	public class Update extends ClassLoadingInformationTest {

		@Test
		public void oneDataSet() throws IdNotAvailableException {
			int loadedClassCount = 3;
			long totalLoadedClassCount = 10L;
			long unloadedClassCount = 2L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(idManager.getPlatformId()).thenReturn(platformIdent);

			when(runtimeBean.getLoadedClassCount()).thenReturn(loadedClassCount);
			when(runtimeBean.getTotalLoadedClassCount()).thenReturn(totalLoadedClassCount);
			when(runtimeBean.getUnloadedClassCount()).thenReturn(unloadedClassCount);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			classLoadingInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(ClassLoadingInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			ClassLoadingInformationData classLoadingData = (ClassLoadingInformationData) sensorData;
			assertThat(classLoadingData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(classLoadingData.getMinLoadedClassCount(), is(equalTo(loadedClassCount)));
			assertThat(classLoadingData.getMaxLoadedClassCount(), is(equalTo(loadedClassCount)));
			assertThat(classLoadingData.getTotalLoadedClassCount(), is(equalTo(loadedClassCount)));

			assertThat(classLoadingData.getMinTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));
			assertThat(classLoadingData.getMaxTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));
			assertThat(classLoadingData.getTotalTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));

			assertThat(classLoadingData.getMinUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getMaxUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getTotalUnloadedClassCount(), is(equalTo(unloadedClassCount)));
		}

		@Test
		public void twoDataSets() throws IdNotAvailableException {
			int loadedClassCount = 3;
			int loadedClassCount2 = 5;
			long totalLoadedClassCount = 10L;
			long totalLoadedClassCount2 = 12L;
			long unloadedClassCount = 2L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(idManager.getPlatformId()).thenReturn(platformIdent);

			// ------------------------
			// FIRST UPDATE CALL
			// ------------------------
			when(runtimeBean.getLoadedClassCount()).thenReturn(loadedClassCount);
			when(runtimeBean.getTotalLoadedClassCount()).thenReturn(totalLoadedClassCount);
			when(runtimeBean.getUnloadedClassCount()).thenReturn(unloadedClassCount);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			classLoadingInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData parameter = sensorDataCaptor.getValue();
			assertThat(parameter, is(instanceOf(ClassLoadingInformationData.class)));
			assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			ClassLoadingInformationData classLoadingData = (ClassLoadingInformationData) parameter;
			assertThat(classLoadingData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(classLoadingData.getMinLoadedClassCount(), is(equalTo(loadedClassCount)));
			assertThat(classLoadingData.getMaxLoadedClassCount(), is(equalTo(loadedClassCount)));
			assertThat(classLoadingData.getTotalLoadedClassCount(), is(equalTo(loadedClassCount)));

			assertThat(classLoadingData.getMinTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));
			assertThat(classLoadingData.getMaxTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));
			assertThat(classLoadingData.getTotalTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));

			assertThat(classLoadingData.getMinUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getMaxUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getTotalUnloadedClassCount(), is(equalTo(unloadedClassCount)));

			// ------------------------
			// SECOND UPDATE CALL
			// ------------------------
			when(runtimeBean.getLoadedClassCount()).thenReturn(loadedClassCount2);
			when(runtimeBean.getTotalLoadedClassCount()).thenReturn(totalLoadedClassCount2);

			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(classLoadingData);
			classLoadingInfo.update(coreService);

			// -> The service adds the data object only once
			// We use an argument capturer to further inspect the given argument.
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			parameter = sensorDataCaptor.getValue();
			assertThat(parameter, is(instanceOf(ClassLoadingInformationData.class)));
			assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			classLoadingData = (ClassLoadingInformationData) parameter;
			assertThat(classLoadingData.getCount(), is(equalTo(2)));

			assertThat(classLoadingData.getMinLoadedClassCount(), is(equalTo(loadedClassCount)));
			assertThat(classLoadingData.getMaxLoadedClassCount(), is(equalTo(loadedClassCount2)));
			assertThat(classLoadingData.getTotalLoadedClassCount(), is(equalTo(loadedClassCount + loadedClassCount2)));

			assertThat(classLoadingData.getMinTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount)));
			assertThat(classLoadingData.getMaxTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount2)));
			assertThat(classLoadingData.getTotalTotalLoadedClassCount(), is(equalTo(totalLoadedClassCount + totalLoadedClassCount2)));

			assertThat(classLoadingData.getMinUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getMaxUnloadedClassCount(), is(equalTo(unloadedClassCount)));
			assertThat(classLoadingData.getTotalUnloadedClassCount(), is(equalTo(unloadedClassCount + unloadedClassCount)));
		}

		@Test
		public void idNotAvailableTest() throws IdNotAvailableException {
			int loadedClassCount = 3;
			long totalLoadedClassCount = 10L;
			long unloadedClassCount = 2L;
			long sensorTypeIdent = 13L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(runtimeBean.getLoadedClassCount()).thenReturn(loadedClassCount);
			when(runtimeBean.getTotalLoadedClassCount()).thenReturn(totalLoadedClassCount);
			when(runtimeBean.getUnloadedClassCount()).thenReturn(unloadedClassCount);

			when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			classLoadingInfo.update(coreService);

			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
		}

	}
}
