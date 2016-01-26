package rocks.inspectit.shared.all.instrumentation.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test for the {@link SensorInstrumentationPoint}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SensorInstrumentationPointTest extends TestBase {

	@InjectMocks
	SensorInstrumentationPoint registeredSensorConfig;

	public class ConatinsSensorIds extends SensorInstrumentationPointTest {

		@Test
		public void containsId() {
			registeredSensorConfig.addSensorId(1, PriorityEnum.MIN);
			registeredSensorConfig.addSensorId(2, PriorityEnum.MAX);

			boolean containsSensorId1 = registeredSensorConfig.containsSensorId(1);
			boolean containsSensorId2 = registeredSensorConfig.containsSensorId(2);

			assertThat(containsSensorId1, is(true));
			assertThat(containsSensorId2, is(true));
		}
	}

	public class GetSensorIds extends SensorInstrumentationPointTest {

		@Test
		public void noDoubleId() {
			registeredSensorConfig.addSensorId(1, PriorityEnum.MIN);
			registeredSensorConfig.addSensorId(2, PriorityEnum.MAX);
			registeredSensorConfig.addSensorId(1, PriorityEnum.MAX);
			registeredSensorConfig.addSensorId(2, PriorityEnum.MIN);

			long[] sensorIds = registeredSensorConfig.getSensorIds();

			assertThat(sensorIds.length, is(2));
		}

		@Test
		public void priority() {
			registeredSensorConfig.addSensorId(1, PriorityEnum.MAX);
			registeredSensorConfig.addSensorId(2, PriorityEnum.MIN);

			long[] sensorIds = registeredSensorConfig.getSensorIds();

			assertThat(sensorIds.length, is(2));
			assertThat(sensorIds[0], is(1L));
			assertThat(sensorIds[1], is(2L));
		}

		@Test
		public void priorityReverse() {
			registeredSensorConfig.addSensorId(1, PriorityEnum.MIN);
			registeredSensorConfig.addSensorId(2, PriorityEnum.MAX);

			long[] sensorIds = registeredSensorConfig.getSensorIds();

			assertThat(sensorIds.length, is(2));
			// highest priority first
			assertThat(sensorIds[0], is(2L));
			assertThat(sensorIds[1], is(1L));
		}
	}
}
