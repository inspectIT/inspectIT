package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryUsage;
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
import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class MemoryInformationTest extends TestBase {

	@InjectMocks
	MemoryInformation memoryInfo;

	@Mock
	MemoryInfoProvider memoryBean;

	@Mock
	OperatingSystemInfoProvider osBean;

	@Mock
	MemoryUsage heapMemoryUsage;

	@Mock
	MemoryUsage nonHeapMemoryUsage;

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
		// we have to replace the real osBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		Field field = memoryInfo.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(memoryInfo, osBean);

		// we have to replace the real memoryBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		field = memoryInfo.getClass().getDeclaredField("memoryBean");
		field.setAccessible(true);
		field.set(memoryInfo, memoryBean);
	}

	public class Update extends MemoryInformationTest {

		@Test
		public void oneDataSet() throws IdNotAvailableException {
			long freePhysicalMemory = 37566L;
			long freeSwapSpace = 578300L;
			long committedVirtualMemorySize = 12345L;
			long usedHeapMemorySize = 3827L;
			long usedNonHeapMemorySize = 12200L;
			long committedHeapMemorySize = 5056L;
			long committedNonHeapMemorySize = 14016L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
			when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
			when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
			when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			memoryInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(MemoryInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			MemoryInformationData memoryData = (MemoryInformationData) sensorData;
			assertThat(memoryData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(memoryData.getMinComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));
			assertThat(memoryData.getMaxComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));
			assertThat(memoryData.getTotalComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));

			assertThat(memoryData.getMinComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));
			assertThat(memoryData.getMaxComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));
			assertThat(memoryData.getTotalComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));

			assertThat(memoryData.getMinComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));
			assertThat(memoryData.getMaxComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));
			assertThat(memoryData.getTotalComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));

			assertThat(memoryData.getMinFreePhysMemory(), is(equalTo(freePhysicalMemory)));
			assertThat(memoryData.getMaxFreePhysMemory(), is(equalTo(freePhysicalMemory)));
			assertThat(memoryData.getTotalFreePhysMemory(), is(equalTo(freePhysicalMemory)));

			assertThat(memoryData.getMinFreeSwapSpace(), is(equalTo(freeSwapSpace)));
			assertThat(memoryData.getMaxFreeSwapSpace(), is(equalTo(freeSwapSpace)));
			assertThat(memoryData.getTotalFreeSwapSpace(), is(equalTo(freeSwapSpace)));

			assertThat(memoryData.getMinUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));
			assertThat(memoryData.getMaxUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));
			assertThat(memoryData.getTotalUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));

			assertThat(memoryData.getMinUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
			assertThat(memoryData.getMaxUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
			assertThat(memoryData.getTotalUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
		}

		@Test
		public void twoDataSets() throws IdNotAvailableException {
			long freePhysicalMemory = 37566L;
			long freePhysicalMemory2 = 37000L;
			long freeSwapSpace = 578300L;
			long freeSwapSpace2 = 578000L;
			long committedVirtualMemorySize = 12345L;
			long committedVirtualMemorySize2 = 12300L;
			long usedHeapMemorySize = 3827L;
			long usedHeapMemorySize2 = 4000L;
			long usedNonHeapMemorySize = 12200L;
			long usedNonHeapMemorySize2 = 13000L;
			long committedHeapMemorySize = 5056L;
			long committedHeapMemorySize2 = 4000L;
			long committedNonHeapMemorySize = 14016L;
			long committedNonHeapMemorySize2 = 13000L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);
			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);

			// ------------------------
			// FIRST UPDATE CALL
			// ------------------------
			when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
			when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
			when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
			when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			memoryInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(MemoryInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			MemoryInformationData memoryData = (MemoryInformationData) sensorData;
			assertThat(memoryData.getCount(), is(equalTo(1)));

			// as there was only one data object min/max/total the values must be the
			// same
			assertThat(memoryData.getMinComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));
			assertThat(memoryData.getMaxComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));
			assertThat(memoryData.getTotalComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));

			assertThat(memoryData.getMinComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));
			assertThat(memoryData.getMaxComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));
			assertThat(memoryData.getTotalComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));

			assertThat(memoryData.getMinComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));
			assertThat(memoryData.getMaxComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));
			assertThat(memoryData.getTotalComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));

			assertThat(memoryData.getMinFreePhysMemory(), is(equalTo(freePhysicalMemory)));
			assertThat(memoryData.getMaxFreePhysMemory(), is(equalTo(freePhysicalMemory)));
			assertThat(memoryData.getTotalFreePhysMemory(), is(equalTo(freePhysicalMemory)));

			assertThat(memoryData.getMinFreeSwapSpace(), is(equalTo(freeSwapSpace)));
			assertThat(memoryData.getMaxFreeSwapSpace(), is(equalTo(freeSwapSpace)));
			assertThat(memoryData.getTotalFreeSwapSpace(), is(equalTo(freeSwapSpace)));

			assertThat(memoryData.getMinUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));
			assertThat(memoryData.getMaxUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));
			assertThat(memoryData.getTotalUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));

			assertThat(memoryData.getMinUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
			assertThat(memoryData.getMaxUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
			assertThat(memoryData.getTotalUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));

			// ------------------------
			// SECOND UPDATE CALL
			// ------------------------
			when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory2);
			when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace2);
			when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize2);
			when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize2);
			when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize2);
			when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize2);
			when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize2);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(memoryData);
			memoryInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(MemoryInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			memoryData = (MemoryInformationData) sensorData;
			assertThat(memoryData.getCount(), is(equalTo(2)));

			// as there was only one data object min/max/total values must be the
			// same
			assertThat(memoryData.getMinComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize2)));
			assertThat(memoryData.getMaxComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize)));
			assertThat(memoryData.getTotalComittedHeapMemorySize(), is(equalTo(committedHeapMemorySize + committedHeapMemorySize2)));

			assertThat(memoryData.getMinComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize2)));
			assertThat(memoryData.getMaxComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize)));
			assertThat(memoryData.getTotalComittedNonHeapMemorySize(), is(equalTo(committedNonHeapMemorySize + committedNonHeapMemorySize2)));

			assertThat(memoryData.getMinComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize2)));
			assertThat(memoryData.getMaxComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize)));
			assertThat(memoryData.getTotalComittedVirtualMemSize(), is(equalTo(committedVirtualMemorySize + committedVirtualMemorySize2)));

			assertThat(memoryData.getMinFreePhysMemory(), is(equalTo(freePhysicalMemory2)));
			assertThat(memoryData.getMaxFreePhysMemory(), is(equalTo(freePhysicalMemory)));
			assertThat(memoryData.getTotalFreePhysMemory(), is(equalTo(freePhysicalMemory + freePhysicalMemory2)));

			assertThat(memoryData.getMinFreeSwapSpace(), is(equalTo(freeSwapSpace2)));
			assertThat(memoryData.getMaxFreeSwapSpace(), is(equalTo(freeSwapSpace)));
			assertThat(memoryData.getTotalFreeSwapSpace(), is(equalTo(freeSwapSpace + freeSwapSpace2)));

			assertThat(memoryData.getMinUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize)));
			assertThat(memoryData.getMaxUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize2)));
			assertThat(memoryData.getTotalUsedHeapMemorySize(), is(equalTo(usedHeapMemorySize + usedHeapMemorySize2)));

			assertThat(memoryData.getMinUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize)));
			assertThat(memoryData.getMaxUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize2)));
			assertThat(memoryData.getTotalUsedNonHeapMemorySize(), is(equalTo(usedNonHeapMemorySize + usedNonHeapMemorySize2)));
		}

		@Test
		public void idNotAvailableTest() throws IdNotAvailableException {
			long freePhysicalMemory = 37566L;
			long freeSwapSpace = 578300L;
			long committedVirtualMemorySize = 12345L;
			long usedHeapMemorySize = 3827L;
			long usedNonHeapMemorySize = 12200L;
			long committedHeapMemorySize = 5056L;
			long committedNonHeapMemorySize = 14016L;
			long sensorTypeIdent = 13L;

			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			when(osBean.getFreePhysicalMemorySize()).thenReturn(freePhysicalMemory);
			when(osBean.getFreeSwapSpaceSize()).thenReturn(freeSwapSpace);
			when(osBean.getCommittedVirtualMemorySize()).thenReturn(committedVirtualMemorySize);
			when(memoryBean.getHeapMemoryUsage().getCommitted()).thenReturn(committedHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getUsed()).thenReturn(usedHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getCommitted()).thenReturn(committedNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getUsed()).thenReturn(usedNonHeapMemorySize);

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenThrow(new IdNotAvailableException("expected"));

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			memoryInfo.update(coreService);

			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(0)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());
		}

	}
}
