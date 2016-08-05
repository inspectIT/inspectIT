package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
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
		void totalCompilationTimeCalculated() {
			when(this.runtimeBean.getTotalCompilationTime()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			CompilationInformationData collector = (CompilationInformationData) this.cut.get();

			assertThat(collector.getMinTotalCompilationTime(), is(9L));
			assertThat(collector.getMaxTotalCompilationTime(), is(11L));
			assertThat(collector.getTotalTotalCompilationTime(), is(40L));
		}

		@Test
		void countIsIncremented() {
			this.cut.gather();
			this.cut.gather();

			CompilationInformationData collector = (CompilationInformationData) this.cut.get();

			assertThat(collector.getCount(), is(2));
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
			CompilationInformationData collector = (CompilationInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalTotalCompilationTime(4L);
			collector.setMaxTotalCompilationTime(5L);
			collector.setMinTotalCompilationTime(6L);

			collector.setTimeStamp(new Timestamp(7L));

			CompilationInformationData compilationInformationData = (CompilationInformationData) this.cut.get();

			assertThat(compilationInformationData.getPlatformIdent(), is(1L));
			assertThat(compilationInformationData.getSensorTypeIdent(), is(2L));
			assertThat(compilationInformationData.getCount(), is(3));

			assertThat(compilationInformationData.getTotalTotalCompilationTime(), is(4L));
			assertThat(compilationInformationData.getMaxTotalCompilationTime(), is(5L));
			assertThat(compilationInformationData.getMinTotalCompilationTime(), is(6L));

			assertThat(compilationInformationData.getTimeStamp().getTime(), is(7L));
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
			CompilationInformationData collector = (CompilationInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalTotalCompilationTime(4L);
			collector.setMaxTotalCompilationTime(5L);
			collector.setMinTotalCompilationTime(6L);

			collector.setTimeStamp(new Timestamp(7L));

			this.cut.reset();
			CompilationInformationData compilationInformationData = (CompilationInformationData) this.cut.get();

			assertThat(compilationInformationData.getPlatformIdent(), is(1L));
			assertThat(compilationInformationData.getSensorTypeIdent(), is(2L));
			assertThat(compilationInformationData.getCount(), is(0));

			assertThat(compilationInformationData.getTotalTotalCompilationTime(), is(0L));
			assertThat(compilationInformationData.getMinTotalCompilationTime(), is(Long.MAX_VALUE));
			assertThat(compilationInformationData.getMaxTotalCompilationTime(), is(0L));

			assertThat(compilationInformationData.getTimeStamp().getTime(), is(not(7L)));
		}
	}
}
