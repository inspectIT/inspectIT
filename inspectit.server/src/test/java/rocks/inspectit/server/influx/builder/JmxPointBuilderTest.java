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
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JmxPointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	JmxPointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	JmxSensorValueData data;

	@Mock
	JmxDefinitionDataIdent jmxDefinitionDataIdent;

	public class CreateBuilder extends JmxPointBuilderTest {

		static final long PLATFORM_ID = 1L;
		static final long JMX_IDENT = 2L;
		static final String AGENT_NAME = "Agent";
		static final String JMX_ATTR = "jmx_attr";

		@BeforeMethod
		public void setup() {
			when(platformIdent.getAgentName()).thenReturn(AGENT_NAME);
			when(jmxDefinitionDataIdent.getDerivedFullName()).thenReturn(JMX_ATTR);
		}

		@Test
		public void happyPath() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getJmxDefinitionDataIdentForId(JMX_IDENT)).thenReturn(jmxDefinitionDataIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getJmxSensorDefinitionDataIdentId()).thenReturn(JMX_IDENT);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));
			when(data.getValueAsDouble()).thenReturn(RandomUtils.nextDouble());

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Jmx.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_NAME, String.valueOf(AGENT_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.Jmx.TAG_JMX_ATTRIBUTE_FULL_NAME, String.valueOf(JMX_ATTR)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Jmx.FIELD_VALUE, (Object) data.getValueAsDouble()));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(null);
			when(cachedDataService.getJmxDefinitionDataIdentForId(JMX_IDENT)).thenReturn(jmxDefinitionDataIdent);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getJmxSensorDefinitionDataIdentId()).thenReturn(JMX_IDENT);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Jmx.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.TAG_AGENT_ID, String.valueOf(PLATFORM_ID)));
			assertThat(getTags(pointBuilder), not(hasKey(Series.TAG_AGENT_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Jmx.FIELD_VALUE, (Object) data.getValueAsDouble()));
		}

		@Test
		public void noJmxIdent() throws Exception {
			when(cachedDataService.getPlatformIdentForId(PLATFORM_ID)).thenReturn(platformIdent);
			when(cachedDataService.getJmxDefinitionDataIdentForId(JMX_IDENT)).thenReturn(null);

			long time = RandomUtils.nextLong();
			when(data.getPlatformIdent()).thenReturn(PLATFORM_ID);
			when(data.getJmxSensorDefinitionDataIdentId()).thenReturn(JMX_IDENT);
			when(data.getTimeStamp()).thenReturn(new Timestamp(time));

			Builder pointBuilder = builder.createBuilder(data);

			assertThat(getMeasurement(pointBuilder), is(Series.Jmx.NAME));
			assertThat(getTime(pointBuilder), is(time));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), not(hasKey(Series.Jmx.TAG_JMX_ATTRIBUTE_FULL_NAME)));
			assertThat(getFields(pointBuilder), hasEntry(Series.Jmx.FIELD_VALUE, (Object) data.getValueAsDouble()));
		}

	}

}
