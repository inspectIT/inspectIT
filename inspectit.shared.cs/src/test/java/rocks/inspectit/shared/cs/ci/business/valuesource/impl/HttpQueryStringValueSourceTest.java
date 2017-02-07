package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class HttpQueryStringValueSourceTest extends TestBase {
	@InjectMocks
	HttpQueryStringValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	@Mock
	HttpTimerData httpTimerData;

	/**
	 * Test
	 * {@link HttpQueryStringValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringValues extends HttpQueryStringValueSourceTest {
		private static final String TEST_QUERY_STRING = "test=true";

		@Test
		public void retrieveQueryString() {
			HttpInfo httpInfo = new HttpInfo();
			httpInfo.setQueryString(TEST_QUERY_STRING);

			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Mockito.doReturn(httpInfo).when(httpTimerData).getHttpInfo();

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(TEST_QUERY_STRING));
		}

		@Test
		public void noHttpData() {
			Mockito.doReturn(new TimerData()).when(invocationSeuence).getTimerData();

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
