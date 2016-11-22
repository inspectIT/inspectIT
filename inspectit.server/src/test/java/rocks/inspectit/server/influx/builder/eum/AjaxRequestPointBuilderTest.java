package rocks.inspectit.server.influx.builder.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;

/**
 * @author Jonas Kunz
 *
 */
public class AjaxRequestPointBuilderTest extends AbstractEUMRequestPointBuilderTest {

	@InjectMocks
	AjaxRequestPointBuilder builder;

	@Mock
	AjaxRequest request;

	private static final long ENTER_TIMESTAMP = 11000;
	private static final double DURATION = 1000;

	private static final String HTTP_METHOD = "POST";
	private static final int HTTP_STATUS = 200;


	@BeforeMethod
	public void initMocks() {
		super.initMocks(request);

		when(request.getOwningSpan().getTimeStamp()).thenReturn(new Timestamp(ENTER_TIMESTAMP));
		when(request.getOwningSpan().getDuration()).thenReturn(DURATION);

		when(request.getBaseUrl()).thenReturn(PAGELOAD_URL);
		when(request.getMethod()).thenReturn(HTTP_METHOD);
		when(request.getStatus()).thenReturn(HTTP_STATUS);
	}

	public static class Build extends AjaxRequestPointBuilderTest {

		@Test
		public void testAllDataAvailable() throws Exception {
			Collection<Builder> result = builder.build(sessionInfo, pageLoadRequest, request);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(BROWSER));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(LANGUAGE));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(DEVICE));
			// ajax specific data
			assertThat(time, equalTo(ENTER_TIMESTAMP));
			assertThat(tags.get(Series.EumAjax.TAG_BASE_URL), equalTo(PAGELOAD_URL));
			assertThat(fields.get(Series.EumAjax.FIELD_DURATION), equalTo((Object) (DURATION)));
			assertThat(fields.get(Series.EumAjax.FIELD_METHOD), equalTo((Object) HTTP_METHOD));
			assertThat(fields.get(Series.EumAjax.FIELD_STATUS), equalTo((Object) Long.valueOf(HTTP_STATUS)));
		}

		@Test
		public void testMissingSessionInfo() throws Exception {
			Collection<Builder> result = builder.build(null, pageLoadRequest, request);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(null));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(null));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(null));
			// ajax specific data
			assertThat(time, equalTo(ENTER_TIMESTAMP));
			assertThat(tags.get(Series.EumAjax.TAG_BASE_URL), equalTo(PAGELOAD_URL));
			assertThat(fields.get(Series.EumAjax.FIELD_DURATION), equalTo((Object) (DURATION)));
			assertThat(fields.get(Series.EumAjax.FIELD_METHOD), equalTo((Object) HTTP_METHOD));
			assertThat(fields.get(Series.EumAjax.FIELD_STATUS), equalTo((Object) Long.valueOf(HTTP_STATUS)));
		}

		@Test
		public void testMissingPageloadRequest() throws Exception {
			Collection<Builder> result = builder.build(sessionInfo, null, request);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(BROWSER));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(LANGUAGE));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(DEVICE));
			// ajax specific data
			assertThat(time, equalTo(ENTER_TIMESTAMP));
			assertThat(tags.get(Series.EumAjax.TAG_BASE_URL), equalTo(PAGELOAD_URL));
			assertThat(fields.get(Series.EumAjax.FIELD_DURATION), equalTo((Object) (DURATION)));
			assertThat(fields.get(Series.EumAjax.FIELD_METHOD), equalTo((Object) HTTP_METHOD));
			assertThat(fields.get(Series.EumAjax.FIELD_STATUS), equalTo((Object) Long.valueOf(HTTP_STATUS)));
		}

	}

}
