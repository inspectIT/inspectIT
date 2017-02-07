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
public class HttpServerNameValueSourceTest extends TestBase {
	@InjectMocks
	HttpServerNameValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	@Mock
	HttpTimerData httpTimerData;

	/**
	 * Test
	 * {@link HttpServerNameValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringValues extends HttpServerNameValueSourceTest {
		private static final String TEST_SERVER_NAME = "localhost";

		@Test
		public void retrieveServerName() {
			HttpInfo httpInfo = new HttpInfo();
			httpInfo.setServerName(TEST_SERVER_NAME);

			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Mockito.doReturn(httpInfo).when(httpTimerData).getHttpInfo();

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(TEST_SERVER_NAME));
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
