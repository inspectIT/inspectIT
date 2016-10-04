package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
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
		private final static double THRESHOLD = 100.1;

		@Mock
		AlertingDefinition alertingDefinition;

		@Mock
		AlertingState alertingState;

		@Mock
		QueryResult queryResult;

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
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).noData(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void noViolation() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD - 1.0 })));
			when(influxDao.isOnline()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).valid(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void violation() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD + 1.0 })));
			when(influxDao.isOnline()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verify(stateManager, times(1)).violation(alertingState, THRESHOLD + 1.0);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void influxDisconnected() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD + 1.0 })));
			when(influxDao.isOnline()).thenReturn(false);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verifyNoMoreInteractions(stateManager);
		}

	}

	/**
	 * Test the private {@link ThresholdChecker#isViolating(AlertingDefinition, double)} method.
	 */
	public static class IsViolating extends ThresholdCheckerTest {

		@Mock
		AlertingDefinition definition;

		@Test
		public void isValidUpper() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 8);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isEqualsUpper() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 10);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isViolatingUpper() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 12);

			assertThat(isViolating, equalTo(true));
		}

		@Test
		public void isValidLower() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 12);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isEqualsLower() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 10);

			assertThat(isViolating, equalTo(false));
		}

		@Test
		public void isViolatingLower() throws Exception {
			when(definition.getThreshold()).thenReturn(10.0);
			when(definition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);

			boolean isViolating = thresholdChecker.isViolating(definition, 8);

			assertThat(isViolating, equalTo(true));
		}
	}
}
