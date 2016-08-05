package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link CpuInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class CpuInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	CpuInformation cut;

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
		void cpuUsageIsSetForMinAndMaxOnTheFirstRun() {
			when(this.osBean.retrieveCpuUsage()).thenReturn(10f);

			this.cut.gather();

			CpuInformationData collector = (CpuInformationData) this.cut.get();

			assertThat(collector.getMinCpuUsage(), is(10f));
			assertThat(collector.getMaxCpuUsage(), is(10f));
			assertThat(collector.getTotalCpuUsage(), is(10f));
		}

		@Test
		void cpuUsageIsCalculated() {
			when(this.osBean.retrieveCpuUsage()).thenReturn(10f).thenReturn(9f).thenReturn(11f).thenReturn(10f);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			CpuInformationData collector = (CpuInformationData) this.cut.get();

			assertThat(collector.getMinCpuUsage(), is(9f));
			assertThat(collector.getMaxCpuUsage(), is(11f));
			assertThat(collector.getTotalCpuUsage(), is(40f));
		}

		@Test
		void updateProcessCpuTime() {
			when(this.osBean.getProcessCpuTime()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			CpuInformationData collector = (CpuInformationData) this.cut.get();

			assertThat(collector.getProcessCpuTime(), is(11L));
		}

		@Test
		void countIsIncremented() {
			this.cut.gather();
			this.cut.gather();

			CpuInformationData collector = (CpuInformationData) this.cut.get();

			assertThat(collector.getCount(), is(2));
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
			CpuInformationData collector = (CpuInformationData) this.cut.getSystemSensorData();

			collector.setCount(1);
			collector.setPlatformIdent(2L);
			collector.setSensorTypeIdent(3L);

			collector.setProcessCpuTime(4L);

			collector.setTotalCpuUsage(5f);
			collector.setMaxCpuUsage(6f);
			collector.setMinCpuUsage(7f);

			collector.setTimeStamp(new Timestamp(8L));

			CpuInformationData cpuInformationData = (CpuInformationData) this.cut.get();

			assertThat(cpuInformationData.getCount(), is(1));
			assertThat(cpuInformationData.getPlatformIdent(), is(2L));
			assertThat(cpuInformationData.getSensorTypeIdent(), is(3L));

			assertThat(cpuInformationData.getProcessCpuTime(), is(4L));

			assertThat(cpuInformationData.getTotalCpuUsage(), is(5f));
			assertThat(cpuInformationData.getMaxCpuUsage(), is(6f));
			assertThat(cpuInformationData.getMinCpuUsage(), is(7f));

			assertThat(cpuInformationData.getTimeStamp().getTime(), is(8L));
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
			CpuInformationData collector = (CpuInformationData) this.cut.getSystemSensorData();

			collector.setCount(1);
			collector.setPlatformIdent(2L);
			collector.setSensorTypeIdent(3L);

			collector.setProcessCpuTime(4L);

			collector.setTotalCpuUsage(5f);
			collector.setMaxCpuUsage(6f);
			collector.setMinCpuUsage(7f);

			collector.setTimeStamp(new Timestamp(8L));

			this.cut.reset();
			CpuInformationData cpuInformationData = (CpuInformationData) this.cut.get();

			assertThat(cpuInformationData.getCount(), is(0));
			assertThat(cpuInformationData.getPlatformIdent(), is(2L));
			assertThat(cpuInformationData.getSensorTypeIdent(), is(3L));

			assertThat(cpuInformationData.getProcessCpuTime(), is(0L));

			assertThat(cpuInformationData.getTotalCpuUsage(), is(0f));
			assertThat(cpuInformationData.getMaxCpuUsage(), is(0f));
			assertThat(cpuInformationData.getMinCpuUsage(), is(Float.MAX_VALUE));

			assertThat(cpuInformationData.getTimeStamp().getTime(), is(not(8L)));
		}
	}
}
