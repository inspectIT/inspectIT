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
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class HttpPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	HttpPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	HttpTimerData data;

	@Mock
	HttpInfo httpInfo;

	public class CreateBuilder extends HttpPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final String AGENT_NAME = "Agent";
		static final String URI = "Uri";
		static final String TAG = "Tag";

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
			when(data.getDuration()).thenReturn(RandomUtils.nextDouble());
			when(data.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getUri()).thenReturn(URI);
			when(httpInfo.hasInspectItTaggingHeader()).thenReturn(true);
			when(httpInfo.getInspectItTaggingHeaderValue()).thenReturn(TAG);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Http.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.URI, String.valueOf(URI)));
			assertThat(getTags(pointBuilder), hasEntry(Tags.INSPECTIT_TAGGING_HEADER, String.valueOf(TAG)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Http.FIELD_DURATION, (Object) data.getDuration()));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getUri()).thenReturn(URI);
			when(httpInfo.hasInspectItTaggingHeader()).thenReturn(true);
			when(httpInfo.getInspectItTaggingHeaderValue()).thenReturn(TAG);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Http.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Tags.AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Http.FIELD_DURATION, (Object) data.getDuration()));
		}

		@Test
		public void noTag() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getUri()).thenReturn(URI);
			when(httpInfo.hasInspectItTaggingHeader()).thenReturn(false);
			when(httpInfo.getInspectItTaggingHeaderValue()).thenReturn(null);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Http.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.INSPECTIT_TAGGING_HEADER)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Http.FIELD_DURATION, (Object) data.getDuration()));
		}

		@Test
		public void noUri() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getHttpInfo()).thenReturn(httpInfo);
			when(httpInfo.getUri()).thenReturn(null);
			when(httpInfo.hasInspectItTaggingHeader()).thenReturn(false);
			when(httpInfo.getInspectItTaggingHeaderValue()).thenReturn(null);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Http.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), not(hasKey(Tags.URI)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Http.FIELD_DURATION, (Object) data.getDuration()));
		}

	}

}
