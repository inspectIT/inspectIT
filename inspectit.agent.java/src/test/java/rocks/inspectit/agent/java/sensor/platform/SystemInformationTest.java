package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryUsage;
import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link SystemInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class SystemInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	SystemInformation cut;

	/** The mocked {@link OperatingSystemInfoProvider}. */
	@Mock
	OperatingSystemInfoProvider osBean;

	/** The mocked {@link MemoryInfoProvider}. */
	@Mock
	MemoryInfoProvider memoryBean;

	/** The mocked {@link RuntimeInfoProvider}. */
	@Mock
	RuntimeInfoProvider runtimeBean;

	/**
	 * Tests the {@link SystemInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends SystemInformationTest {

		@Test
		void gatherForTheFirstTime() {
			SystemInformationData collector = (SystemInformationData) this.cut.getSystemSensorData();

			collector.setTimeStamp(new Timestamp(1L));

			when(this.osBean.getTotalPhysicalMemorySize()).thenReturn(2L);
			when(this.osBean.getTotalSwapSpaceSize()).thenReturn(3L);
			when(this.osBean.getAvailableProcessors()).thenReturn(4);
			when(this.osBean.getArch()).thenReturn("5");
			when(this.osBean.getName()).thenReturn("6");
			when(this.osBean.getVersion()).thenReturn("7");

			when(this.runtimeBean.getJitCompilerName()).thenReturn("8");
			when(this.runtimeBean.getClassPath()).thenReturn("9");
			when(this.runtimeBean.getBootClassPath()).thenReturn("10");
			when(this.runtimeBean.getLibraryPath()).thenReturn("11");
			when(this.runtimeBean.getVmVendor()).thenReturn("12");
			when(this.runtimeBean.getVmVersion()).thenReturn("13");
			when(this.runtimeBean.getVmName()).thenReturn("14");
			when(this.runtimeBean.getSpecName()).thenReturn("15");

			MemoryUsage heapMemoryUsage = mock(MemoryUsage.class);
			when(heapMemoryUsage.getInit()).thenReturn(16L);
			when(heapMemoryUsage.getMax()).thenReturn(17L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);

			MemoryUsage nonHeapMemoryUsage = mock(MemoryUsage.class);
			when(nonHeapMemoryUsage.getInit()).thenReturn(18L);
			when(nonHeapMemoryUsage.getMax()).thenReturn(19L);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			collector.setPlatformIdent(20L);
			collector.setSensorTypeIdent(21L);

			this.cut.gather();
			SystemInformationData systemInformationData = (SystemInformationData) this.cut.getSystemSensorData();

			assertThat(systemInformationData.getTimeStamp().getTime(), is(not(1L)));

			assertThat(systemInformationData.getTotalPhysMemory(), is(2L));
			assertThat(systemInformationData.getTotalSwapSpace(), is(3L));
			assertThat(systemInformationData.getAvailableProcessors(), is(4));
			assertThat(systemInformationData.getArchitecture(), is("5"));
			assertThat(systemInformationData.getOsName(), is("6"));
			assertThat(systemInformationData.getOsVersion(), is("7"));

			assertThat(systemInformationData.getJitCompilerName(), is("8"));
			assertThat(systemInformationData.getClassPath(), is("9"));
			assertThat(systemInformationData.getBootClassPath(), is("10"));
			assertThat(systemInformationData.getLibraryPath(), is("11"));
			assertThat(systemInformationData.getVmVendor(), is("12"));
			assertThat(systemInformationData.getVmVersion(), is("13"));
			assertThat(systemInformationData.getVmName(), is("14"));
			assertThat(systemInformationData.getVmSpecName(), is("15"));

			assertThat(systemInformationData.getInitHeapMemorySize(), is(16L));
			assertThat(systemInformationData.getMaxHeapMemorySize(), is(17L));

			assertThat(systemInformationData.getInitNonHeapMemorySize(), is(18L));
			assertThat(systemInformationData.getMaxNonHeapMemorySize(), is(19L));

			assertThat(systemInformationData.getPlatformIdent(), is(20L));
			assertThat(systemInformationData.getSensorTypeIdent(), is(21L));
		}

		@Test
		void gatherForTheSecondTime() {
			// First time
			SystemInformationData collector = (SystemInformationData) this.cut.getSystemSensorData();

			collector.setTimeStamp(new Timestamp(1L));

			when(this.osBean.getTotalPhysicalMemorySize()).thenReturn(2L);
			when(this.osBean.getTotalSwapSpaceSize()).thenReturn(3L);
			when(this.osBean.getAvailableProcessors()).thenReturn(4);
			when(this.osBean.getArch()).thenReturn("5");
			when(this.osBean.getName()).thenReturn("6");
			when(this.osBean.getVersion()).thenReturn("7");

			when(this.runtimeBean.getJitCompilerName()).thenReturn("8");
			when(this.runtimeBean.getClassPath()).thenReturn("9");
			when(this.runtimeBean.getBootClassPath()).thenReturn("10");
			when(this.runtimeBean.getLibraryPath()).thenReturn("11");
			when(this.runtimeBean.getVmVendor()).thenReturn("12");
			when(this.runtimeBean.getVmVersion()).thenReturn("13");
			when(this.runtimeBean.getVmName()).thenReturn("14");
			when(this.runtimeBean.getSpecName()).thenReturn("15");

			MemoryUsage heapMemoryUsageA = mock(MemoryUsage.class);
			when(heapMemoryUsageA.getInit()).thenReturn(16L);
			when(heapMemoryUsageA.getMax()).thenReturn(17L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsageA);

			MemoryUsage nonHeapMemoryUsageA = mock(MemoryUsage.class);
			when(nonHeapMemoryUsageA.getInit()).thenReturn(18L);
			when(nonHeapMemoryUsageA.getMax()).thenReturn(19L);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsageA);

			collector.setPlatformIdent(20L);
			collector.setSensorTypeIdent(21L);

			this.cut.gather();

			// Second time
			collector = (SystemInformationData) this.cut.getSystemSensorData();

			collector.setTimeStamp(new Timestamp(100L));

			when(this.osBean.getTotalPhysicalMemorySize()).thenReturn(200L);
			when(this.osBean.getTotalSwapSpaceSize()).thenReturn(300L);
			when(this.osBean.getAvailableProcessors()).thenReturn(400);
			when(this.osBean.getArch()).thenReturn("500");
			when(this.osBean.getName()).thenReturn("600");
			when(this.osBean.getVersion()).thenReturn("700");

			when(this.runtimeBean.getJitCompilerName()).thenReturn("800");
			when(this.runtimeBean.getClassPath()).thenReturn("900");
			when(this.runtimeBean.getBootClassPath()).thenReturn("1000");
			when(this.runtimeBean.getLibraryPath()).thenReturn("1100");
			when(this.runtimeBean.getVmVendor()).thenReturn("1200");
			when(this.runtimeBean.getVmVersion()).thenReturn("1300");
			when(this.runtimeBean.getVmName()).thenReturn("1400");
			when(this.runtimeBean.getSpecName()).thenReturn("1500");

			MemoryUsage heapMemoryUsageB = mock(MemoryUsage.class);
			when(heapMemoryUsageB.getInit()).thenReturn(1600L);
			when(heapMemoryUsageB.getMax()).thenReturn(1700L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsageB);

			MemoryUsage nonHeapMemoryUsageB = mock(MemoryUsage.class);
			when(nonHeapMemoryUsageB.getInit()).thenReturn(1800L);
			when(nonHeapMemoryUsageB.getMax()).thenReturn(1900L);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsageB);

			this.cut.gather();
			SystemInformationData systemInformationData = (SystemInformationData) this.cut.getSystemSensorData();

			assertThat(systemInformationData.getTimeStamp().getTime(), is(not(1L)));

			assertThat(systemInformationData.getTotalPhysMemory(), is(2L));
			assertThat(systemInformationData.getTotalSwapSpace(), is(3L));
			assertThat(systemInformationData.getAvailableProcessors(), is(4));
			assertThat(systemInformationData.getArchitecture(), is("5"));
			assertThat(systemInformationData.getOsName(), is("6"));
			assertThat(systemInformationData.getOsVersion(), is("7"));

			assertThat(systemInformationData.getJitCompilerName(), is("8"));
			assertThat(systemInformationData.getClassPath(), is("9"));
			assertThat(systemInformationData.getBootClassPath(), is("10"));
			assertThat(systemInformationData.getLibraryPath(), is("11"));
			assertThat(systemInformationData.getVmVendor(), is("12"));
			assertThat(systemInformationData.getVmVersion(), is("13"));
			assertThat(systemInformationData.getVmName(), is("14"));
			assertThat(systemInformationData.getVmSpecName(), is("15"));

			assertThat(systemInformationData.getInitHeapMemorySize(), is(16L));
			assertThat(systemInformationData.getMaxHeapMemorySize(), is(17L));

			assertThat(systemInformationData.getInitNonHeapMemorySize(), is(18L));
			assertThat(systemInformationData.getMaxNonHeapMemorySize(), is(19L));

			assertThat(systemInformationData.getPlatformIdent(), is(20L));
			assertThat(systemInformationData.getSensorTypeIdent(), is(21L));
		}
	}

	/**
	 * Tests the {@link SystemInformation#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends SystemInformationTest {
		@Test
		void getForTheFirstTime() {
			SystemInformationData systemInformationData = (SystemInformationData) this.cut.getSystemSensorData();

			Object firstExecution = this.cut.get();

			assertThat((SystemInformationData) firstExecution, is(systemInformationData));
		}

		@Test
		void getForTheSecondTime() {
			this.cut.get();
			Object secondExecution = this.cut.get();

			assertThat(secondExecution, is(nullValue()));
		}
	}
}
