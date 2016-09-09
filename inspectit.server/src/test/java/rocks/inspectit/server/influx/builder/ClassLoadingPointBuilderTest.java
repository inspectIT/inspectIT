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
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClassLoadingPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	ClassLoadingPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	ClassLoadingInformationData data;

	public class CreateBuilder extends ClassLoadingPointBuilderTest {

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
			when(data.getTotalLoadedClassCount()).thenReturn(RandomUtils.nextInt());
			when(data.getTotalTotalLoadedClassCount()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalUnloadedClassCount()).thenReturn(RandomUtils.nextLong());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.ClassLoadingInfomation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ClassLoadingInfomation.FIELD_LOADED_CLASSES, (Object) ((long) data.getTotalLoadedClassCount() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.ClassLoadingInfomation.FIELD_TOTAL_LOADED_CLASSES, (Object) (data.getTotalTotalLoadedClassCount() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.ClassLoadingInfomation.FIELD_UNLOADED_CLASSES, (Object) (data.getTotalUnloadedClassCount() / data.getCount())));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getCount()).thenReturn(1);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.ClassLoadingInfomation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
		}

	}

}
