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
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest.NavigationTimings;

/**
 * @author Jonas Kunz
 *
 */
public class PageLoadRequestPointBuilderTest extends AbstractEUMRequestPointBuilderTest {

	@InjectMocks
	PageLoadRequestPointBuilder builder;

	@Mock
	NavigationTimings navTimings;

	private static final double NAV_START = 11000;
	private static final double UNLOAD_START = NAV_START + 1;
	private static final double UNLOAD_END = NAV_START + 2;
	private static final double REDIRECT_START = NAV_START + 3;
	private static final double REDIRECT_END = NAV_START + 4;
	private static final double CONNECT_START = NAV_START + 5;
	private static final double CONNECT_END = NAV_START + 6;
	private static final double DOMAINLOOKUP_START = NAV_START + 7;
	private static final double DOMAINLOOKUP_END = NAV_START + 8;
	private static final double FETCH_START = NAV_START + 9;
	private static final double DOM_COMPLETE = NAV_START + 10;
	private static final double DOM_CONT_LOAD_START = NAV_START + 11;
	private static final double DOM_CONT_LOAD_END = NAV_START + 12;
	private static final double DOM_INTERACTIVE = NAV_START + 13;
	private static final double DOM_LOADING = NAV_START + 14;
	private static final double FIRSTPAINT = NAV_START + 15;
	private static final double RESPONSE_START = NAV_START + 16;
	private static final double RESPONSE_END = NAV_START + 17;
	private static final double SECURE_CONNECTION_START = NAV_START + 18;
	private static final double LOAD_START = NAV_START + 19;
	private static final double LOAD_END = NAV_START + 20;
	private static final double SPEED_INDEX = 100;

	@BeforeMethod
	public void initMocks() {
		super.initMocks(null);

		when(pageLoadRequest.getOwningSpan().getTimeStamp()).thenReturn(new Timestamp((long) NAV_START));
		when(pageLoadRequest.getOwningSpan().getDuration()).thenReturn((LOAD_END - NAV_START));
		when(pageLoadRequest.getNavigationTimings()).thenReturn(navTimings);

		when(navTimings.getNavigationStart()).thenReturn(NAV_START);
		when(navTimings.getUnloadEventStart()).thenReturn(UNLOAD_START);
		when(navTimings.getUnloadEventEnd()).thenReturn(UNLOAD_END);
		when(navTimings.getRedirectStart()).thenReturn(REDIRECT_START);
		when(navTimings.getRedirectEnd()).thenReturn(REDIRECT_END);
		when(navTimings.getConnectStart()).thenReturn(CONNECT_START);
		when(navTimings.getConnectEnd()).thenReturn(CONNECT_END);
		when(navTimings.getDomainLookupStart()).thenReturn(DOMAINLOOKUP_START);
		when(navTimings.getDomainLookupEnd()).thenReturn(DOMAINLOOKUP_END);
		when(navTimings.getFetchStart()).thenReturn(FETCH_START);
		when(navTimings.getDomComplete()).thenReturn(DOM_COMPLETE);
		when(navTimings.getDomContentLoadedEventStart()).thenReturn(DOM_CONT_LOAD_START);
		when(navTimings.getDomContentLoadedEventEnd()).thenReturn(DOM_CONT_LOAD_END);
		when(navTimings.getDomInteractive()).thenReturn(DOM_INTERACTIVE);
		when(navTimings.getDomLoading()).thenReturn(DOM_LOADING);
		when(navTimings.getFirstPaint()).thenReturn(FIRSTPAINT);
		when(navTimings.getLoadEventStart()).thenReturn(LOAD_START);
		when(navTimings.getLoadEventEnd()).thenReturn(LOAD_END);
		when(navTimings.getResponseStart()).thenReturn(RESPONSE_START);
		when(navTimings.getResponseEnd()).thenReturn(RESPONSE_END);
		when(navTimings.getSecureConnectionStart()).thenReturn(SECURE_CONNECTION_START);
		when(navTimings.getSpeedIndex()).thenReturn(SPEED_INDEX);
	}

	public static class Build extends PageLoadRequestPointBuilderTest {

