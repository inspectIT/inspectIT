package info.novatec.inspectit.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.not;

import info.novatec.inspectit.ci.business.valuesource.impl.HttpParameterValueSource;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class HttpParameterValueSourceTest extends TestBase {
	@InjectMocks
	HttpParameterValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	@Mock
	HttpTimerData httpTimerData;

	/**
	 * Test
	 * {@link HttpParameterValueSource#getStringValues(InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends HttpParameterValueSourceTest {
		private static final String PARAMETER_NAME_1 = "parameter1";
		private static final String PARAMETER_NAME_2 = "parameter2";
		private static final String VALUE_A = "valueA";
		private static final String VALUE_B = "valueB";
		private static final String VALUE_C = "valueC";
		private static final String VALUE_D = "valueD";

		@BeforeMethod
		public void init() {
			Map<String, String[]> parameterMap = new HashMap<>();
			parameterMap.put(PARAMETER_NAME_1, new String[] { VALUE_A, VALUE_B });
			parameterMap.put(PARAMETER_NAME_2, new String[] { VALUE_C, VALUE_D });
			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Mockito.doReturn(parameterMap).when(httpTimerData).getParameters();
		}

		@Test
		public void retrieveFirstParameter() {
			valueSource.setParameterName(PARAMETER_NAME_1);
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(VALUE_A));
			assertThat(values, hasItemInArray(VALUE_B));
			assertThat(values, not(hasItemInArray(VALUE_C)));
			assertThat(values, not(hasItemInArray(VALUE_D)));
		}

		@Test
		public void retrieveSecondParameter() {
			valueSource.setParameterName(PARAMETER_NAME_2);
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(VALUE_C));
			assertThat(values, hasItemInArray(VALUE_D));
			assertThat(values, not(hasItemInArray(VALUE_A)));
			assertThat(values, not(hasItemInArray(VALUE_B)));
		}
	}
}
