package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link ClassLoadingInformation}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
public class ClassLoadingInformationTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	ClassLoadingInformation cut;

	/** The mocked {@link RuntimeInfoProvider}. */
	@Mock
	RuntimeInfoProvider runtimeBean;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	PlatformSensorTypeConfig sensorTypeConfig;

	@Mock
	IPlatformManager platformManager;

	/**
	 * Tests the {@link ClassLoadingSensor#gather()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Gather extends ClassLoadingInformationTest {

		@Test
		void loadedClassCountIsCalculated() {
			when(this.runtimeBean.getLoadedClassCount()).thenReturn(10).thenReturn(9).thenReturn(11).thenReturn(10);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.get();

			assertThat(collector.getMinLoadedClassCount(), is(9));
			assertThat(collector.getMaxLoadedClassCount(), is(11));
			assertThat(collector.getTotalLoadedClassCount(), is(40));
		}

		@Test
		void totalLoadedClassCountIsCalculated() {
			when(this.runtimeBean.getTotalLoadedClassCount()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.get();

			assertThat(collector.getMinTotalLoadedClassCount(), is(9L));
			assertThat(collector.getMaxTotalLoadedClassCount(), is(11L));
			assertThat(collector.getTotalTotalLoadedClassCount(), is(40L));
		}

		@Test
		void unloadedClassCountIsCalculated() {
			when(this.runtimeBean.getUnloadedClassCount()).thenReturn(10L).thenReturn(9L).thenReturn(11L).thenReturn(10L);

			this.cut.gather();
			this.cut.gather();
			this.cut.gather();
			this.cut.gather();

			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.get();

			assertThat(collector.getMinUnloadedClassCount(), is(9L));
			assertThat(collector.getMaxUnloadedClassCount(), is(11L));
			assertThat(collector.getTotalUnloadedClassCount(), is(40L));
		}

		@Test
		void countIsIncremented() {
			this.cut.gather();
			this.cut.gather();

			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.get();

			assertThat(collector.getCount(), is(2));
		}
	}

	/**
	 * Tests the {@link ClassLoadingInformation#get()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Get extends ClassLoadingInformationTest {

		@Test
		void getNewClassLoadingInformationData() throws Exception {
			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalLoadedClassCount(4);
			collector.setMinLoadedClassCount(5);
			collector.setMaxLoadedClassCount(6);

			collector.setTotalTotalLoadedClassCount(7L);
			collector.setMinTotalLoadedClassCount(8L);
			collector.setMaxTotalLoadedClassCount(9L);

			collector.setTotalUnloadedClassCount(10L);
			collector.setMinUnloadedClassCount(11L);
			collector.setMaxUnloadedClassCount(12L);

			collector.setTimeStamp(new Timestamp(13L));

			ClassLoadingInformationData classLoadingInformationData = (ClassLoadingInformationData) this.cut.get();

			assertThat(classLoadingInformationData.getPlatformIdent(), is(1L));
			assertThat(classLoadingInformationData.getSensorTypeIdent(), is(2L));
			assertThat(classLoadingInformationData.getCount(), is(3));

			assertThat(classLoadingInformationData.getTotalLoadedClassCount(), is(4));
			assertThat(classLoadingInformationData.getMinLoadedClassCount(), is(5));
			assertThat(classLoadingInformationData.getMaxLoadedClassCount(), is(6));

			assertThat(classLoadingInformationData.getTotalTotalLoadedClassCount(), is(7L));
			assertThat(classLoadingInformationData.getMinTotalLoadedClassCount(), is(8L));
			assertThat(classLoadingInformationData.getMaxTotalLoadedClassCount(), is(9L));

			assertThat(classLoadingInformationData.getTotalUnloadedClassCount(), is(10L));
			assertThat(classLoadingInformationData.getMinUnloadedClassCount(), is(11L));
			assertThat(classLoadingInformationData.getMaxUnloadedClassCount(), is(12L));

			assertThat(classLoadingInformationData.getTimeStamp().getTime(), is(13L));
		}
	}

	/**
	 * Tests the {@link ClassLoadingInformation#reset()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class Reset extends ClassLoadingInformationTest {

		@Test
		void collectorClassIsResetted() throws Exception {
			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.getSystemSensorData();

			collector.setPlatformIdent(1L);
			collector.setSensorTypeIdent(2L);
			collector.setCount(3);

			collector.setTotalLoadedClassCount(4);
			collector.setMinLoadedClassCount(5);
			collector.setMaxLoadedClassCount(6);

			collector.setTotalTotalLoadedClassCount(7L);
			collector.setMinTotalLoadedClassCount(8L);
			collector.setMaxTotalLoadedClassCount(9L);

			collector.setTotalUnloadedClassCount(10L);
			collector.setMinUnloadedClassCount(11L);
			collector.setMaxUnloadedClassCount(12L);

			collector.setTimeStamp(new Timestamp(13L));

			this.cut.reset();
			ClassLoadingInformationData classLoadingInformationData = (ClassLoadingInformationData) this.cut.get();

			assertThat(classLoadingInformationData.getPlatformIdent(), is(1L));
			assertThat(classLoadingInformationData.getSensorTypeIdent(), is(2L));
			assertThat(classLoadingInformationData.getCount(), is(0));

			assertThat(classLoadingInformationData.getTotalLoadedClassCount(), is(0));
			assertThat(classLoadingInformationData.getMinLoadedClassCount(), is(Integer.MAX_VALUE));
			assertThat(classLoadingInformationData.getMaxLoadedClassCount(), is(0));

			assertThat(classLoadingInformationData.getTotalTotalLoadedClassCount(), is(0L));
			assertThat(classLoadingInformationData.getMinTotalLoadedClassCount(), is(Long.MAX_VALUE));
			assertThat(classLoadingInformationData.getMaxTotalLoadedClassCount(), is(0L));

			assertThat(classLoadingInformationData.getTotalUnloadedClassCount(), is(0L));
			assertThat(classLoadingInformationData.getMinUnloadedClassCount(), is(Long.MAX_VALUE));
			assertThat(classLoadingInformationData.getMaxUnloadedClassCount(), is(0L));

			assertThat(classLoadingInformationData.getTimeStamp().getTime(), is(not(13L)));
		}
	}

}