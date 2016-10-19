package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.influx.dao.IInfluxDBDao;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 * Tests for the {@link ThresholdChecker}.
 *
 * @author Marius Oehler
 *
 */
public class ThresholdCheckerTest extends TestBase {

	@InjectMocks
	ThresholdChecker thresholdChecker;

	@Mock
	Logger log;

	@Mock
	IInfluxDBDao influxDao;

	@Mock
	AlertingStateLifecycleManager stateManager;

	/**
	 * Tests the
	 * {@link ThresholdChecker#checkThreshold(rocks.inspectit.server.alerting.state.AlertingState)}
	 * method.
	 */
	public static class CheckThreshold extends ThresholdCheckerTest {
		AlertingDefinition alertingDefinition;
		String alertId;
		long time = 12345678L;
		long stopTime = 123456789L;
		String alertDefName = "MyAlert";
		double threshold = 100.1;
		long agentId = 123;

		QueryResult queryResult;

		@BeforeMethod
		public void init() {
			alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.setThreshold(threshold);
			alertingDefinition.setName(alertDefName);
			alertingDefinition.setTimeRange(10L, TimeUnit.MINUTES);
		}

		@BeforeMethod
		public void buildQueryResult() {
			queryResult = new QueryResult();
			Result result = new Result();
			queryResult.setResults(Collections.singletonList(result));
			org.influxdb.dto.QueryResult.Series series = new org.influxdb.dto.QueryResult.Series();
			result.setSeries(Collections.singletonList(series));
			Object[] values = { "ABC", new Double(2.2) };
			series.setValues(Collections.singletonList(Arrays.asList(values)));
			series.setColumns(Arrays.asList(new String[] { "col1", "col2" }));
		}

		@Test
		public void noData() throws BusinessException, Exception {
			when(influxDao.isOnline()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			AlertingState alertingState = new AlertingState(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).noData(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void noViolation() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", threshold - 1.0 })));
			when(influxDao.isOnline()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			AlertingState alertingState = new AlertingState(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).valid(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void violation() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", threshold + 1.0 })));
			when(influxDao.isOnline()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			AlertingState alertingState = new AlertingState(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).violation(alertingState, threshold + 1.0);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void influxDisconnected() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", threshold + 1.0 })));
			when(influxDao.isOnline()).thenReturn(false);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			AlertingState alertingState = new AlertingState(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verifyNoMoreInteractions(stateManager);
		}

	}

	/**
	 * Test the private {@link ThresholdChecker#isViolating(AlertingDefinition, double)} method.
	 */
	public static class IsViolating extends ThresholdCheckerTest {

		private boolean executeIsViolating(AlertingDefinition alertingDefinition, double value) throws Exception {
			Method isViolatingMethod = ThresholdChecker.class.getDeclaredMethod("isViolating", AlertingDefinition.class, double.class);
			isViolatingMethod.setAccessible(true);
			return (boolean) isViolatingMethod.invoke(thresholdChecker, alertingDefinition, value);
		}

		@Test
		public void isValidUpper() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 8);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isEqualsUpper() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 10);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isViolatingUpper() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 12);

			assertThat(isViolating, equalTo(true));
		}

		@Test
		public void isValidLower() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 12);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isEqualsLower() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 10);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isViolatingLower() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setThreshold(10);
			definition.setThresholdType(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = executeIsViolating(definition, 8);

			assertThat(isViolating, equalTo(true));
		}
	}
}
