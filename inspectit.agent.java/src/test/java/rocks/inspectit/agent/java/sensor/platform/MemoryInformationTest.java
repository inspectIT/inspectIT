package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryUsage;
import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;

/**
 * Test class for {@link MemoryInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class MemoryInformationTest extends AbstractLogSupport {

	/** Class under test. */
	@InjectMocks
	MemoryInformation cut;

	/** The mocked {@link MemoryInformationData}. */
	@Mock
	MemoryInformationData collector;

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
		void gatherMax() {
			MemoryUsage memoryUsage = Mockito.mock(MemoryUsage.class);
			when(memoryUsage.getUsed()).thenReturn(50L).thenReturn(55L);
			when(memoryUsage.getCommitted()).thenReturn(75L).thenReturn(80L);

			when(this.osBean.getFreePhysicalMemorySize()).thenReturn(500L);
			when(this.osBean.getFreeSwapSpaceSize()).thenReturn(2000L);
			when(this.osBean.getCommittedVirtualMemorySize()).thenReturn(100L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.collector.getMinFreePhysMemory()).thenReturn(498L);
			when(this.collector.getMaxFreePhysMemory()).thenReturn(499L);
			when(this.collector.getMinFreeSwapSpace()).thenReturn(1998L);
			when(this.collector.getMaxFreeSwapSpace()).thenReturn(1999L);
			when(this.collector.getMinComittedVirtualMemSize()).thenReturn(98L);
			when(this.collector.getMaxComittedVirtualMemSize()).thenReturn(99L);
			when(this.collector.getMinUsedHeapMemorySize()).thenReturn(48L);
			when(this.collector.getMaxUsedHeapMemorySize()).thenReturn(49L);
			when(this.collector.getMinComittedHeapMemorySize()).thenReturn(73L);
			when(this.collector.getMaxComittedHeapMemorySize()).thenReturn(74L);
			when(this.collector.getMinUsedNonHeapMemorySize()).thenReturn(53L);
			when(this.collector.getMaxUsedNonHeapMemorySize()).thenReturn(54L);
			when(this.collector.getMinComittedNonHeapMemorySize()).thenReturn(78L);
			when(this.collector.getMaxComittedNonHeapMemorySize()).thenReturn(79L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addFreePhysMemory(500L);
			verify(this.collector).addFreeSwapSpace(2000L);
			verify(this.collector).addComittedVirtualMemSize(100L);
			verify(this.collector).addUsedHeapMemorySize(50L);
			verify(this.collector).addComittedHeapMemorySize(75L);
			verify(this.collector).addUsedNonHeapMemorySize(55L);
			verify(this.collector).addComittedNonHeapMemorySize(80L);
			verify(this.collector).setMaxFreePhysMemory(500L);
			verify(this.collector).setMaxFreeSwapSpace(2000L);
			verify(this.collector).setMaxComittedVirtualMemSize(100L);
			verify(this.collector).setMaxUsedHeapMemorySize(50L);
			verify(this.collector).setMaxComittedHeapMemorySize(75L);
			verify(this.collector).setMaxUsedNonHeapMemorySize(55L);
			verify(this.collector).setMaxComittedNonHeapMemorySize(80L);
			verify(this.collector, never()).setMinFreePhysMemory(Mockito.anyLong());
			verify(this.collector, never()).setMinFreeSwapSpace(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedVirtualMemSize(Mockito.anyLong());
			verify(this.collector, never()).setMinUsedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinUsedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedNonHeapMemorySize(Mockito.anyLong());
		}

		@Test
		void gatherMin() {
			MemoryUsage memoryUsage = Mockito.mock(MemoryUsage.class);
			when(memoryUsage.getUsed()).thenReturn(50L).thenReturn(55L);
			when(memoryUsage.getCommitted()).thenReturn(75L).thenReturn(80L);

			when(this.osBean.getFreePhysicalMemorySize()).thenReturn(500L);
			when(this.osBean.getFreeSwapSpaceSize()).thenReturn(2000L);
			when(this.osBean.getCommittedVirtualMemorySize()).thenReturn(100L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.collector.getMinFreePhysMemory()).thenReturn(501L);
			when(this.collector.getMinFreeSwapSpace()).thenReturn(2001L);
			when(this.collector.getMinComittedVirtualMemSize()).thenReturn(101L);
			when(this.collector.getMinUsedHeapMemorySize()).thenReturn(51L);
			when(this.collector.getMinComittedHeapMemorySize()).thenReturn(76L);
			when(this.collector.getMinUsedNonHeapMemorySize()).thenReturn(56L);
			when(this.collector.getMinComittedNonHeapMemorySize()).thenReturn(81L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addFreePhysMemory(500L);
			verify(this.collector).addFreeSwapSpace(2000L);
			verify(this.collector).addComittedVirtualMemSize(100L);
			verify(this.collector).addUsedHeapMemorySize(50L);
			verify(this.collector).addComittedHeapMemorySize(75L);
			verify(this.collector).addUsedNonHeapMemorySize(55L);
			verify(this.collector).addComittedNonHeapMemorySize(80L);
			verify(this.collector).setMinFreePhysMemory(500L);
			verify(this.collector).setMinFreeSwapSpace(2000L);
			verify(this.collector).setMinComittedVirtualMemSize(100L);
			verify(this.collector).setMinUsedHeapMemorySize(50L);
			verify(this.collector).setMinComittedHeapMemorySize(75L);
			verify(this.collector).setMinUsedNonHeapMemorySize(55L);
			verify(this.collector).setMinComittedNonHeapMemorySize(80L);
			verify(this.collector, never()).setMaxFreePhysMemory(Mockito.anyLong());
			verify(this.collector, never()).setMaxFreeSwapSpace(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedVirtualMemSize(Mockito.anyLong());
			verify(this.collector, never()).setMaxUsedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxUsedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).getMaxFreePhysMemory();
			verify(this.collector, never()).getMaxFreeSwapSpace();
			verify(this.collector, never()).getMaxComittedVirtualMemSize();
			verify(this.collector, never()).getMaxUsedHeapMemorySize();
			verify(this.collector, never()).getMaxComittedHeapMemorySize();
			verify(this.collector, never()).getMaxUsedNonHeapMemorySize();
			verify(this.collector, never()).getMaxComittedNonHeapMemorySize();
		}

		@Test
		void noRecentInformation() {
			MemoryUsage memoryUsage = Mockito.mock(MemoryUsage.class);
			when(memoryUsage.getUsed()).thenReturn(50L).thenReturn(55L);
			when(memoryUsage.getCommitted()).thenReturn(75L).thenReturn(80L);

			when(this.osBean.getFreePhysicalMemorySize()).thenReturn(500L);
			when(this.osBean.getFreeSwapSpaceSize()).thenReturn(2000L);
			when(this.osBean.getCommittedVirtualMemorySize()).thenReturn(100L);
			when(this.memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.memoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsage);
			when(this.collector.getMinFreePhysMemory()).thenReturn(500L);
			when(this.collector.getMaxFreePhysMemory()).thenReturn(500L);
			when(this.collector.getMinFreeSwapSpace()).thenReturn(2000L);
			when(this.collector.getMaxFreeSwapSpace()).thenReturn(2000L);
			when(this.collector.getMinComittedVirtualMemSize()).thenReturn(100L);
			when(this.collector.getMaxComittedVirtualMemSize()).thenReturn(100L);
			when(this.collector.getMinUsedHeapMemorySize()).thenReturn(50L);
			when(this.collector.getMaxUsedHeapMemorySize()).thenReturn(50L);
			when(this.collector.getMinComittedHeapMemorySize()).thenReturn(75L);
			when(this.collector.getMaxComittedHeapMemorySize()).thenReturn(75L);
			when(this.collector.getMinUsedNonHeapMemorySize()).thenReturn(55L);
			when(this.collector.getMaxUsedNonHeapMemorySize()).thenReturn(55L);
			when(this.collector.getMinComittedNonHeapMemorySize()).thenReturn(80L);
			when(this.collector.getMaxComittedNonHeapMemorySize()).thenReturn(80L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addFreePhysMemory(500L);
			verify(this.collector).addFreeSwapSpace(2000L);
			verify(this.collector).addComittedVirtualMemSize(100L);
			verify(this.collector).addUsedHeapMemorySize(50L);
			verify(this.collector).addComittedHeapMemorySize(75L);
			verify(this.collector).addUsedNonHeapMemorySize(55L);
			verify(this.collector).addComittedNonHeapMemorySize(80L);
			verify(this.collector, never()).setMinFreePhysMemory(Mockito.anyLong());
			verify(this.collector, never()).setMinFreeSwapSpace(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedVirtualMemSize(Mockito.anyLong());
			verify(this.collector, never()).setMinUsedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinUsedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMinComittedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxFreePhysMemory(Mockito.anyLong());
			verify(this.collector, never()).setMaxFreeSwapSpace(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedVirtualMemSize(Mockito.anyLong());
			verify(this.collector, never()).setMaxUsedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxUsedNonHeapMemorySize(Mockito.anyLong());
			verify(this.collector, never()).setMaxComittedNonHeapMemorySize(Mockito.anyLong());
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
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getTotalFreePhysMemory()).thenReturn(4L);
			when(this.collector.getMinFreePhysMemory()).thenReturn(5L);
			when(this.collector.getMaxFreePhysMemory()).thenReturn(6L);
			when(this.collector.getTotalFreeSwapSpace()).thenReturn(7L);
			when(this.collector.getMinFreeSwapSpace()).thenReturn(8L);
			when(this.collector.getMaxFreeSwapSpace()).thenReturn(9L);
			when(this.collector.getTotalComittedVirtualMemSize()).thenReturn(10L);
			when(this.collector.getMinComittedVirtualMemSize()).thenReturn(11L);
			when(this.collector.getMaxComittedVirtualMemSize()).thenReturn(12L);
			when(this.collector.getTotalUsedHeapMemorySize()).thenReturn(13L);
			when(this.collector.getMinUsedHeapMemorySize()).thenReturn(14L);
			when(this.collector.getMaxUsedHeapMemorySize()).thenReturn(15L);
			when(this.collector.getTotalComittedHeapMemorySize()).thenReturn(16L);
			when(this.collector.getMinComittedHeapMemorySize()).thenReturn(17L);
			when(this.collector.getMaxComittedHeapMemorySize()).thenReturn(18L);
			when(this.collector.getTotalUsedNonHeapMemorySize()).thenReturn(19L);
			when(this.collector.getMinComittedNonHeapMemorySize()).thenReturn(20L);
			when(this.collector.getMaxComittedNonHeapMemorySize()).thenReturn(21L);
			when(this.collector.getTotalComittedNonHeapMemorySize()).thenReturn(22L);

			Timestamp timestamp = mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			MemoryInformationData collector = (MemoryInformationData) this.cut.get();

			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getTotalFreePhysMemory(), is(4L));
			assertThat(collector.getMinFreePhysMemory(), is(5L));
			assertThat(collector.getMaxFreePhysMemory(), is(6L));
			assertThat(collector.getTotalFreeSwapSpace(), is(7L));
			assertThat(collector.getMinFreeSwapSpace(), is(8L));
			assertThat(collector.getMaxFreeSwapSpace(), is(9L));
			assertThat(collector.getTotalComittedVirtualMemSize(), is(10L));
			assertThat(collector.getMinComittedVirtualMemSize(), is(11L));
			assertThat(collector.getMaxComittedVirtualMemSize(), is(12L));
			assertThat(collector.getTotalUsedHeapMemorySize(), is(13L));
			assertThat(collector.getMinUsedHeapMemorySize(), is(14L));
			assertThat(collector.getMaxUsedHeapMemorySize(), is(15L));
			assertThat(collector.getTotalComittedHeapMemorySize(), is(16L));
			assertThat(collector.getMinComittedHeapMemorySize(), is(17L));
			assertThat(collector.getMaxComittedHeapMemorySize(), is(18L));
			assertThat(collector.getTotalUsedNonHeapMemorySize(), is(19L));
			assertThat(collector.getMinComittedNonHeapMemorySize(), is(20L));
			assertThat(collector.getMaxComittedNonHeapMemorySize(), is(21L));
			assertThat(collector.getTotalComittedNonHeapMemorySize(), is(22L));
			assertThat(collector.getTimeStamp(), is(timestamp));
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
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setTotalFreePhysMemory(0);
			verify(this.collector).setMinFreePhysMemory(Long.MAX_VALUE);
			verify(this.collector).setMaxFreePhysMemory(0);
			verify(this.collector).setTotalFreeSwapSpace(0);
			verify(this.collector).setMinFreeSwapSpace(Long.MAX_VALUE);
			verify(this.collector).setMaxFreeSwapSpace(0);
			verify(this.collector).setTotalComittedVirtualMemSize(0);
			verify(this.collector).setMinComittedVirtualMemSize(Long.MAX_VALUE);
			verify(this.collector).setMaxComittedVirtualMemSize(0);
			verify(this.collector).setTotalUsedHeapMemorySize(0L);
			verify(this.collector).setMinUsedHeapMemorySize(Long.MAX_VALUE);
			verify(this.collector).setMaxUsedHeapMemorySize(0);
			verify(this.collector).setTotalComittedHeapMemorySize(0L);
			verify(this.collector).setMinComittedHeapMemorySize(Long.MAX_VALUE);
			verify(this.collector).setMaxComittedHeapMemorySize(0L);
			verify(this.collector).setTotalUsedNonHeapMemorySize(0L);
			verify(this.collector).setMinComittedNonHeapMemorySize(Long.MAX_VALUE);
			verify(this.collector).setMaxComittedNonHeapMemorySize(0L);
			verify(this.collector).setTotalComittedNonHeapMemorySize(0L);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}
}