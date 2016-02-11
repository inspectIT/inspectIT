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
import info.novatec.inspectit.communication.data.CompilationInformationData;

import java.lang.reflect.Field;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class CompilationInformationTest extends AbstractLogSupport {

	private CompilationInformation compilationInfo;

	@Mock
	private RuntimeInfoProvider runtimeBean;

	@Mock
	private IIdManager idManager;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		compilationInfo = new CompilationInformation(idManager);
		compilationInfo.log = LoggerFactory.getLogger(CompilationInformation.class);

		// we have to replace the real runtimeBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = compilationInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(compilationInfo, runtimeBean);
	}

	@Test
	public void oneDataSet() throws IdNotAvailableException {
		long totalCompilationTime = 12345L;
		long sensorTypeIdent = 13L;
		long platformIdent = 11L;

		when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
		compilationInfo.update(coreService, sensorTypeIdent);

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

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		// ------------------------
		// FIRST UPDATE CALL
		// ------------------------
		when(runtimeBean.getTotalCompilationTime()).thenReturn(totalCompilationTime);
		compilationInfo.update(coreService, sensorTypeIdent);

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
		compilationInfo.update(coreService, sensorTypeIdent);

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

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenReturn(sensorTypeIdent);

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		compilationInfo.update(coreService, sensorTypeIdent);

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

		when(idManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));
		when(idManager.getRegisteredSensorTypeId(sensorTypeIdent)).thenThrow(new IdNotAvailableException("expected"));

		// there is no current data object available
		when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

		compilationInfo.update(coreService, sensorTypeIdent);

		ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
		verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
	}

}
