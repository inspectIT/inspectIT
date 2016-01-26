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
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class SystemInformationTest extends TestBase {

	@InjectMocks
	SystemInformation systemInfo;

	@Mock
	MemoryInfoProvider memoryBean;

	@Mock
	OperatingSystemInfoProvider osBean;

	@Mock
	RuntimeInfoProvider runtimeBean;

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
		Field field = systemInfo.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(systemInfo, osBean);

		// we have to replace the real memoryBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		field = systemInfo.getClass().getDeclaredField("memoryBean");
		field.setAccessible(true);
		field.set(systemInfo, memoryBean);

		// we have to replace the real runtimeBean by the mocked one, so that we don't retrieve the
		// info from the underlying JVM
		field = systemInfo.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(systemInfo, runtimeBean);
	}

	public class Update extends SystemInformationTest {

		@Test
		public void oneStaticDataSet() throws IdNotAvailableException {
			long totalPhysMemory = 775000L;
			long totalSwapSpace = 555000L;
			int availableProcessors = 4;
			String architecture = "i386";
			String osName = "linux";
			String osVersion = "2.26";
			String jitCompilerName = "HotSpot Client Compiler";
			String classPath = "thisIsTheClassPath";
			String bootClassPath = "thisIsTheBootClassPath";
			String libraryPath = "thisIsTheLibraryPath";
			String vmVendor = "Sun Microsystems";
			String vmVersion = "1.5.0_15";
			String vmName = "inspectit-vm";
			String vmSpecName = "Java Virtual Machine";
			long initHeapMemorySize = 4000L;
			long maxHeapMemorySize = 10000L;
			long initNonHeapMemorySize = 12000L;
			long maxNonHeapMemorySize = 14000L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			when(osBean.getArch()).thenReturn(architecture);
			when(osBean.getAvailableProcessors()).thenReturn(availableProcessors);
			when(osBean.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
			when(osBean.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
			when(osBean.getVersion()).thenReturn(osVersion);
			when(osBean.getName()).thenReturn(osName);
			when(runtimeBean.getJitCompilerName()).thenReturn(jitCompilerName);
			when(runtimeBean.getClassPath()).thenReturn(classPath);
			when(runtimeBean.getBootClassPath()).thenReturn(bootClassPath);
			when(runtimeBean.getLibraryPath()).thenReturn(libraryPath);
			when(runtimeBean.getVmName()).thenReturn(vmName);
			when(runtimeBean.getVmVendor()).thenReturn(vmVendor);
			when(runtimeBean.getVmVersion()).thenReturn(vmVersion);
			when(runtimeBean.getSpecName()).thenReturn(vmSpecName);
			when(memoryBean.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);

			systemInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(SystemInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			SystemInformationData systemData = (SystemInformationData) sensorData;

			// as there was only one data object values must be the
			// same
			assertThat(systemData.getArchitecture(), is(equalTo(architecture)));
			assertThat(systemData.getAvailableProcessors(), is(equalTo(availableProcessors)));
			assertThat(systemData.getBootClassPath(), is(equalTo(bootClassPath)));
			assertThat(systemData.getClassPath(), is(equalTo(classPath)));
			assertThat(systemData.getInitHeapMemorySize(), is(equalTo(initHeapMemorySize)));
			assertThat(systemData.getInitNonHeapMemorySize(), is(equalTo(initNonHeapMemorySize)));
			assertThat(systemData.getJitCompilerName(), is(equalTo(jitCompilerName)));
			assertThat(systemData.getLibraryPath(), is(equalTo(libraryPath)));
			assertThat(systemData.getMaxHeapMemorySize(), is(equalTo(maxHeapMemorySize)));
			assertThat(systemData.getMaxNonHeapMemorySize(), is(equalTo(maxNonHeapMemorySize)));
			assertThat(systemData.getOsName(), is(equalTo(osName)));
			assertThat(systemData.getOsVersion(), is(equalTo(osVersion)));
			assertThat(systemData.getTotalPhysMemory(), is(equalTo(totalPhysMemory)));
			assertThat(systemData.getTotalSwapSpace(), is(equalTo(totalSwapSpace)));
			assertThat(systemData.getVmName(), is(equalTo(vmName)));
			assertThat(systemData.getVmSpecName(), is(equalTo(vmSpecName)));
			assertThat(systemData.getVmVendor(), is(equalTo(vmVendor)));
			assertThat(systemData.getVmVersion(), is(equalTo(vmVersion)));
		}

		/**
		 * This testcase combines different testcases that simulate the absense of static information.
		 * Realizing each case separately would require many code with almost no additional value.
		 *
		 * Maybe this test is obsolete because we don't expect an exception to be thrown directly in
		 * {@link SystemInformation} but only in the getter methods of {@link DefaultRuntimeMXBean}
		 *
		 * @throws IdNotAvailableException
		 */
		@Test
		public void informationNotAvailable() throws IdNotAvailableException {
			long totalPhysMemory = 775000L;
			long totalSwapSpace = 555000L;
			int availableProcessors = 4;
			String empty = "";
			String jitCompilerName = "HotSpot Client Compiler";
			String vmName = "inspectit-vm";
			long initHeapMemorySize = 4000L;
			long maxHeapMemorySize = 10000L;
			long initNonHeapMemorySize = 12000L;
			long maxNonHeapMemorySize = 14000L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			when(osBean.getArch()).thenReturn("");
			when(osBean.getAvailableProcessors()).thenReturn(availableProcessors);
			when(osBean.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
			when(osBean.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
			when(osBean.getVersion()).thenReturn("");
			when(osBean.getName()).thenReturn("");
			when(runtimeBean.getJitCompilerName()).thenReturn(jitCompilerName);
			when(runtimeBean.getClassPath()).thenReturn("");
			when(runtimeBean.getBootClassPath()).thenReturn("");
			when(runtimeBean.getLibraryPath()).thenReturn("");
			when(runtimeBean.getVmName()).thenReturn(vmName);
			when(runtimeBean.getVmVendor()).thenReturn("");
			when(runtimeBean.getVmVersion()).thenReturn("");
			when(runtimeBean.getSpecName()).thenReturn("");
			when(memoryBean.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			systemInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(SystemInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			SystemInformationData systemData = (SystemInformationData) sensorData;

			// as there was only one data object the values must be the
			// same
			assertThat(systemData.getArchitecture(), is(equalTo(empty)));
			assertThat(systemData.getAvailableProcessors(), is(equalTo(availableProcessors)));
			assertThat(systemData.getBootClassPath(), is(equalTo(empty)));
			assertThat(systemData.getClassPath(), is(equalTo(empty)));
			assertThat(systemData.getInitHeapMemorySize(), is(equalTo(initHeapMemorySize)));
			assertThat(systemData.getInitNonHeapMemorySize(), is(equalTo(initNonHeapMemorySize)));
			assertThat(systemData.getJitCompilerName(), is(equalTo(jitCompilerName)));
			assertThat(systemData.getLibraryPath(), is(equalTo(empty)));
			assertThat(systemData.getMaxHeapMemorySize(), is(equalTo(maxHeapMemorySize)));
			assertThat(systemData.getMaxNonHeapMemorySize(), is(equalTo(maxNonHeapMemorySize)));
			assertThat(systemData.getOsName(), is(equalTo(empty)));
			assertThat(systemData.getOsVersion(), is(equalTo(empty)));
			assertThat(systemData.getTotalPhysMemory(), is(equalTo(totalPhysMemory)));
			assertThat(systemData.getTotalSwapSpace(), is(equalTo(totalSwapSpace)));
			assertThat(systemData.getVmName(), is(equalTo(vmName)));
			assertThat(systemData.getVmSpecName(), is(equalTo(empty)));
			assertThat(systemData.getVmVendor(), is(equalTo(empty)));
			assertThat(systemData.getVmVersion(), is(equalTo(empty)));
		}

		/**
		 * Maybe this test is obsolete because we don't expect an exception to be thrown directly in
		 * {@link SystemInformation#getBootClassPath()} but only in
		 * {@link DefaultRuntimeMXBean#getBootClassPath()}
		 *
		 * @throws IdNotAvailableException
		 */
		@Test
		public void bootClassPathNotSupported() throws IdNotAvailableException {
			long totalPhysMemory = 775000L;
			long totalSwapSpace = 555000L;
			int availableProcessors = 4;
			String architecture = "i386";
			String osName = "linux";
			String osVersion = "2.26";
			String jitCompilerName = "HotSpot Client Compiler";
			String classPath = "thisIsTheClassPath";
			String bootClassPath = "";
			String libraryPath = "thisIsTheLibraryPath";
			String vmVendor = "Sun Microsystems";
			String vmVersion = "1.5.0_15";
			String vmName = "inspectit-vm";
			String vmSpecName = "Java Virtual Machine";
			long initHeapMemorySize = 4000L;
			long maxHeapMemorySize = 10000L;
			long initNonHeapMemorySize = 12000L;
			long maxNonHeapMemorySize = 14000L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			when(osBean.getArch()).thenReturn(architecture);
			when(osBean.getAvailableProcessors()).thenReturn(availableProcessors);
			when(osBean.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
			when(osBean.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
			when(osBean.getVersion()).thenReturn(osVersion);
			when(osBean.getName()).thenReturn(osName);
			when(runtimeBean.getJitCompilerName()).thenReturn(jitCompilerName);
			when(runtimeBean.getClassPath()).thenReturn(classPath);
			when(runtimeBean.getBootClassPath()).thenReturn("");
			when(runtimeBean.getLibraryPath()).thenReturn(libraryPath);
			when(runtimeBean.getVmName()).thenReturn(vmName);
			when(runtimeBean.getVmVendor()).thenReturn(vmVendor);
			when(runtimeBean.getVmVersion()).thenReturn(vmVersion);
			when(runtimeBean.getSpecName()).thenReturn(vmSpecName);
			when(memoryBean.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			systemInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(SystemInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			SystemInformationData systemData = (SystemInformationData) sensorData;

			// as there was only one data object values must be the
			// same
			assertThat(systemData.getArchitecture(), is(equalTo(architecture)));
			assertThat(systemData.getAvailableProcessors(), is(equalTo(availableProcessors)));
			assertThat(systemData.getBootClassPath(), is(equalTo(bootClassPath)));
			assertThat(systemData.getClassPath(), is(equalTo(classPath)));
			assertThat(systemData.getInitHeapMemorySize(), is(equalTo(initHeapMemorySize)));
			assertThat(systemData.getInitNonHeapMemorySize(), is(equalTo(initNonHeapMemorySize)));
			assertThat(systemData.getJitCompilerName(), is(equalTo(jitCompilerName)));
			assertThat(systemData.getLibraryPath(), is(equalTo(libraryPath)));
			assertThat(systemData.getMaxHeapMemorySize(), is(equalTo(maxHeapMemorySize)));
			assertThat(systemData.getMaxNonHeapMemorySize(), is(equalTo(maxNonHeapMemorySize)));
			assertThat(systemData.getOsName(), is(equalTo(osName)));
			assertThat(systemData.getOsVersion(), is(equalTo(osVersion)));
			assertThat(systemData.getTotalPhysMemory(), is(equalTo(totalPhysMemory)));
			assertThat(systemData.getTotalSwapSpace(), is(equalTo(totalSwapSpace)));
			assertThat(systemData.getVmName(), is(equalTo(vmName)));
			assertThat(systemData.getVmSpecName(), is(equalTo(vmSpecName)));
			assertThat(systemData.getVmVendor(), is(equalTo(vmVendor)));
			assertThat(systemData.getVmVersion(), is(equalTo(vmVersion)));
		}

		@Test
		public void valueTooLong() throws IdNotAvailableException {
			String tooLong = fillString('x', 10001);
			String limit = fillString('x', 10000);

			long totalPhysMemory = 775000L;
			long totalSwapSpace = 555000L;
			int availableProcessors = 4;
			String architecture = "i386";
			String osName = "linux";
			String osVersion = "2.26";
			String jitCompilerName = "HotSpot Client Compiler";
			String vmVendor = "Sun Microsystems";
			String vmVersion = "1.5.0_15";
			String vmName = "inspectit-vm";
			String vmSpecName = "Java Virtual Machine";
			long initHeapMemorySize = 4000L;
			long maxHeapMemorySize = 10000L;
			long initNonHeapMemorySize = 12000L;
			long maxNonHeapMemorySize = 14000L;
			long sensorTypeIdent = 13L;
			long platformIdent = 11L;

			when(sensorTypeConfig.getId()).thenReturn(sensorTypeIdent);
			when(platformManager.getPlatformId()).thenReturn(platformIdent);
			when(memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
			when(memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

			when(osBean.getArch()).thenReturn(architecture);
			when(osBean.getAvailableProcessors()).thenReturn(availableProcessors);
			when(osBean.getTotalPhysicalMemorySize()).thenReturn(totalPhysMemory);
			when(osBean.getTotalSwapSpaceSize()).thenReturn(totalSwapSpace);
			when(osBean.getVersion()).thenReturn(osVersion);
			when(osBean.getName()).thenReturn(osName);
			when(runtimeBean.getJitCompilerName()).thenReturn(jitCompilerName);
			when(runtimeBean.getClassPath()).thenReturn(tooLong);
			when(runtimeBean.getBootClassPath()).thenReturn(tooLong);
			when(runtimeBean.getLibraryPath()).thenReturn(tooLong);
			when(runtimeBean.getVmName()).thenReturn(vmName);
			when(runtimeBean.getVmVendor()).thenReturn(vmVendor);
			when(runtimeBean.getVmVersion()).thenReturn(vmVersion);
			when(runtimeBean.getSpecName()).thenReturn(vmSpecName);
			when(memoryBean.getHeapMemoryUsage().getInit()).thenReturn(initHeapMemorySize);
			when(memoryBean.getHeapMemoryUsage().getMax()).thenReturn(maxHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getInit()).thenReturn(initNonHeapMemorySize);
			when(memoryBean.getNonHeapMemoryUsage().getMax()).thenReturn(maxNonHeapMemorySize);

			// there is no current data object available
			when(coreService.getPlatformSensorData(sensorTypeIdent)).thenReturn(null);
			systemInfo.update(coreService);

			// -> The service must create a new one and add it to the storage
			// We use an argument capturer to further inspect the given argument.
			ArgumentCaptor<SystemSensorData> sensorDataCaptor = ArgumentCaptor.forClass(SystemSensorData.class);
			verify(coreService, times(1)).addPlatformSensorData(eq(sensorTypeIdent), sensorDataCaptor.capture());

			SystemSensorData sensorData = sensorDataCaptor.getValue();
			assertThat(sensorData, is(instanceOf(SystemInformationData.class)));
			assertThat(sensorData.getPlatformIdent(), is(equalTo(platformIdent)));
			assertThat(sensorData.getSensorTypeIdent(), is(equalTo(sensorTypeIdent)));

			SystemInformationData systemData = (SystemInformationData) sensorData;

			// as there was only one data object the values must be the
			// same
			assertThat(systemData.getArchitecture(), is(equalTo(architecture)));
			assertThat(systemData.getAvailableProcessors(), is(equalTo(availableProcessors)));
			assertThat(systemData.getBootClassPath(), is(equalTo(limit)));
			assertThat(systemData.getClassPath(), is(equalTo(limit)));
			assertThat(systemData.getInitHeapMemorySize(), is(equalTo(initHeapMemorySize)));
			assertThat(systemData.getInitNonHeapMemorySize(), is(equalTo(initNonHeapMemorySize)));
			assertThat(systemData.getJitCompilerName(), is(equalTo(jitCompilerName)));
			assertThat(systemData.getLibraryPath(), is(equalTo(limit)));
			assertThat(systemData.getMaxHeapMemorySize(), is(equalTo(maxHeapMemorySize)));
			assertThat(systemData.getMaxNonHeapMemorySize(), is(equalTo(maxNonHeapMemorySize)));
			assertThat(systemData.getOsName(), is(equalTo(osName)));
			assertThat(systemData.getOsVersion(), is(equalTo(osVersion)));
			assertThat(systemData.getTotalPhysMemory(), is(equalTo(totalPhysMemory)));
			assertThat(systemData.getTotalSwapSpace(), is(equalTo(totalSwapSpace)));
			assertThat(systemData.getVmName(), is(equalTo(vmName)));
			assertThat(systemData.getVmSpecName(), is(equalTo(vmSpecName)));
			assertThat(systemData.getVmVendor(), is(equalTo(vmVendor)));
			assertThat(systemData.getVmVersion(), is(equalTo(vmVersion)));
		}

		/**
		 * Creates a new String with the specified length.
		 *
		 * @param character
		 * @param count
		 * @return
		 */
		private String fillString(char character, int count) {
			// creates a string of 'x' repeating characters
			char[] chars = new char[count];
			while (count > 0) {
				chars[--count] = character;
			}
			return new String(chars);
		}

	}
}
