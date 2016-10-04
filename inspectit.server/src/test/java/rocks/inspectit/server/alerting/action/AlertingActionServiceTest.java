package rocks.inspectit.server.alerting.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.AlertRegistry;
import rocks.inspectit.server.alerting.action.impl.EmailAlertAction;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Tests the {@link AlertingActionService}.
 *
 * @author Alexander Wert
 *
 */
public class AlertingActionServiceTest extends TestBase {
	@InjectMocks
	AlertingActionService alertingService;

	EmailAlertAction emailAction = Mockito.mock(EmailAlertAction.class);

	@Mock
	Logger log;

	@Mock
	AlertRegistry businessTransactionsAlertRegistry;

	@Spy
	List<IAlertAction> actions = new ArrayList<>(Collections.singletonList((IAlertAction) emailAction));

	/**
	 * Tesets the
	 * {@link AlertingActionService#alertStarting(rocks.inspectit.server.alerting.state.AlertingState, double)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertStarted extends AlertingActionServiceTest {

		@Test
		public void businessTransactionAlert() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.setThreshold(1);
			AlertingState alertingState = new AlertingState(alertingDefinition);
			double violationValue = 2;

			alertingService.alertStarting(alertingState, violationValue);

			verify(businessTransactionsAlertRegistry, times(1)).registerAlert(any(Alert.class));
			verify(emailAction, times(1)).onStarting(alertingState);
		}

		@Test
		public void noBusinessTransactionAlert() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("measurement");
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.setThreshold(1);
			AlertingState alertingState = new AlertingState(alertingDefinition);
			double violationValue = 2;

			alertingService.alertStarting(alertingState, violationValue);

			verify(emailAction, times(1)).onStarting(alertingState);
		}
	}

	/**
	 * Tests the
	 * {@link AlertingActionService#alertOngoing(rocks.inspectit.server.alerting.state.AlertingState)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertOngoing extends AlertingActionServiceTest {
		@Test
		public void upperThreshold() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("test");
			alertingDefinition.setField("test");
			alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
			alertingDefinition.setThreshold(1);
			AlertingState alertingState = new AlertingState(alertingDefinition);
			double valueBefore = 1;
			alertingState.setExtremeValue(valueBefore);
			double violationValue = 2;

			alertingService.alertOngoing(alertingState, violationValue);

			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onOngoing(alertingState);
			assertThat(alertingState.getExtremeValue(), greaterThan(valueBefore));
		}

		@Test
		public void lowerThreshold() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("test");
			alertingDefinition.setField("test");
			alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
			alertingDefinition.setThreshold(1);
			AlertingState alertingState = new AlertingState(alertingDefinition);
			double valueBefore = 1;
			alertingState.setExtremeValue(valueBefore);
			double violationValue = 0;

			alertingService.alertOngoing(alertingState, violationValue);

			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onOngoing(alertingState);
			assertThat(alertingState.getExtremeValue(), lessThan(valueBefore));
		}
	}

	/**
	 * Tesets the
	 * {@link AlertingActionService#alertEnding(rocks.inspectit.server.alerting.state.AlertingState)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class AlertEnding extends AlertingActionServiceTest {
		@Test
		public void alertEnded() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("test");
			alertingDefinition.setField("test");
			alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
			alertingDefinition.setThreshold(1);
			AlertingState alertingState = new AlertingState(alertingDefinition);
			Alert alert = new Alert(alertingDefinition, 0);
			alert.setStopTimestamp(0);
			alertingState.setAlert(alert);

			alertingService.alertEnding(alertingState);

			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onEnding(alertingState);
			assertThat(alert.getStopTimestamp(), greaterThan(0L));
		}
	}
}
