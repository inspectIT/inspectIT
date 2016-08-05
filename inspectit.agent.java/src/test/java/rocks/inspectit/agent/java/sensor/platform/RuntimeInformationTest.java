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
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link RuntimeInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class RuntimeInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	RuntimeInformation cut;

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
		void lastUptimeIsSet() {
			when(this.runtimeBean.getUptime()).thenReturn(10L).thenReturn(9L);

			this.cut.gather();
			this.cut.gather();

			RuntimeInformationData collector = (RuntimeInformationData) this.cut.get();

			assertThat(collector.getUptime(), is(9L));
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
			RuntimeInformationData collector = (RuntimeInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);

			collector.setUptime(4L);

			collector.setTimeStamp(new Timestamp(5L));

			RuntimeInformationData runtimeInformationData = (RuntimeInformationData) this.cut.get();

			assertThat(runtimeInformationData.getPlatformIdent(), is(1L));
			assertThat(runtimeInformationData.getSensorTypeIdent(), is(2L));

			assertThat(runtimeInformationData.getUptime(), is(4L));

			assertThat(runtimeInformationData.getTimeStamp().getTime(), is(5L));
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
			RuntimeInformationData collector = (RuntimeInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);

			collector.setUptime(4L);

			collector.setTimeStamp(new Timestamp(5L));

			this.cut.reset();
			RuntimeInformationData runtimeInformationData = (RuntimeInformationData) this.cut.get();

			assertThat(runtimeInformationData.getPlatformIdent(), is(1L));
			assertThat(runtimeInformationData.getSensorTypeIdent(), is(2L));

			assertThat(runtimeInformationData.getUptime(), is(0L));

			assertThat(runtimeInformationData.getTimeStamp().getTime(), is(not(5L)));
		}
	}
}
