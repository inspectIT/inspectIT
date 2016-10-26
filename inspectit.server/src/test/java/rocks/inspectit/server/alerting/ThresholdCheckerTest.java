package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
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
@SuppressWarnings("PMD")
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
			when(queryResult.getResults()).thenReturn(Arrays.asList(result));
		}

		@Test
		public void noData() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState).setLastCheckTime(timeCaptor.capture());
			verify(alertingState).getAlertingDefinition();
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).noData(alertingState);
			verify(alertingDefinition).getThresholdType();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void noViolationUpperThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(15D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState).setLastCheckTime(timeCaptor.capture());
			verify(alertingState, times(2)).getAlertingDefinition();
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).valid(alertingState);
			verify(alertingDefinition, times(2)).getThresholdType();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void noViolationLowerThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(5D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState).setLastCheckTime(timeCaptor.capture());
			verify(alertingState, times(2)).getAlertingDefinition();
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).valid(alertingState);
			verify(alertingDefinition, times(2)).getThresholdType();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void violationUpperThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(5D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState).setLastCheckTime(timeCaptor.capture());
			verify(alertingState, times(2)).getAlertingDefinition();
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).violation(alertingState, 10D);
			verify(alertingDefinition, times(2)).getThresholdType();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void violationLowerThreshold() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(queryResult);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			when(alertingDefinition.getThreshold()).thenReturn(15D);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState).setLastCheckTime(timeCaptor.capture());
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState, times(2)).getAlertingDefinition();
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).violation(alertingState, 10D);
			verify(alertingDefinition, times(2)).getThresholdType();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void neverChecked() throws BusinessException, Exception {
			long time = System.currentTimeMillis();
			when(influxDao.isConnected()).thenReturn(true);
			when(influxDao.query(any(String.class))).thenReturn(new QueryResult());
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getLastCheckTime()).thenReturn(-1L);

			thresholdChecker.checkThreshold(alertingState);

			ArgumentCaptor<Long> currentTimeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(alertingState, times(2)).getLastCheckTime();
			verify(alertingState, times(2)).setLastCheckTime(currentTimeCaptor.capture());
			verify(alertingState, times(2)).getAlertingDefinition();
			assertThat(currentTimeCaptor.getValue(), greaterThanOrEqualTo(time));
			verify(alertingDefinition, times(2)).getTimeRange(TimeUnit.MILLISECONDS);
			verify(influxDao).query(any(String.class));
			verify(influxDao).isConnected();
			verify(stateManager).noData(alertingState);
			verify(alertingDefinition).getThresholdType();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getTags();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition, times(2)).getTimeRange(TimeUnit.MILLISECONDS);
			verifyNoMoreInteractions(influxDao, alertingState, stateManager, alertingDefinition);
		}

		@Test
		public void influxDisconnected() throws BusinessException, Exception {
			when(influxDao.isConnected()).thenReturn(false);

			thresholdChecker.checkThreshold(alertingState);

			verify(influxDao).isConnected();
			verifyNoMoreInteractions(influxDao);
			verifyZeroInteractions(stateManager, alertingState);
		}

	}
}
