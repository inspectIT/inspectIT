package rocks.inspectit.server.influx.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SystemInformationPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	SystemInformationPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	SystemInformationData data;

	public class CreateBuilder extends SystemInformationPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final String AGENT_NAME = "Agent";

		@BeforeMethod
		public void setup() {
			when(platformIdent.getAgentName()).thenReturn(AGENT_NAME);
		}

		@Test
		public void happyPath() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getArchitecture()).thenReturn("arch");
			when(data.getAvailableProcessors()).thenReturn(RandomUtils.nextInt());
			when(data.getBootClassPath()).thenReturn("bootclph");
			when(data.getClassPath()).thenReturn("clph");
			when(data.getInitHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getInitNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getJitCompilerName()).thenReturn("jitcmp");
			when(data.getLibraryPath()).thenReturn("libpath");
			when(data.getMaxHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getMaxNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getOsName()).thenReturn("osname");
			when(data.getOsVersion()).thenReturn("osver");
			when(data.getTotalPhysMemory()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalSwapSpace()).thenReturn(RandomUtils.nextLong());
			when(data.getVmName()).thenReturn("vmname");
			when(data.getVmSpecName()).thenReturn("vmspec");
			when(data.getVmVendor()).thenReturn("vmvendor");
			when(data.getVmVersion()).thenReturn("vmversion");

			Collection<Builder> pointBuilderCol = builder.createBuilders(data);
			assertThat(pointBuilderCol.size(), is(1));
			Builder pointBuilder = pointBuilderCol.iterator().next();

			assertThat(getMeasurement(pointBuilder), is(Series.SystemInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_ARCHITECTURE, (Object) data.getArchitecture()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_BOOT_CLASS_PATH, (Object) data.getBootClassPath()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_CLASS_PATH, (Object) data.getClassPath()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_INIT_HEAP_MEMORY_SIZE, (Object) data.getInitHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_INIT_NON_HEAP_MEMORY_SIZE, (Object) data.getInitNonHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_JIT_COMPILER_NAME, (Object) data.getJitCompilerName()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_LIBRARY_PATH, (Object) data.getLibraryPath()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_MAX_HEAP_SIZE, (Object) data.getMaxHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_MAX_NON_HEAP_SIZE, (Object) data.getMaxNonHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_NUM_AVAILABLE_PROCESSORS, (Object) Long.valueOf(data.getAvailableProcessors())));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_OS_NAME, (Object) data.getOsName()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_OS_VERSION, (Object) data.getOsVersion()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_TOTAL_PHYS_MEMORY, (Object) data.getTotalPhysMemory()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_TOTAL_SWAP_SPACE, (Object) data.getTotalSwapSpace()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_NAME, (Object) data.getVmName()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_SPEC_NAME, (Object) data.getVmSpecName()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_VENDOR, (Object) data.getVmVendor()));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_VERSION, (Object) data.getVmVersion()));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));

			Collection<Builder> pointBuilderCol = builder.createBuilders(data);
			assertThat(pointBuilderCol.size(), is(1));
			Builder pointBuilder = pointBuilderCol.iterator().next();

			assertThat(getMeasurement(pointBuilder), is(Series.SystemInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
		}

		@Test
		public void vmArgs() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			Set<VmArgumentData> vmSet = new HashSet<>();
			vmSet.add(new VmArgumentData("key1", "value1"));
			vmSet.add(new VmArgumentData("key2", "value2"));
			when(data.getVmSet()).thenReturn(vmSet );

			Collection<Builder> pointBuilderCol = builder.createBuilders(data);
			assertThat(pointBuilderCol.size(), is(1));
			Builder pointBuilder = pointBuilderCol.iterator().next();

			assertThat(getMeasurement(pointBuilder), is(Series.SystemInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			Object vmArgs = "key1=value1" + System.getProperty("line.separator") + "key2=value2";
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_ATTRIBUTES, vmArgs));
		}

		@Test
		public void stringsNull() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getArchitecture()).thenReturn(null);
			when(data.getBootClassPath()).thenReturn(null);
			when(data.getClassPath()).thenReturn(null);
			when(data.getJitCompilerName()).thenReturn(null);
			when(data.getLibraryPath()).thenReturn(null);
			when(data.getOsName()).thenReturn(null);
			when(data.getOsVersion()).thenReturn(null);
			when(data.getVmName()).thenReturn(null);
			when(data.getVmSpecName()).thenReturn(null);
			when(data.getVmVendor()).thenReturn(null);
			when(data.getVmVersion()).thenReturn(null);

			Collection<Builder> pointBuilderCol = builder.createBuilders(data);
			assertThat(pointBuilderCol.size(), is(1));
			Builder pointBuilder = pointBuilderCol.iterator().next();

			assertThat(getMeasurement(pointBuilder), is(Series.SystemInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_ARCHITECTURE, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_BOOT_CLASS_PATH, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_CLASS_PATH, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_JIT_COMPILER_NAME, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_LIBRARY_PATH, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_OS_NAME, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_OS_VERSION, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_NAME, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_SPEC_NAME, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_VENDOR, (Object) StringUtils.EMPTY));
			assertThat(getFields(pointBuilder), hasEntry(Series.SystemInformation.FIELD_VM_VERSION, (Object) StringUtils.EMPTY));
		}

	}

}
