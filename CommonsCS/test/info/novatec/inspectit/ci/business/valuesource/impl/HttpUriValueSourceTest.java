package info.novatec.inspectit.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

import info.novatec.inspectit.ci.business.valuesource.impl.HttpUriValueSource;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.HttpInfo;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class HttpUriValueSourceTest extends TestBase {
	@InjectMocks
	HttpUriValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	@Mock
	HttpTimerData httpTimerData;

	/**
	 * Test
	 * {@link HttpUriValueSource#getStringValues(InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends HttpUriValueSourceTest {
		private static final String TEST_URI = "/my/test/uri";

		@Test
		public void retrieveURI() {
			HttpInfo httpInfo = new HttpInfo(TEST_URI, "POST", null);

			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Mockito.doReturn(httpInfo).when(httpTimerData).getHttpInfo();

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(TEST_URI));
		}
	}
}
