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

import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link CompilationSensorTest}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class CompilationInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	CompilationInformation cut;

	/** The mocked {@link CompilationInformationData}. */
	@Mock
	CompilationInformationData collector;

	/** The mocked {@link RuntimeInfoProvider}. */
	@Mock
	RuntimeInfoProvider runtimeBean;

	/**
	 * Tests the {@link CompilationInformation#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends CompilationInformationTest {

		@Test
		void gatherMax() {
			when(this.runtimeBean.getTotalCompilationTime()).thenReturn(10L);
			when(this.collector.getMinTotalCompilationTime()).thenReturn(9L);
			when(this.collector.getMaxTotalCompilationTime()).thenReturn(9L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addTotalCompilationTime(10);
			verify(this.collector).setMaxTotalCompilationTime(10);
			verify(this.collector, never()).setMinTotalCompilationTime(Mockito.anyLong());
		}

		@Test
		void gatherMin() {
			when(this.runtimeBean.getTotalCompilationTime()).thenReturn(10L);
			when(this.collector.getMinTotalCompilationTime()).thenReturn(11L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addTotalCompilationTime(10);
			verify(this.collector).setMinTotalCompilationTime(10);
			verify(this.collector, never()).setMaxTotalCompilationTime(Mockito.anyLong());
			verify(this.collector, never()).getMaxTotalCompilationTime();
		}

		@Test
		void noRecentInformation() {
			when(this.runtimeBean.getTotalCompilationTime()).thenReturn(10L);
			when(this.collector.getMinTotalCompilationTime()).thenReturn(9L);
			when(this.collector.getMaxTotalCompilationTime()).thenReturn(10L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addTotalCompilationTime(10);
			verify(this.collector, never()).setMinTotalCompilationTime(Mockito.anyLong());
			verify(this.collector, never()).setMaxTotalCompilationTime(Mockito.anyLong());
		}
	}

	/**
	 * Tests the {@link CompilationInformation#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends CompilationInformationTest {

		@Test
		void getNewCompilationInformationData() throws Exception {
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getTotalTotalCompilationTime()).thenReturn(4L);
			when(this.collector.getMaxTotalCompilationTime()).thenReturn(5L);
			when(this.collector.getMinTotalCompilationTime()).thenReturn(6L);

			Timestamp timestamp = mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			CompilationInformationData collector = (CompilationInformationData) this.cut.get();

			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getTotalTotalCompilationTime(), is(4L));
			assertThat(collector.getMaxTotalCompilationTime(), is(5L));
			assertThat(collector.getMinTotalCompilationTime(), is(6L));
			assertThat(collector.getTimeStamp(), is(timestamp));
		}
	}

	/**
	 * Tests the {@link CompilationInformationTest#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends CompilationInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setTotalTotalCompilationTime(0L);
			verify(this.collector).setMinTotalCompilationTime(Long.MAX_VALUE);
			verify(this.collector).setMaxTotalCompilationTime(0L);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}
}
