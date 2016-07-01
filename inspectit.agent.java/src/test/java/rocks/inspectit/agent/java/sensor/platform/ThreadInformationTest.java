package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;

/**
 * Test class for {@link ThreadInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class ThreadInformationTest extends AbstractLogSupport {

	/** Class under test. */
	@InjectMocks
	ThreadInformation cut;

	/** The mocked {@link ThreadInformationData}. */
	@Mock
	ThreadInformationData collector;

	/** The mocked {@link ThreadInfoProvider}. */
	@Mock
	ThreadInfoProvider threadBean;

	/**
	 * Tests the {@link ThreadInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends ThreadInformationTest {

		@Test
		void gatherMax() {
			when(this.threadBean.getDaemonThreadCount()).thenReturn(5);
			when(this.threadBean.getPeakThreadCount()).thenReturn(10);
			when(this.threadBean.getThreadCount()).thenReturn(6);
			when(this.threadBean.getTotalStartedThreadCount()).thenReturn(3L);
			when(this.collector.getMinDaemonThreadCount()).thenReturn(3);
			when(this.collector.getMaxDaemonThreadCount()).thenReturn(4);
			when(this.collector.getMinPeakThreadCount()).thenReturn(8);
			when(this.collector.getMaxPeakThreadCount()).thenReturn(9);
			when(this.collector.getMinThreadCount()).thenReturn(4);
			when(this.collector.getMaxThreadCount()).thenReturn(5);
			when(this.collector.getMinTotalStartedThreadCount()).thenReturn(1L);
			when(this.collector.getMaxTotalStartedThreadCount()).thenReturn(2L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addDaemonThreadCount(5);
			verify(this.collector).addPeakThreadCount(10);
			verify(this.collector).addThreadCount(6);
			verify(this.collector).addTotalStartedThreadCount(3);
			verify(this.collector).setMaxDaemonThreadCount(5);
			verify(this.collector).setMaxPeakThreadCount(10);
			verify(this.collector).setMaxThreadCount(6);
			verify(this.collector).setMaxTotalStartedThreadCount(3);
			verify(this.collector, never()).setMinDaemonThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinPeakThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinTotalStartedThreadCount(Matchers.anyLong());
		}

		@Test
		void gatherMin() {
			when(this.threadBean.getDaemonThreadCount()).thenReturn(5);
			when(this.threadBean.getPeakThreadCount()).thenReturn(10);
			when(this.threadBean.getThreadCount()).thenReturn(6);
			when(this.threadBean.getTotalStartedThreadCount()).thenReturn(3L);
			when(this.collector.getMinDaemonThreadCount()).thenReturn(6);
			when(this.collector.getMinPeakThreadCount()).thenReturn(11);
			when(this.collector.getMinThreadCount()).thenReturn(7);
			when(this.collector.getMinTotalStartedThreadCount()).thenReturn(4L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addDaemonThreadCount(5);
			verify(this.collector).addPeakThreadCount(10);
			verify(this.collector).addThreadCount(6);
			verify(this.collector).addTotalStartedThreadCount(3);
			verify(this.collector).setMinDaemonThreadCount(5);
			verify(this.collector).setMinPeakThreadCount(10);
			verify(this.collector).setMinThreadCount(6);
			verify(this.collector).setMinTotalStartedThreadCount(3);
			verify(this.collector, never()).setMaxDaemonThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxPeakThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxTotalStartedThreadCount(Matchers.anyLong());
		}

		@Test
		void noRecentInformation() {
			when(this.threadBean.getDaemonThreadCount()).thenReturn(5);
			when(this.threadBean.getPeakThreadCount()).thenReturn(10);
			when(this.threadBean.getThreadCount()).thenReturn(6);
			when(this.threadBean.getTotalStartedThreadCount()).thenReturn(3L);
			when(this.collector.getMinDaemonThreadCount()).thenReturn(4);
			when(this.collector.getMaxDaemonThreadCount()).thenReturn(6);
			when(this.collector.getMinPeakThreadCount()).thenReturn(9);
			when(this.collector.getMaxPeakThreadCount()).thenReturn(11);
			when(this.collector.getMinThreadCount()).thenReturn(5);
			when(this.collector.getMaxThreadCount()).thenReturn(7);
			when(this.collector.getMinTotalStartedThreadCount()).thenReturn(2L);
			when(this.collector.getMaxTotalStartedThreadCount()).thenReturn(4L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addDaemonThreadCount(5);
			verify(this.collector).addPeakThreadCount(10);
			verify(this.collector).addThreadCount(6);
			verify(this.collector).addTotalStartedThreadCount(3);
			verify(this.collector, never()).setMaxDaemonThreadCount(5);
			verify(this.collector, never()).setMaxPeakThreadCount(10);
			verify(this.collector, never()).setMaxThreadCount(6);
			verify(this.collector, never()).setMaxTotalStartedThreadCount(3);
			verify(this.collector, never()).setMinDaemonThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinPeakThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMinTotalStartedThreadCount(Matchers.anyLong());
			verify(this.collector, never()).setMaxDaemonThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxPeakThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxThreadCount(Matchers.anyInt());
			verify(this.collector, never()).setMaxTotalStartedThreadCount(Matchers.anyLong());
		}
	}

	/**
	 * Tests the {@link ThreadInformationTest#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends ThreadInformationTest {

		@Test
		void getNewThreadInformationTestData() throws Exception {
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getTotalDaemonThreadCount()).thenReturn(4);
			when(this.collector.getMinDaemonThreadCount()).thenReturn(5);
			when(this.collector.getMaxDaemonThreadCount()).thenReturn(6);
			when(this.collector.getTotalPeakThreadCount()).thenReturn(7);
			when(this.collector.getMinPeakThreadCount()).thenReturn(8);
			when(this.collector.getMaxPeakThreadCount()).thenReturn(9);
			when(this.collector.getTotalThreadCount()).thenReturn(10);
			when(this.collector.getMinThreadCount()).thenReturn(11);
			when(this.collector.getMaxThreadCount()).thenReturn(12);
			when(this.collector.getTotalTotalStartedThreadCount()).thenReturn(13L);
			when(this.collector.getMinTotalStartedThreadCount()).thenReturn(14L);
			when(this.collector.getMaxTotalStartedThreadCount()).thenReturn(15L);

			Timestamp timestamp = mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getTotalDaemonThreadCount(), is(4));
			assertThat(collector.getMinDaemonThreadCount(), is(5));
			assertThat(collector.getMaxDaemonThreadCount(), is(6));
			assertThat(collector.getTotalPeakThreadCount(), is(7));
			assertThat(collector.getMinPeakThreadCount(), is(8));
			assertThat(collector.getMaxPeakThreadCount(), is(9));
			assertThat(collector.getTotalThreadCount(), is(10));
			assertThat(collector.getMinThreadCount(), is(11));
			assertThat(collector.getMaxThreadCount(), is(12));
			assertThat(collector.getTotalTotalStartedThreadCount(), is(13L));
			assertThat(collector.getMinTotalStartedThreadCount(), is(14L));
			assertThat(collector.getMaxTotalStartedThreadCount(), is(15L));
			assertThat(collector.getTimeStamp(), is(timestamp));
		}
	}

	/**
	 * Tests the {@link ThreadInformation#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends ThreadInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setTotalDaemonThreadCount(0);
			verify(this.collector).setMinDaemonThreadCount(Integer.MAX_VALUE);
			verify(this.collector).setMaxDaemonThreadCount(0);
			verify(this.collector).setTotalPeakThreadCount(0);
			verify(this.collector).setMinPeakThreadCount(Integer.MAX_VALUE);
			verify(this.collector).setMaxPeakThreadCount(0);
			verify(this.collector).setTotalThreadCount(0);
			verify(this.collector).setMinThreadCount(Integer.MAX_VALUE);
			verify(this.collector).setMaxThreadCount(0);
			verify(this.collector).setTotalTotalStartedThreadCount(0L);
			verify(this.collector).setMinTotalStartedThreadCount(Integer.MAX_VALUE);
			verify(this.collector).setMaxTotalStartedThreadCount(0);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}
}