package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link ThreadInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class ThreadInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	ThreadInformation cut;

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
		void daemonThreadCountIsCalculated() {
			when(this.threadBean.getDaemonThreadCount()).thenReturn(10).thenReturn(9).thenReturn(11).thenReturn(10);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getMinDaemonThreadCount(), is(9));
			assertThat(collector.getMaxDaemonThreadCount(), is(11));
			assertThat(collector.getTotalDaemonThreadCount(), is(40));
		}

		@Test
		void peakThreadCountIsCalculated() {
			when(this.threadBean.getPeakThreadCount()).thenReturn(10).thenReturn(9).thenReturn(11).thenReturn(10);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getMinPeakThreadCount(), is(9));
			assertThat(collector.getMaxPeakThreadCount(), is(11));
			assertThat(collector.getTotalPeakThreadCount(), is(40));
		}

		@Test
		void threadCountIsCalculated() {
			when(this.threadBean.getThreadCount()).thenReturn(10).thenReturn(9).thenReturn(11).thenReturn(10);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getMinThreadCount(), is(9));
			assertThat(collector.getMaxThreadCount(), is(11));
			assertThat(collector.getTotalThreadCount(), is(40));
		}

		@Test
		void totalStartedThreadCountIsCalculated() {
			when(this.threadBean.getTotalStartedThreadCount()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getMinTotalStartedThreadCount(), is(9L));
			assertThat(collector.getMaxTotalStartedThreadCount(), is(11L));
			assertThat(collector.getTotalTotalStartedThreadCount(), is(40L));
		}

		@Test
		void countIsIncremented() {
			this.cut.gather();
			this.cut.gather();

			ThreadInformationData collector = (ThreadInformationData) this.cut.get();

			assertThat(collector.getCount(), is(2));
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
			ThreadInformationData collector = (ThreadInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalDaemonThreadCount(4);
			collector.setMinDaemonThreadCount(5);
			collector.setMaxDaemonThreadCount(6);

			collector.setTotalPeakThreadCount(7);
			collector.setMinPeakThreadCount(8);
			collector.setMaxPeakThreadCount(9);

			collector.setTotalThreadCount(10);
			collector.setMinThreadCount(11);
			collector.setMaxThreadCount(12);

			collector.setTotalTotalStartedThreadCount(13L);
			collector.setMinTotalStartedThreadCount(14L);
			collector.setMaxTotalStartedThreadCount(15L);

			collector.setTimeStamp(new Timestamp(16L));

			ThreadInformationData threadInformationData = (ThreadInformationData) this.cut.get();

			assertThat(threadInformationData.getPlatformIdent(), is(1L));
			assertThat(threadInformationData.getSensorTypeIdent(), is(2L));
			assertThat(threadInformationData.getCount(), is(3));

			assertThat(threadInformationData.getTotalDaemonThreadCount(), is(4));
			assertThat(threadInformationData.getMinDaemonThreadCount(), is(5));
			assertThat(threadInformationData.getMaxDaemonThreadCount(), is(6));

			assertThat(threadInformationData.getTotalPeakThreadCount(), is(7));
			assertThat(threadInformationData.getMinPeakThreadCount(), is(8));
			assertThat(threadInformationData.getMaxPeakThreadCount(), is(9));

			assertThat(threadInformationData.getTotalThreadCount(), is(10));
			assertThat(threadInformationData.getMinThreadCount(), is(11));
			assertThat(threadInformationData.getMaxThreadCount(), is(12));

			assertThat(threadInformationData.getTotalTotalStartedThreadCount(), is(13L));
			assertThat(threadInformationData.getMinTotalStartedThreadCount(), is(14L));
			assertThat(threadInformationData.getMaxTotalStartedThreadCount(), is(15L));

			assertThat(threadInformationData.getTimeStamp().getTime(), is(16L));
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
			ThreadInformationData collector = (ThreadInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalDaemonThreadCount(4);
			collector.setMinDaemonThreadCount(5);
			collector.setMaxDaemonThreadCount(6);

			collector.setTotalPeakThreadCount(7);
			collector.setMinPeakThreadCount(8);
			collector.setMaxPeakThreadCount(9);

			collector.setTotalThreadCount(10);
			collector.setMinThreadCount(11);
			collector.setMaxThreadCount(12);

			collector.setTotalTotalStartedThreadCount(13L);
			collector.setMinTotalStartedThreadCount(14L);
			collector.setMaxTotalStartedThreadCount(15L);

			collector.setTimeStamp(new Timestamp(16L));

			this.cut.reset();
			ThreadInformationData threadInformationData = (ThreadInformationData) this.cut.get();

			assertThat(threadInformationData.getPlatformIdent(), is(1L));
			assertThat(threadInformationData.getSensorTypeIdent(), is(2L));
			assertThat(threadInformationData.getCount(), is(0));

			assertThat(threadInformationData.getTotalDaemonThreadCount(), is(0));
			assertThat(threadInformationData.getMinDaemonThreadCount(), is(Integer.MAX_VALUE));
			assertThat(threadInformationData.getMaxDaemonThreadCount(), is(0));

			assertThat(threadInformationData.getTotalPeakThreadCount(), is(0));
			assertThat(threadInformationData.getMinPeakThreadCount(), is(Integer.MAX_VALUE));
			assertThat(threadInformationData.getMaxPeakThreadCount(), is(0));

			assertThat(threadInformationData.getTotalThreadCount(), is(0));
			assertThat(threadInformationData.getMinThreadCount(), is(Integer.MAX_VALUE));
			assertThat(threadInformationData.getMaxThreadCount(), is(0));

			assertThat(threadInformationData.getTotalTotalStartedThreadCount(), is(0L));
			assertThat(threadInformationData.getMinTotalStartedThreadCount(), is(Long.MAX_VALUE));
			assertThat(threadInformationData.getMaxTotalStartedThreadCount(), is(0L));

			assertThat(threadInformationData.getTimeStamp().getTime(), is(not(16L)));
		}
	}
}