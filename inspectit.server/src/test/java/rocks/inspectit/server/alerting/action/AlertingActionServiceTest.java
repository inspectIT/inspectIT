package rocks.inspectit.server.alerting.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Matchers.any;

import java.util.Iterator;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.AlertRegistry;
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
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AlertingActionServiceTest extends TestBase {

	@InjectMocks
	AlertingActionService alertingService;

	@Mock
	Logger log;

	@Mock
	AlertRegistry businessTransactionsAlertRegistry;

	@Mock
	List<IAlertAction> alertActions;

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
		@SuppressWarnings("unchecked")
		public void startAlert() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			AlertingDefinition alertingDefinition = Mockito.mock(AlertingDefinition.class);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(1234L);

			alertingService.alertStarting(alertingState, 1.0D);

			ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
			Mockito.verify(businessTransactionsAlertRegistry).registerAlert(captor.capture());
			Mockito.verifyNoMoreInteractions(businessTransactionsAlertRegistry);
			assertThat(captor.getValue().getAlertingDefinition(), is(alertingDefinition));
			assertThat(captor.getValue().getStartTimestamp(), is(1234L));
			Mockito.verify(alertAction).onStarting(alertingState);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verify(alertingState).getLastCheckTime();
			Mockito.verify(alertingState).setAlert(any(Alert.class));
			Mockito.verify(alertingState).setExtremeValue(1.0D);
			Mockito.verifyNoMoreInteractions(alertingState);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() {
			alertingService.alertStarting(null, 1.0D);
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
		@SuppressWarnings("unchecked")
		public void upperThresholdNewExtremeValue() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			AlertingDefinition alertingDefinition = Mockito.mock(AlertingDefinition.class);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(1234L);
			Mockito.when(alertingState.getExtremeValue()).thenReturn(5D);

			alertingService.alertOngoing(alertingState, 10D);

			Mockito.verify(alertingState).setExtremeValue(10D);
			Mockito.verify(alertingState).getExtremeValue();
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertAction).onOngoing(alertingState);
			Mockito.verifyNoMoreInteractions(alertAction);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verifyZeroInteractions(businessTransactionsAlertRegistry);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void upperThreshold() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			AlertingDefinition alertingDefinition = Mockito.mock(AlertingDefinition.class);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.UPPER_THRESHOLD);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(1234L);
			Mockito.when(alertingState.getExtremeValue()).thenReturn(5D);

			alertingService.alertOngoing(alertingState, 3D);

			Mockito.verify(alertingState).setExtremeValue(5D);
			Mockito.verify(alertingState).getExtremeValue();
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertAction).onOngoing(alertingState);
			Mockito.verifyNoMoreInteractions(alertAction);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verifyZeroInteractions(businessTransactionsAlertRegistry);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void lowerThresholdNewExtremeValue() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			AlertingDefinition alertingDefinition = Mockito.mock(AlertingDefinition.class);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(1234L);
			Mockito.when(alertingState.getExtremeValue()).thenReturn(5D);

			alertingService.alertOngoing(alertingState, 3D);

			Mockito.verify(alertingState).setExtremeValue(3D);
			Mockito.verify(alertingState).getExtremeValue();
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertAction).onOngoing(alertingState);
			Mockito.verifyNoMoreInteractions(alertAction);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verifyZeroInteractions(businessTransactionsAlertRegistry);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void lowerThreshold() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			AlertingDefinition alertingDefinition = Mockito.mock(AlertingDefinition.class);
			Mockito.when(alertingDefinition.getThresholdType()).thenReturn(ThresholdType.LOWER_THRESHOLD);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingState.getLastCheckTime()).thenReturn(1234L);
			Mockito.when(alertingState.getExtremeValue()).thenReturn(5D);

			alertingService.alertOngoing(alertingState, 10D);

			Mockito.verify(alertingState).setExtremeValue(5D);
			Mockito.verify(alertingState).getExtremeValue();
			Mockito.verify(alertingState).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertAction).onOngoing(alertingState);
			Mockito.verifyNoMoreInteractions(alertAction);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verifyZeroInteractions(businessTransactionsAlertRegistry);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() {
			alertingService.alertOngoing(null, 1.0D);
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
		@SuppressWarnings("unchecked")
		public void alertEnded() {
			IAlertAction alertAction = Mockito.mock(IAlertAction.class);
			Iterator<IAlertAction> iterator = Mockito.mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(alertAction);
			Mockito.when(alertActions.iterator()).thenReturn(iterator);
			Alert alert = Mockito.mock(Alert.class);
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.getAlert()).thenReturn(alert);

			long leftBorder = System.currentTimeMillis();
			alertingService.alertEnding(alertingState);
			long rightBorder = System.currentTimeMillis();

			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			ArgumentCaptor<AlertClosingReason> closingReasonCaptor = ArgumentCaptor.forClass(AlertClosingReason.class);
			Mockito.verify(alert).close(timeCaptor.capture(), closingReasonCaptor.capture());
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(leftBorder));
			assertThat(timeCaptor.getValue(), lessThanOrEqualTo(rightBorder));
			assertThat(closingReasonCaptor.getValue(), is(AlertClosingReason.ALERT_RESOLVED));
			Mockito.verifyNoMoreInteractions(alert);
			Mockito.verify(alertingState).getAlert();
			Mockito.verify(alertingState).setAlert(null);
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertAction).onEnding(alertingState);
			Mockito.verifyNoMoreInteractions(alertAction);
			Mockito.verify(alertActions).iterator();
			Mockito.verifyNoMoreInteractions(alertActions);
			Mockito.verifyZeroInteractions(businessTransactionsAlertRegistry);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() {
			alertingService.alertEnding(null);
		}
	}
}
