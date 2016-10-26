package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
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
	InfluxDBDao influxDao;

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
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			long startTime = System.currentTimeMillis();
			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> lastChecktimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(1)).setLastCheckTime(lastChecktimeCaptor.capture());
			assertThat(lastChecktimeCaptor.getValue(), greaterThanOrEqualTo(startTime));
			verify(stateManager, times(1)).noData(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void noViolationUpperThreshold() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD - 1.0 })));
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			long startTime = System.currentTimeMillis();
			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> lastChecktimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(1)).setLastCheckTime(lastChecktimeCaptor.capture());
			assertThat(lastChecktimeCaptor.getValue(), greaterThanOrEqualTo(startTime));
			verify(stateManager, times(1)).valid(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void noViolationLowerThreshold() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD + 1.0 })));
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			long startTime = System.currentTimeMillis();
			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> lastChecktimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(1)).setLastCheckTime(lastChecktimeCaptor.capture());
			assertThat(lastChecktimeCaptor.getValue(), greaterThanOrEqualTo(startTime));
			verify(stateManager, times(1)).valid(alertingState);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void violationUpperThreshold() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD + 1.0 })));
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			long startTime = System.currentTimeMillis();
			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> lastChecktimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(1)).setLastCheckTime(lastChecktimeCaptor.capture());
			assertThat(lastChecktimeCaptor.getValue(), greaterThanOrEqualTo(startTime));
			verify(stateManager, times(1)).violation(alertingState, THRESHOLD + 1.0);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void violationLowerThreshold() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD - 1.0 })));
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			long startTime = System.currentTimeMillis();
			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> lastChecktimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(1)).setLastCheckTime(lastChecktimeCaptor.capture());
			assertThat(lastChecktimeCaptor.getValue(), greaterThanOrEqualTo(startTime));
			verify(stateManager, times(1)).violation(alertingState, THRESHOLD - 1.0);
			verifyNoMoreInteractions(stateManager);
		}

		@Test
		public void influxDisconnected() throws BusinessException, Exception {
			queryResult.getResults().get(0).getSeries().get(0).setValues(Collections.singletonList(Arrays.asList(new Object[] { "a", THRESHOLD + 1.0 })));
			when(influxDao.isConnected()).thenReturn(false);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			verifyNoMoreInteractions(stateManager);
		}

	}
}
