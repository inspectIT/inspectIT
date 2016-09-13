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
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MemoryInformationPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	MemoryInformationPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	MemoryInformationData data;

	public class CreateBuilder extends MemoryInformationPointBuilderTest {

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
			when(data.getTotalFreePhysMemory()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalFreeSwapSpace()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalComittedHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalComittedNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalUsedHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getTotalUsedNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getMinUsedHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getMaxUsedHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getMinUsedNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());
			when(data.getMaxUsedNonHeapMemorySize()).thenReturn(RandomUtils.nextLong());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.MemoryInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_COMMITTED_HEAP_MEMORY, (Object) (data.getTotalComittedHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_COMMITTED_NON_HEAP_MEMORY, (Object) (data.getTotalComittedNonHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_FREE_PHYS_MEMORY, (Object) (data.getTotalFreePhysMemory() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_FREE_SWAP_SPACE, (Object) (data.getTotalFreeSwapSpace() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_USED_HEAP_MEMORY, (Object) (data.getTotalUsedHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_USED_NON_HEAP_MEMORY, (Object) (data.getTotalUsedNonHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MAX_USED_HEAP_MEMORY, (Object) data.getMaxUsedHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MIN_USED_HEAP_MEMORY, (Object) data.getMinUsedHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MAX_USED_NON_HEAP_MEMORY, (Object) data.getMaxUsedNonHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MIN_USED_NON_HEAP_MEMORY, (Object) data.getMinUsedNonHeapMemorySize()));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getCount()).thenReturn(1);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.MemoryInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
		}

		@Test
		public void allZeros() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getCount()).thenReturn(5);
			when(data.getTotalFreePhysMemory()).thenReturn(0L);
			when(data.getTotalFreeSwapSpace()).thenReturn(0L);
			when(data.getTotalComittedHeapMemorySize()).thenReturn(0L);
			when(data.getTotalComittedNonHeapMemorySize()).thenReturn(0L);
			when(data.getTotalUsedHeapMemorySize()).thenReturn(0L);
			when(data.getTotalUsedNonHeapMemorySize()).thenReturn(0L);
			when(data.getMinUsedHeapMemorySize()).thenReturn(0L);
			when(data.getMaxUsedHeapMemorySize()).thenReturn(0L);
			when(data.getMinUsedNonHeapMemorySize()).thenReturn(0L);
			when(data.getMaxUsedNonHeapMemorySize()).thenReturn(0L);

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.MemoryInformation.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_COMMITTED_HEAP_MEMORY, (Object) (data.getTotalComittedHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_COMMITTED_NON_HEAP_MEMORY, (Object) (data.getTotalComittedNonHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_FREE_PHYS_MEMORY, (Object) (data.getTotalFreePhysMemory() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_FREE_SWAP_SPACE, (Object) (data.getTotalFreeSwapSpace() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_USED_HEAP_MEMORY, (Object) (data.getTotalUsedHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_AVG_USED_NON_HEAP_MEMORY, (Object) (data.getTotalUsedNonHeapMemorySize() / data.getCount())));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MAX_USED_HEAP_MEMORY, (Object) data.getMaxUsedHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MIN_USED_HEAP_MEMORY, (Object) data.getMinUsedHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MAX_USED_NON_HEAP_MEMORY, (Object) data.getMaxUsedNonHeapMemorySize()));
			assertThat(getFields(pointBuilder), hasEntry(Series.MemoryInformation.FIELD_MIN_USED_NON_HEAP_MEMORY, (Object) data.getMinUsedNonHeapMemorySize()));
		}

	}

}
