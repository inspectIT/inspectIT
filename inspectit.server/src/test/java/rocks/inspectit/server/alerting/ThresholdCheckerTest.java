package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
@SuppressWarnings("PMD")
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
		@Mock
		AlertingDefinition alertingDefinition;

		@Mock
		AlertingState alertingState;

		@Mock
		QueryResult queryResult;

		@BeforeMethod
		public void buildQueryResult() {
			Object[] values = { "12:00", 10D };
			Series series = new Series();
			series.setValues(Arrays.asList(Arrays.asList(values)));
			series.setColumns(Arrays.asList(new String[] { "time", "value" }));
			Result result = new Result();
			result.setSeries(Arrays.asList(series));
			Mockito.when(queryResult.getResults()).thenReturn(Arrays.asList(result));
		}

		@Test
		public void noData() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState).setLastCheckTime(timeCaptor.capture());
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).noData(alertingState);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition).getThresholdType();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void noViolationUpperThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(queryResult);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			Mockito.when(alertingDefinition.getThreshold()).thenReturn(15D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState).setLastCheckTime(timeCaptor.capture());
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).valid(alertingState);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition, times(2)).getThresholdType();
			Mockito.verify(alertingDefinition).getThreshold();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void noViolationLowerThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(queryResult);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			Mockito.when(alertingDefinition.getThreshold()).thenReturn(5D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState).setLastCheckTime(timeCaptor.capture());
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).valid(alertingState);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition, times(2)).getThresholdType();
			Mockito.verify(alertingDefinition).getThreshold();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void violationUpperThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(queryResult);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			Mockito.when(alertingDefinition.getThreshold()).thenReturn(5D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState).setLastCheckTime(timeCaptor.capture());
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).violation(alertingState, 10D);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition, times(2)).getThresholdType();
			Mockito.verify(alertingDefinition).getThreshold();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void violationLowerThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(queryResult);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			Mockito.when(alertingDefinition.getThreshold()).thenReturn(15D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState).setLastCheckTime(timeCaptor.capture());
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).violation(alertingState, 10D);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition, times(2)).getThresholdType();
			Mockito.verify(alertingDefinition).getThreshold();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void neverChecked() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			Mockito.when(influxDao.isOnline()).thenReturn(true);
			Mockito.when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(-1L);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> currentTimeCaptor = ArgumentCaptor.forClass(Long.class);
			Mockito.verify(alertingState, times(2)).getLastCheckTime();
			Mockito.verify(alertingState, times(2)).setLastCheckTime(currentTimeCaptor.capture());
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			assertThat(currentTimeCaptor.getValue(), greaterThanOrEqualTo(time));
			Mockito.verify(alertingDefinition, times(2)).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verify(influxDao).query(any(String.class));
			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verify(stateManager).noData(alertingState);
			Mockito.verifyNoMoreInteractions(stateManager);
			Mockito.verify(alertingDefinition).getThresholdType();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verify(alertingDefinition).getTags();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition, times(2)).getTimeRange(TimeUnit.MILLISECONDS);
			Mockito.verifyNoMoreInteractions(alertingDefinition);
		}

		@Test
		public void influxDisconnected() throws BusinessException, Exception {
			Mockito.when(influxDao.isOnline()).thenReturn(false);

			thresholdChecker.checkThreshold(alertingState);

			Mockito.verify(influxDao).isOnline();
			Mockito.verifyNoMoreInteractions(influxDao);
			Mockito.verifyZeroInteractions(stateManager);
			Mockito.verifyZeroInteractions(alertingState);
		}

	}
}