		@Test
		public void testAllDataAvailable() throws Exception {
			Collection<Builder> result = builder.build(sessionInfo, pageLoadRequest, pageLoadRequest);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(BROWSER));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(LANGUAGE));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(DEVICE));
			// nav timings specific data
			assertThat(time, equalTo((long) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_NAVIGATION_START), equalTo((Object) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_START), equalTo((Object) (UNLOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_END), equalTo((Object) (UNLOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_START), equalTo((Object) (REDIRECT_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_END), equalTo((Object) (REDIRECT_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_START), equalTo((Object) (CONNECT_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_END), equalTo((Object) (CONNECT_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_START), equalTo((Object) (DOMAINLOOKUP_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_END), equalTo((Object) (DOMAINLOOKUP_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FETCH_START), equalTo((Object) (FETCH_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_COMPLETE), equalTo((Object) (DOM_COMPLETE - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_START), equalTo((Object) (DOM_CONT_LOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_END), equalTo((Object) (DOM_CONT_LOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_INTERACTIVE), equalTo((Object) (DOM_INTERACTIVE - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_LOADING), equalTo((Object) (DOM_LOADING - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FIRSTPAINT), equalTo((Object) (FIRSTPAINT - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_START), equalTo((Object) (LOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_END), equalTo((Object) (LOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_START), equalTo((Object) (RESPONSE_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_END), equalTo((Object) (RESPONSE_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SECURE_CONNECT_START), equalTo((Object) (SECURE_CONNECTION_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SPEEDINDEX), equalTo((Object) (SPEED_INDEX)));
		}

		@Test
		public void testMissingSessionInfo() throws Exception {
			Collection<Builder> result = builder.build(null, pageLoadRequest, pageLoadRequest);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(null));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(null));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(null));
			// nav timings specific data
			assertThat(time, equalTo((long) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_NAVIGATION_START), equalTo((Object) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_START), equalTo((Object) (UNLOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_END), equalTo((Object) (UNLOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_START), equalTo((Object) (REDIRECT_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_END), equalTo((Object) (REDIRECT_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_START), equalTo((Object) (CONNECT_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_END), equalTo((Object) (CONNECT_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_START), equalTo((Object) (DOMAINLOOKUP_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_END), equalTo((Object) (DOMAINLOOKUP_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FETCH_START), equalTo((Object) (FETCH_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_COMPLETE), equalTo((Object) (DOM_COMPLETE - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_START), equalTo((Object) (DOM_CONT_LOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_END), equalTo((Object) (DOM_CONT_LOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_INTERACTIVE), equalTo((Object) (DOM_INTERACTIVE - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_LOADING), equalTo((Object) (DOM_LOADING - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FIRSTPAINT), equalTo((Object) (FIRSTPAINT - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_START), equalTo((Object) (LOAD_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_END), equalTo((Object) (LOAD_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_START), equalTo((Object) (RESPONSE_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_END), equalTo((Object) (RESPONSE_END - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SECURE_CONNECT_START), equalTo((Object) (SECURE_CONNECTION_START - NAV_START)));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SPEEDINDEX), equalTo((Object) (SPEED_INDEX)));
		}

		@Test
		public void testMissingNavTimings() throws Exception {
			when(pageLoadRequest.getNavigationTimings()).thenReturn(null);

			Collection<Builder> result = builder.build(sessionInfo, pageLoadRequest, pageLoadRequest);

			assertThat(result.size(), equalTo(1));
			Builder point = result.iterator().next();
			Map<String, Object> fields = getFields(point);
			Map<String, String> tags = getTags(point);
			Long time = getTime(point);
			// session info
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_BROWSER), equalTo(BROWSER));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_LANGUAGE), equalTo(LANGUAGE));
			assertThat(tags.get(Series.EUMBasicRequestSeries.TAG_DEVICE), equalTo(DEVICE));
			// nav timings specific data
			assertThat(time, equalTo((long) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_NAVIGATION_START), equalTo((Object) NAV_START));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_UNLOAD_EVENT_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_REDIRECT_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_CONNECT_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FETCH_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_COMPLETE), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_INTERACTIVE), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_DOM_LOADING), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_FIRSTPAINT), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_LOAD_EVENT_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_RESPONSE_END), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SECURE_CONNECT_START), equalTo(null));
			assertThat(fields.get(Series.EumPageLoad.FIELD_SPEEDINDEX), equalTo(null));
		}

	}

}
