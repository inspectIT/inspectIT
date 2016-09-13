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
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ThreadInformationPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	ThreadInformationPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	ThreadInformationData data;

	public class CreateBuilder extends ThreadInformationPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final String AGENT_NAME = "Agent";

		@BeforeMethod
		public void setup() {
			when(platformIdent.getAgentName()).thenReturn(AGENT_NAME);
		}

		@Test
		public void happyPath() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getCount()).thenReturn(5);
			when(data.getTotalThreadCount()).thenReturn(RandomUtils.nextInt());
			when(data.getTotalDaemonThreadCount()).thenReturn(RandomUtils.nextInt());
			when(data.getTotalTotalStartedThreadCount()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalPeakThreadCount()).thenReturn(RandomUtils.nextInt());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.ThreadInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ThreadInformation.FIELD_DEAMON_THREAD_COUNT, (Object) Long.valueOf(data.getTotalDaemonThreadCount() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.ThreadInformation.FIELD_LIVE_THREAD_COUNT, (Object) Long.valueOf(data.getTotalThreadCount() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.ThreadInformation.FIELD_PEAK_THREAD_COUNT, (Object) Long.valueOf(data.getTotalPeakThreadCount() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.ThreadInformation.FIELD_TOTAL_STARTED_THREAD_COUNT, (Object) (data.getTotalTotalStartedThreadCount() / data.getCount())));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getCount()).thenReturn(1);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.ThreadInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
		}

	}

}
