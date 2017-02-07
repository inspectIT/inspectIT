package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;

import org.mockito.InjectMocks;
import org.mockito.Mock;
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
	InvocationSequenceData invocationSequenceData;

	@Mock
	HttpTimerData httpTimerData;

	/**
	 * Test
	 * {@link HttpQueryStringValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringValues extends HttpQueryStringValueSourceTest {

		@Test
		public void retrieveQueryString() {
			HttpInfo httpInfo = new HttpInfo();
			httpInfo.setQueryString("test=true");
			doReturn(httpTimerData).when(invocationSequenceData).getTimerData();
			doReturn(httpInfo).when(httpTimerData).getHttpInfo();

			String[] values = valueSource.getStringValues(invocationSequenceData, cachedDataService);

			assertThat(values, hasItemInArray("test=true"));
		}

		@Test
		public void noHttpData() {
			doReturn(new TimerData()).when(invocationSequenceData).getTimerData();

			String[] values = valueSource.getStringValues(invocationSequenceData, cachedDataService);

			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
