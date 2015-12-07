package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
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
	 * {@link HttpParameterValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends HttpParameterValueSourceTest {
		private static final String PARAMETER_NAME_1 = "parameter1";
		private static final String PARAMETER_NAME_2 = "parameter2";
		private static final String VALUE_A = "valueA";
		private static final String VALUE_B = "valueB";
		private static final String VALUE_C = "valueC";
		private static final String VALUE_D = "valueD";

		@Test
		public void retrieveFirstParameter() {
			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Map<String, String[]> parameterMap = new HashMap<>();
			parameterMap.put(PARAMETER_NAME_1, new String[] { VALUE_A, VALUE_B });
			parameterMap.put(PARAMETER_NAME_2, new String[] { VALUE_C, VALUE_D });
			Mockito.doReturn(parameterMap).when(httpTimerData).getParameters();

			valueSource.setParameterName(PARAMETER_NAME_1);
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(VALUE_A));
			assertThat(values, hasItemInArray(VALUE_B));
			assertThat(values, not(hasItemInArray(VALUE_C)));
			assertThat(values, not(hasItemInArray(VALUE_D)));
		}

		@Test
		public void retrieveSecondParameter() {
			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Map<String, String[]> parameterMap = new HashMap<>();
			parameterMap.put(PARAMETER_NAME_1, new String[] { VALUE_A, VALUE_B });
			parameterMap.put(PARAMETER_NAME_2, new String[] { VALUE_C, VALUE_D });
			Mockito.doReturn(parameterMap).when(httpTimerData).getParameters();

			valueSource.setParameterName(PARAMETER_NAME_2);
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(VALUE_C));
			assertThat(values, hasItemInArray(VALUE_D));
			assertThat(values, not(hasItemInArray(VALUE_A)));
			assertThat(values, not(hasItemInArray(VALUE_B)));
		}

		@Test
		public void noHttpData() {
			Mockito.doReturn(new TimerData()).when(invocationSeuence).getTimerData();

			valueSource.setParameterName(PARAMETER_NAME_2);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}

		@Test
		public void noHttpParameter() {
			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Mockito.doReturn(new HashMap<>()).when(httpTimerData).getParameters();

			valueSource.setParameterName(PARAMETER_NAME_2);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}

		@Test
		public void noRequiredHttpParameter() {
			Mockito.doReturn(httpTimerData).when(invocationSeuence).getTimerData();
			Map<String, String[]> parameterMap = new HashMap<>();
			parameterMap.put(PARAMETER_NAME_1, new String[] { VALUE_A, VALUE_B });
			Mockito.doReturn(parameterMap).when(httpTimerData).getParameters();

			valueSource.setParameterName(PARAMETER_NAME_2);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
