package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
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

	/** The mocked {@link ClassLoadingInformationData}. */
	@Mock
	ClassLoadingInformationData collector;

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
		void gatherMax() {
			when(this.runtimeBean.getLoadedClassCount()).thenReturn(5);
			when(this.runtimeBean.getTotalLoadedClassCount()).thenReturn(13L);
			when(this.runtimeBean.getUnloadedClassCount()).thenReturn(8L);
			when(this.collector.getMinLoadedClassCount()).thenReturn(1);
			when(this.collector.getMaxLoadedClassCount()).thenReturn(3);
			when(this.collector.getMinTotalLoadedClassCount()).thenReturn(9L);
			when(this.collector.getMaxTotalLoadedClassCount()).thenReturn(11L);
			when(this.collector.getMinUnloadedClassCount()).thenReturn(4L);
			when(this.collector.getMaxUnloadedClassCount()).thenReturn(6L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addLoadedClassCount(5);
			verify(this.collector).addTotalLoadedClassCount(13);
			verify(this.collector).addUnloadedClassCount(8);
			verify(this.collector).setMaxLoadedClassCount(5);
			verify(this.collector).setMaxTotalLoadedClassCount(13);
			verify(this.collector).setMaxUnloadedClassCount(8);
			verify(this.collector, never()).setMinLoadedClassCount(Mockito.anyInt());
			verify(this.collector, never()).setMinTotalLoadedClassCount(Mockito.anyLong());
			verify(this.collector, never()).setMinUnloadedClassCount(Mockito.anyLong());
		}

		@Test
		void gatherMin() {
			when(this.runtimeBean.getLoadedClassCount()).thenReturn(5);
			when(this.runtimeBean.getTotalLoadedClassCount()).thenReturn(13L);
			when(this.runtimeBean.getUnloadedClassCount()).thenReturn(8L);
			when(this.collector.getMinLoadedClassCount()).thenReturn(6);
			when(this.collector.getMaxLoadedClassCount()).thenReturn(7);
			when(this.collector.getMinTotalLoadedClassCount()).thenReturn(14L);
			when(this.collector.getMaxTotalLoadedClassCount()).thenReturn(15L);
			when(this.collector.getMinUnloadedClassCount()).thenReturn(9L);
			when(this.collector.getMaxUnloadedClassCount()).thenReturn(10L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addLoadedClassCount(5);
			verify(this.collector).addTotalLoadedClassCount(13);
			verify(this.collector).addUnloadedClassCount(8);
			verify(this.collector).setMinLoadedClassCount(5);
			verify(this.collector).setMinTotalLoadedClassCount(13);
			verify(this.collector).setMinUnloadedClassCount(8);
			verify(this.collector, never()).setMaxLoadedClassCount(Mockito.anyInt());
			verify(this.collector, never()).setMaxTotalLoadedClassCount(Mockito.anyLong());
			verify(this.collector, never()).setMaxUnloadedClassCount(Mockito.anyLong());
		}

		@Test
		void noRecentInformation() {
			when(this.runtimeBean.getLoadedClassCount()).thenReturn(3);
			when(this.runtimeBean.getTotalLoadedClassCount()).thenReturn(11L);
			when(this.runtimeBean.getUnloadedClassCount()).thenReturn(6L);
			when(this.collector.getMinLoadedClassCount()).thenReturn(1);
			when(this.collector.getMaxLoadedClassCount()).thenReturn(3);
			when(this.collector.getMinTotalLoadedClassCount()).thenReturn(9L);
			when(this.collector.getMaxTotalLoadedClassCount()).thenReturn(11L);
			when(this.collector.getMinUnloadedClassCount()).thenReturn(4L);
			when(this.collector.getMaxUnloadedClassCount()).thenReturn(6L);

			this.cut.gather();

			verify(this.collector).incrementCount();
			verify(this.collector).addLoadedClassCount(3);
			verify(this.collector).addTotalLoadedClassCount(11);
			verify(this.collector).addUnloadedClassCount(6);
			verify(this.collector, never()).setMinLoadedClassCount(Mockito.anyInt());
			verify(this.collector, never()).setMinTotalLoadedClassCount(Mockito.anyLong());
			verify(this.collector, never()).setMinUnloadedClassCount(Mockito.anyLong());
			verify(this.collector, never()).setMaxLoadedClassCount(Mockito.anyInt());
			verify(this.collector, never()).setMaxTotalLoadedClassCount(Mockito.anyLong());
			verify(this.collector, never()).setMaxUnloadedClassCount(Mockito.anyLong());
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
			when(this.collector.getPlatformIdent()).thenReturn(1L);
			when(this.collector.getSensorTypeIdent()).thenReturn(2L);
			when(this.collector.getCount()).thenReturn(3);
			when(this.collector.getTotalLoadedClassCount()).thenReturn(4);
			when(this.collector.getMinLoadedClassCount()).thenReturn(5);
			when(this.collector.getMaxLoadedClassCount()).thenReturn(6);
			when(this.collector.getTotalTotalLoadedClassCount()).thenReturn(7L);
			when(this.collector.getMinTotalLoadedClassCount()).thenReturn(8L);
			when(this.collector.getMaxTotalLoadedClassCount()).thenReturn(9L);
			when(this.collector.getTotalUnloadedClassCount()).thenReturn(10L);
			when(this.collector.getMinUnloadedClassCount()).thenReturn(11L);
			when(this.collector.getMaxUnloadedClassCount()).thenReturn(12L);

			Timestamp timestamp = mock(Timestamp.class);
			when(this.collector.getTimeStamp()).thenReturn(timestamp);

			ClassLoadingInformationData collector = (ClassLoadingInformationData) this.cut.get();

			assertThat(collector, not(sameInstance(this.collector)));
			assertThat(collector.getPlatformIdent(), is(1L));
			assertThat(collector.getSensorTypeIdent(), is(2L));
			assertThat(collector.getCount(), is(3));
			assertThat(collector.getTotalLoadedClassCount(), is(4));
			assertThat(collector.getMinLoadedClassCount(), is(5));
			assertThat(collector.getMaxLoadedClassCount(), is(6));
			assertThat(collector.getTotalTotalLoadedClassCount(), is(7L));
			assertThat(collector.getMinTotalLoadedClassCount(), is(8L));
			assertThat(collector.getMaxTotalLoadedClassCount(), is(9L));
			assertThat(collector.getTotalUnloadedClassCount(), is(10L));
			assertThat(collector.getMinUnloadedClassCount(), is(11L));
			assertThat(collector.getMaxUnloadedClassCount(), is(12L));
			assertThat(collector.getTimeStamp(), is(timestamp));
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
			this.cut.reset();

			verify(this.collector).setCount(0);
			verify(this.collector).setTotalLoadedClassCount(0);
			verify(this.collector).setMinLoadedClassCount(Integer.MAX_VALUE);
			verify(this.collector).setMaxLoadedClassCount(0);
			verify(this.collector).setTotalTotalLoadedClassCount(0L);
			verify(this.collector).setMinTotalLoadedClassCount(Long.MAX_VALUE);
			verify(this.collector).setMaxTotalLoadedClassCount(0L);
			verify(this.collector).setTotalUnloadedClassCount(0L);
			verify(this.collector).setMinUnloadedClassCount(Long.MAX_VALUE);
			verify(this.collector).setMaxUnloadedClassCount(0L);
			verify(this.collector).setTimeStamp(any(Timestamp.class));
		}
	}

}