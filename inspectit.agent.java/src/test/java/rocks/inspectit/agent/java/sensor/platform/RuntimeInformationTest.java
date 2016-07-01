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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;

/**
 * Test class for {@link RuntimeInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class RuntimeInformationTest extends AbstractLogSupport {

	/** Class under test. */
	@InjectMocks
	RuntimeInformation cut;

	/** The mocked {@link RuntimeInformationData}. */
	@Mock
	RuntimeInformationData collector;

	/** The mocked {@link RuntimeInfoProvider}. */
	@Mock
	RuntimeInfoProvider runtimeBean;

	/**
	 * Tests the {@link RuntimeInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends RuntimeInformationTest {

		@Test
		void gatherMax() {
			when(this.runtimeBean.getUptime()).thenReturn(100L);
			when(this.collector.getMaxUptime()).thenReturn(50L);
			when(this.collector.getMinUptime()).thenReturn(25L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addUptime(100L);
			verify(this.collector).setMaxUptime(100L);
			verify(this.collector).getMinUptime();
			verify(this.collector).getMaxUptime();
			verify(this.collector, never()).setMinUptime(Mockito.anyLong());
		}

		@Test
		void gatherMin() {
			when(this.runtimeBean.getUptime()).thenReturn(20L);
			when(this.collector.getMaxUptime()).thenReturn(50L);
			when(this.collector.getMinUptime()).thenReturn(25L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addUptime(20L);
			verify(this.collector).setMinUptime(20L);
			verify(this.collector).getMinUptime();
			verify(this.collector, never()).getMaxUptime();
			verify(this.collector, never()).setMaxUptime(Mockito.anyLong());
		}

		@Test
		void noRecentInformation() {
			when(this.runtimeBean.getUptime()).thenReturn(100L);
			when(this.collector.getMaxUptime()).thenReturn(100L);
			when(this.collector.getMinUptime()).thenReturn(100L);

			cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addUptime(100L);
			verify(this.collector).getMinUptime();
			verify(this.collector).getMaxUptime();
			verify(this.collector, never()).setMinUptime(Mockito.anyLong());
			verify(this.collector, never()).setMaxUptime(Mockito.anyLong());
		}
	}

	/**
	 * Tests the {@link RuntimeInformationTest#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends RuntimeInformationTest {

		@Test
		void getNewRuntimeInformationData() throws Exception {
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getTotalUptime()).thenReturn(4L);
			when(this.collector.getMinUptime()).thenReturn(5L);
			when(this.collector.getMaxUptime()).thenReturn(6L);

			Timestamp timestamp = mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			RuntimeInformationData collector = (RuntimeInformationData) this.cut.get();

			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getTotalUptime(), is(4L));
			assertThat(collector.getMinUptime(), is(5L));
			assertThat(collector.getMaxUptime(), is(6L));
			assertThat(collector.getTimeStamp(), is(timestamp));
		}
	}

	/**
	 * Tests the {@link RuntimeInformationTest#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends RuntimeInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setTotalUptime(0);
			verify(this.collector).setMinUptime(Long.MAX_VALUE);
			verify(this.collector).setMaxUptime(0);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}
}
