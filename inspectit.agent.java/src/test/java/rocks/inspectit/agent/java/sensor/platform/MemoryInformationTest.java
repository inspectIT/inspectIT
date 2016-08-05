package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryUsage;
import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link MemoryInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class MemoryInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	MemoryInformation cut;

	/** The mocked {@link MemoryInfoProvider}. */
	@Mock
	MemoryInfoProvider memoryBean;

	/** The mocked {@link OperatingSystemInfoProvider}. */
	@Mock
	OperatingSystemInfoProvider osBean;

	/**
	 * Tests the {@link MemoryInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends MemoryInformationTest {

		@Test
		void freeMemoryIsCalculated() {
			this.mockCollectorWithDefaults();

			when(this.osBean.getFreePhysicalMemorySize()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinFreePhysMemory(), is(9L));
			assertThat(collector.getMaxFreePhysMemory(), is(11L));
			assertThat(collector.getTotalFreePhysMemory(), is(40L));
		}

		@Test
		void freeSwapSpaceIsCalculated() {
			this.mockCollectorWithDefaults();

			when(this.osBean.getFreeSwapSpaceSize()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinFreeSwapSpace(), is(9L));
			assertThat(collector.getMaxFreeSwapSpace(), is(11L));
			assertThat(collector.getTotalFreeSwapSpace(), is(40L));
		}

		@Test
		void comittedVirtualMemSizeIsCalculated() {
			this.mockCollectorWithDefaults();

			when(this.osBean.getCommittedVirtualMemorySize()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinComittedVirtualMemSize(), is(9L));
			assertThat(collector.getMaxComittedVirtualMemSize(), is(11L));
			assertThat(collector.getTotalComittedVirtualMemSize(), is(40L));
		}

		@Test
		void usedHeapMemorySizeIsCalculated() {
			this.mockCollectorWithDefaults();

			MemoryUsage heapMemoryUsage = this.memoryBean.getHeapMemoryUsage();
			when(heapMemoryUsage.getUsed()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinUsedHeapMemorySize(), is(9L));
			assertThat(collector.getMaxUsedHeapMemorySize(), is(11L));
			assertThat(collector.getTotalUsedHeapMemorySize(), is(40L));
		}

		@Test
		void comittedHeapMemorySizeIsCalculated() {
			this.mockCollectorWithDefaults();

			MemoryUsage heapMemoryUsage = this.memoryBean.getHeapMemoryUsage();
			when(heapMemoryUsage.getCommitted()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinComittedHeapMemorySize(), is(9L));
			assertThat(collector.getMaxComittedHeapMemorySize(), is(11L));
			assertThat(collector.getTotalComittedHeapMemorySize(), is(40L));
		}

		@Test
		void usedNonHeapMemorySizeIsCalculated() {
			this.mockCollectorWithDefaults();

			MemoryUsage nonHeapMemoryUsage = this.memoryBean.getNonHeapMemoryUsage();
			when(nonHeapMemoryUsage.getUsed()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinUsedNonHeapMemorySize(), is(9L));
			assertThat(collector.getMaxUsedNonHeapMemorySize(), is(11L));
			assertThat(collector.getTotalUsedNonHeapMemorySize(), is(40L));
		}

		@Test
		void comittedNonHeapMemorySizeIsCalculated() {
			this.mockCollectorWithDefaults();

			MemoryUsage nonHeapMemoryUsage = this.memoryBean.getNonHeapMemoryUsage();
			when(nonHeapMemoryUsage.getCommitted()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getMinComittedNonHeapMemorySize(), is(9L));
			assertThat(collector.getMaxComittedNonHeapMemorySize(), is(11L));
			assertThat(collector.getTotalComittedNonHeapMemorySize(), is(40L));
		}

		@Test
		void countIsIncremented() {
			this.mockCollectorWithDefaults();

			this.cut.gather();
			this.cut.gather();

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getCount(), is(2));
		}
	}

	/**
	 * Tests the {@link MemoryInformation#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends MemoryInformationTest {

		@Test
		void getNewMemoryInformationData() throws Exception {
			MemoryInformationData collector = (MemoryInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalFreePhysMemory(4L);
			collector.setMinFreePhysMemory(5L);
			collector.setMaxFreePhysMemory(6L);

			collector.setTotalFreeSwapSpace(7L);
			collector.setMinFreeSwapSpace(8L);
			collector.setMaxFreeSwapSpace(9L);

			collector.setTotalComittedVirtualMemSize(10L);
			collector.setMinComittedVirtualMemSize(11L);
			collector.setMaxComittedVirtualMemSize(12L);

			collector.setTotalUsedHeapMemorySize(13L);
			collector.setMinUsedHeapMemorySize(14L);
			collector.setMaxUsedHeapMemorySize(15L);

			collector.setTotalComittedHeapMemorySize(16L);
			collector.setMinComittedHeapMemorySize(17L);
			collector.setMaxComittedHeapMemorySize(18L);

			collector.setTotalUsedNonHeapMemorySize(19L);
			collector.setMinUsedNonHeapMemorySize(20L);
			collector.setMaxUsedNonHeapMemorySize(21L);

			collector.setMinComittedNonHeapMemorySize(22L);
			collector.setMaxComittedNonHeapMemorySize(23L);
			collector.setTotalComittedNonHeapMemorySize(24L);

			collector.setTimeStamp(new Timestamp(25L));

			MemoryInformationData memoryInformationData = (MemoryInformationData) this.cut.get();

			assertThat(memoryInformationData.getPlatformIdent(), is(1L));
			assertThat(memoryInformationData.getSensorTypeIdent(), is(2L));
			assertThat(memoryInformationData.getCount(), is(3));

			assertThat(memoryInformationData.getTotalFreePhysMemory(), is(4L));
			assertThat(memoryInformationData.getMinFreePhysMemory(), is(5L));
			assertThat(memoryInformationData.getMaxFreePhysMemory(), is(6L));

			assertThat(memoryInformationData.getTotalFreeSwapSpace(), is(7L));
			assertThat(memoryInformationData.getMinFreeSwapSpace(), is(8L));
			assertThat(memoryInformationData.getMaxFreeSwapSpace(), is(9L));

			assertThat(memoryInformationData.getTotalComittedVirtualMemSize(), is(10L));
			assertThat(memoryInformationData.getMinComittedVirtualMemSize(), is(11L));
			assertThat(memoryInformationData.getMaxComittedVirtualMemSize(), is(12L));

			assertThat(memoryInformationData.getTotalUsedHeapMemorySize(), is(13L));
			assertThat(memoryInformationData.getMinUsedHeapMemorySize(), is(14L));
			assertThat(memoryInformationData.getMaxUsedHeapMemorySize(), is(15L));

			assertThat(memoryInformationData.getTotalComittedHeapMemorySize(), is(16L));
			assertThat(memoryInformationData.getMinComittedHeapMemorySize(), is(17L));
			assertThat(memoryInformationData.getMaxComittedHeapMemorySize(), is(18L));

			assertThat(memoryInformationData.getTotalUsedNonHeapMemorySize(), is(19L));
			assertThat(memoryInformationData.getMinUsedNonHeapMemorySize(), is(20L));
			assertThat(memoryInformationData.getMaxUsedNonHeapMemorySize(), is(21L));

			assertThat(memoryInformationData.getMinComittedNonHeapMemorySize(), is(22L));
			assertThat(memoryInformationData.getMaxComittedNonHeapMemorySize(), is(23L));
			assertThat(memoryInformationData.getTotalComittedNonHeapMemorySize(), is(24L));

			assertThat(memoryInformationData.getTimeStamp().getTime(), is(25L));
		}
	}

	/**
	 * Tests the {@link MemoryInformation#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends MemoryInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			MemoryInformationData collector = (MemoryInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalFreePhysMemory(4L);
			collector.setMinFreePhysMemory(5L);
			collector.setMaxFreePhysMemory(6L);

			collector.setTotalFreeSwapSpace(7L);
			collector.setMinFreeSwapSpace(8L);
			collector.setMaxFreeSwapSpace(9L);

			collector.setTotalComittedVirtualMemSize(10L);
			collector.setMinComittedVirtualMemSize(11L);
			collector.setMaxComittedVirtualMemSize(12L);

			collector.setTotalUsedHeapMemorySize(13L);
			collector.setMinUsedHeapMemorySize(14L);
			collector.setMaxUsedHeapMemorySize(15L);

			collector.setTotalComittedHeapMemorySize(16L);
			collector.setMinComittedHeapMemorySize(17L);
			collector.setMaxComittedHeapMemorySize(18L);

			collector.setTotalUsedNonHeapMemorySize(19L);
			collector.setMinUsedNonHeapMemorySize(20L);
			collector.setMaxUsedNonHeapMemorySize(21L);

			collector.setMinComittedNonHeapMemorySize(22L);
			collector.setMaxComittedNonHeapMemorySize(23L);
			collector.setTotalComittedNonHeapMemorySize(24L);

			collector.setTimeStamp(new Timestamp(25L));

			this.cut.reset();
			MemoryInformationData memoryInformationData = (MemoryInformationData) this.cut.get();

			assertThat(memoryInformationData.getPlatformIdent(), is(1L));
			assertThat(memoryInformationData.getSensorTypeIdent(), is(2L));
			assertThat(memoryInformationData.getCount(), is(0));

			assertThat(memoryInformationData.getTotalFreePhysMemory(), is(0L));
			assertThat(memoryInformationData.getMinFreePhysMemory(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxFreePhysMemory(), is(0L));

			assertThat(memoryInformationData.getTotalFreeSwapSpace(), is(0L));
			assertThat(memoryInformationData.getMinFreeSwapSpace(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxFreeSwapSpace(), is(0L));

			assertThat(memoryInformationData.getTotalComittedVirtualMemSize(), is(0L));
			assertThat(memoryInformationData.getMinComittedVirtualMemSize(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxComittedVirtualMemSize(), is(0L));

			assertThat(memoryInformationData.getTotalUsedHeapMemorySize(), is(0L));
			assertThat(memoryInformationData.getMinUsedHeapMemorySize(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxUsedHeapMemorySize(), is(0L));

			assertThat(memoryInformationData.getTotalComittedHeapMemorySize(), is(0L));
			assertThat(memoryInformationData.getMinComittedHeapMemorySize(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxComittedHeapMemorySize(), is(0L));

			assertThat(memoryInformationData.getTotalUsedNonHeapMemorySize(), is(0L));
			assertThat(memoryInformationData.getMinUsedNonHeapMemorySize(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxUsedNonHeapMemorySize(), is(0L));

			assertThat(memoryInformationData.getTotalComittedNonHeapMemorySize(), is(0L));
			assertThat(memoryInformationData.getMinComittedNonHeapMemorySize(), is(Long.MAX_VALUE));
			assertThat(memoryInformationData.getMaxComittedNonHeapMemorySize(), is(0L));

			assertThat(memoryInformationData.getTimeStamp().getTime(), is(not(25L)));
		}
	}

	protected void mockCollectorWithDefaults() {
		MemoryUsage heapMemoryUsage = Mockito.mock(MemoryUsage.class);
		when(heapMemoryUsage.getUsed()).thenReturn(0L);
		when(heapMemoryUsage.getCommitted()).thenReturn(0L);

		MemoryUsage nonHeapMemoryUsage = Mockito.mock(MemoryUsage.class);
		when(nonHeapMemoryUsage.getUsed()).thenReturn(0L);
		when(nonHeapMemoryUsage.getCommitted()).thenReturn(0L);

		when(this.osBean.getFreePhysicalMemorySize()).thenReturn(0L);
		when(this.osBean.getFreeSwapSpaceSize()).thenReturn(0L);
		when(this.osBean.getCommittedVirtualMemorySize()).thenReturn(0L);
		when(this.memoryBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
		when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);
	}
}