package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryUsage;
import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

	/** The mocked {@link SystemInformationData}. */
	@Mock
	SystemInformationData collector;

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
	 * Tests the {@link SystemInformation#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends SystemInformationTest {

		@Test
		void captureForTheFirstTime() {
			MemoryUsage memoryUsage = mock(MemoryUsage.class);

			when(memoryUsage.getInit()).thenReturn(1L).thenReturn(5L);
			when(memoryUsage.getMax()).thenReturn(10L).thenReturn(50L);

			when(this.osBean.getTotalPhysicalMemorySize()).thenReturn(1L);
			when(this.osBean.getTotalSwapSpaceSize()).thenReturn(2L);
			when(this.osBean.getAvailableProcessors()).thenReturn(3);
			when(this.osBean.getArch()).thenReturn("The Arch");
			when(this.osBean.getName()).thenReturn("The Name");
			when(this.osBean.getVersion()).thenReturn("The Version");
			when(this.runtimeBean.getJitCompilerName()).thenReturn("The Jit Compiler name");
			when(this.runtimeBean.getClassPath()).thenReturn("The Classpath");
			when(this.runtimeBean.getBootClassPath()).thenReturn("The Boot classpath");
			when(this.runtimeBean.getLibraryPath()).thenReturn("The Library path");
			when(this.runtimeBean.getVmVendor()).thenReturn("The Vm vendor");
			when(this.runtimeBean.getVmVersion()).thenReturn("The Vm version");
			when(this.runtimeBean.getVmName()).thenReturn("The Vm name");
			when(this.runtimeBean.getSpecName()).thenReturn("The Spec name");
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsage);

			this.cut.get();

			verify(this.collector).setTimeStamp(Mockito.any(Timestamp.class));
			verify(this.collector).setTotalPhysMemory(1L);
			verify(this.collector).setTotalSwapSpace(2L);
			verify(this.collector).setAvailableProcessors(3);
			verify(this.collector).setArchitecture("The Arch");
			verify(this.collector).setOsName("The Name");
			verify(this.collector).setOsVersion("The Version");
			verify(this.collector).setJitCompilerName("The Jit Compiler name");
			verify(this.collector).setClassPath("The Classpath");
			verify(this.collector).setBootClassPath("The Boot classpath");
			verify(this.collector).setLibraryPath("The Library path");
			verify(this.collector).setVmVendor("The Vm vendor");
			verify(this.collector).setVmVersion("The Vm version");
			verify(this.collector).setVmName("The Vm name");
			verify(this.collector).setVmSpecName("The Spec name");
			verify(this.collector).setInitHeapMemorySize(1L);
			verify(this.collector).setMaxHeapMemorySize(10L);
			verify(this.collector).setInitNonHeapMemorySize(5L);
			verify(this.collector).setMaxNonHeapMemorySize(50L);
			verify(this.collector, atLeastOnce()).addVMArguments(Mockito.anyString(), Mockito.anyString());
		}

		@Test
		void captureForTheSecondTime() {
			MemoryUsage memoryUsage = mock(MemoryUsage.class);

			when(memoryUsage.getInit()).thenReturn(1L).thenReturn(5L);
			when(memoryUsage.getMax()).thenReturn(10L).thenReturn(50L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsage);

			when(this.collector.getTimeStamp()).thenReturn(new Timestamp(1L));

			Object firstExecution = this.cut.get();
			Object secondExecution = this.cut.get();

			assertThat(firstExecution, is(notNullValue()));
			assertThat(secondExecution, is(nullValue()));
		}
	}
}
