package rocks.inspectit.server.alerting.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.AlertRegistry;
import rocks.inspectit.server.alerting.action.impl.EmailAlertAction;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.shared.cs.communication.data.cmr.AlertClosingReason;

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

	@Mock
	AlertingDefinition alertingDefinition;

	@Mock
	AlertingState alertingState;

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
			long startTime = 123456789;
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getLastCheckTime()).thenReturn(startTime);

			alertingService.alertStarting(alertingState, 1.0);

			ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
			verify(businessTransactionsAlertRegistry, times(1)).registerAlert(captor.capture());
			assertThat(captor.getValue().getAlertingDefinition(), is(alertingDefinition));
			assertThat(captor.getValue().getStartTimestamp(), is(startTime));
			verify(emailAction, times(1)).onStarting(alertingState);
			verify(alertingState, times(1)).setAlert(any(Alert.class));
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
			double valueBefore = 1;
			double violationValue = 2;
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getExtremeValue()).thenReturn(valueBefore);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);

			alertingService.alertOngoing(alertingState, violationValue);

			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onOngoing(alertingState);
			verify(alertingState, times(1)).setExtremeValue(violationValue);
		}

		@Test
		public void lowerThreshold() {
			double valueBefore = 1;
			double violationValue = 0;
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getExtremeValue()).thenReturn(valueBefore);
			when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);

			alertingService.alertOngoing(alertingState, violationValue);

			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onOngoing(alertingState);
			verify(alertingState, times(1)).setExtremeValue(violationValue);
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
		@Mock
		Alert alert;

		@Test
		public void alertEnded() {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);

			long leftBorder = System.currentTimeMillis();
			alertingService.alertEnding(alertingState);
			long rightBorder = System.currentTimeMillis();

			ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
			ArgumentCaptor<AlertClosingReason> closeReasonCaptor = ArgumentCaptor.forClass(AlertClosingReason.class);
			verify(alert, times(1)).close(captor.capture(), closeReasonCaptor.capture());
			assertThat(leftBorder, lessThanOrEqualTo(captor.getValue()));
			assertThat(rightBorder, greaterThanOrEqualTo(captor.getValue()));
			assertThat(AlertClosingReason.ALERT_RESOLVED, is(closeReasonCaptor.getValue()));
			verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			verify(emailAction, times(1)).onEnding(alertingState);
		}
	}
}
