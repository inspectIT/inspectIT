package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;

/**
 * Test class for {@link CpuInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class CpuInformationTest extends AbstractLogSupport {

	/** Class under test. */
	@InjectMocks
	CpuInformation cut;

	/** The mocked {@link CpuInformationData}. */
	@Mock
	CpuInformationData collector;

	/** The mocked {@link OperatingSystemInfoProvider}. */
	@Mock
	OperatingSystemInfoProvider osBean;

	/**
	 * Tests the {@link CpuInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends CpuInformationTest {

		@Test
		void gatherMax() {
			when(this.osBean.retrieveCpuUsage()).thenReturn(50f);
			when(this.osBean.getProcessCpuTime()).thenReturn(75L);
			when(this.collector.getMinCpuUsage()).thenReturn(40f);
			when(this.collector.getMaxCpuUsage()).thenReturn(45f);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).updateProcessCpuTime(75L);
			verify(this.collector).addCpuUsage(50f);
			verify(this.collector, never()).setMinCpuUsage(Mockito.anyFloat());
			verify(this.collector).setMaxCpuUsage(50f);
		}

		@Test
		void gatherMin() {
			when(this.osBean.retrieveCpuUsage()).thenReturn(50f);
			when(this.osBean.getProcessCpuTime()).thenReturn(75L);
			when(this.collector.getMinCpuUsage()).thenReturn(51f);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).updateProcessCpuTime(75L);
			verify(this.collector).addCpuUsage(50f);
			verify(this.collector).setMinCpuUsage(50f);
			verify(this.collector, never()).setMaxCpuUsage(Mockito.anyFloat());
		}

		@Test
		void noRecentInformation() {
			when(this.osBean.retrieveCpuUsage()).thenReturn(50f);
			when(this.osBean.getProcessCpuTime()).thenReturn(75L);
			when(this.collector.getMinCpuUsage()).thenReturn(40f);
			when(this.collector.getMaxCpuUsage()).thenReturn(60f);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).updateProcessCpuTime(75L);
			verify(this.collector).addCpuUsage(50f);
			verify(this.collector, never()).setMinCpuUsage(Mockito.anyFloat());
			verify(this.collector, never()).setMaxCpuUsage(Mockito.anyFloat());
		}
	}

	/**
	 * Tests the {@link CpuInformationData#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends CpuInformationTest {

		@Test
		void getNewCpuInformationData() throws Exception {
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getProcessCpuTime()).thenReturn(4L);
			when(this.collector.getMinCpuUsage()).thenReturn(5.0f);
			when(this.collector.getMaxCpuUsage()).thenReturn(6.0f);

			Timestamp timestamp = Mockito.mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			CpuInformationData collector = (CpuInformationData) this.cut.get();

			assertThat(collector, not(sameInstance(this.collector)));
			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getProcessCpuTime(), is(4L));
			assertThat(collector.getMinCpuUsage(), is(5.0f));
			assertThat(collector.getMaxCpuUsage(), is(6.0f));
			assertThat(collector.getTimeStamp(), is(timestamp));
		}
	}

	/**
	 * Tests the {@link CpuInformationData#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends CpuInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setProcessCpuTime(0L);
			verify(this.collector).setMinCpuUsage(Float.MAX_VALUE);
			verify(this.collector).setMaxCpuUsage(0.0f);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}
}
