package rocks.inspectit.server.influx.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.influx.constants.Tags;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class TimerPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	TimerPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	TimerData data;

	@Mock
	MethodIdent methodIdent;

	public class CreateBuilder extends TimerPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final long METHOD_ID = 2L;
		static final String AGENT_NAME = "Agent";
		static final String FQN = "class_fqn";
		static final String METHOD = "method";
		static final String METHOD_SIG = "signature";

		@BeforeMethod
		public void setup() {
			when(platformIdent.getAgentName()).thenReturn(AGENT_NAME);
			when(methodIdent.getFQN()).thenReturn(FQN);
			when(methodIdent.getMethodName()).thenReturn(METHOD);
			when(methodIdent.getFullyQualifiedMethodSignature()).thenReturn(METHOD_SIG);
		}

		@Test
		public void happyPath() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getMethodIdentForId(METHOD_ID)).thenReturn(methodIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getMethodIdent()).thenReturn(METHOD_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getMin()).thenReturn(RandomUtils.nextDouble());
			when(data.getMax()).thenReturn(RandomUtils.nextDouble());
			when(data.getAverage()).thenReturn(RandomUtils.nextDouble());
			when(data.getCpuMin()).thenReturn(RandomUtils.nextDouble());
			when(data.getCpuMax()).thenReturn(RandomUtils.nextDouble());
			when(data.getCpuAverage()).thenReturn(RandomUtils.nextDouble());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Methods.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.CLASS_FQN, String.valueOf(FQN)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.METHOD_NAME, String.valueOf(METHOD)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.METHOD_SIGNATURE, String.valueOf(METHOD_SIG)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_MIN_DURATION, (Object) data.getMin()));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_DURATION, (Object) data.getAverage()));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_MAX_DURATION, (Object) data.getMax()));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_MIN_CPU_TIME, (Object) data.getCpuMin()));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_CPU_TIME, (Object) data.getCpuAverage()));
			assertThat(getFields(pointBuilder), hasEntry(Series.Methods.FIELD_MAX_CPU_TIME, (Object) data.getCpuMax()));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);
			when(cachedDataService.getMethodIdentForId(METHOD_ID)).thenReturn(methodIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getMethodIdent()).thenReturn(METHOD_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(RandomUtils.nextDouble());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Methods.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.CLASS_FQN, String.valueOf(FQN)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.METHOD_NAME, String.valueOf(METHOD)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.METHOD_SIGNATURE, String.valueOf(METHOD_SIG)));
		}

		@Test
		public void noMethodIdent() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getMethodIdentForId(METHOD_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getMethodIdent()).thenReturn(METHOD_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getDuration()).thenReturn(RandomUtils.nextDouble());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Methods.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.CLASS_FQN)));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.METHOD_NAME)));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.METHOD_SIGNATURE)));
		}

	}

}
