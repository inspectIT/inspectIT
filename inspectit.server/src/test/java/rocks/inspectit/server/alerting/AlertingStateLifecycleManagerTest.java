package rocks.inspectit.server.alerting;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.action.AlertingActionService;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AlertingStateLifecycleManager}.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AlertingStateLifecycleManagerTest extends TestBase {

	@InjectMocks
	AlertingStateLifecycleManager lifecycleManager;

	@Mock
	Logger log;

	@Mock
	AlertingActionService alertingActionService;

	/**
	 * Tests the {@link AlertingStateLifecycleManager#violation(AlertingState, double)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Violation extends AlertingStateLifecycleManagerTest {

		@Test
		public void alertStarted() {
			double violationValue = 100D;
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.violation(alertingState, violationValue);

			verify(alertingState).isAlertActive();
			verifyNoMoreInteractions(alertingState);
			verify(alertingActionService).alertStarting(alertingState, violationValue);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertOngoing() {
			double violationValue = 100D;
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);

			lifecycleManager.violation(alertingState, violationValue);

			verify(alertingState).isAlertActive();
			verify(alertingState).setValidCount(0);
			verifyNoMoreInteractions(alertingState);
			verify(alertingActionService).alertOngoing(alertingState, violationValue);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.violation(null, 0);

			verifyZeroInteractions(alertingActionService);
		}
	}

	/**
	 * Tests the {@link AlertingStateLifecycleManager#valid(AlertingState)} method.
	 *
	 * @author Alexander Wert
	 * @author Marius Oehler
	 *
	 */
	public static class Valid extends AlertingStateLifecycleManagerTest {

		@Test
		public void noAlertActive() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.valid(alertingState);

			verify(alertingState).isAlertActive();
			verifyNoMoreInteractions(alertingState);
			verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void alertActiveNoReset() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);
			when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.valid(alertingState);

			verify(alertingState).isAlertActive();
			verify(alertingState).getValidCount();
			verify(alertingState).setValidCount(2);
			verifyNoMoreInteractions(alertingState);
			verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void resetActiveAlert() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);
			when(alertingState.getValidCount()).thenReturn(2);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.valid(alertingState);

			verify(alertingState).isAlertActive();
			verify(alertingState).getValidCount();
			verifyNoMoreInteractions(alertingState);
			verify(alertingActionService).alertEnding(alertingState);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.valid(null);

			verifyZeroInteractions(alertingActionService);
		}
	}

	/**
	 * Tests the {@link AlertingStateLifecycleManager#noData(AlertingState)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class NoData extends AlertingStateLifecycleManagerTest {

		@Test
		public void noAlertActive() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.noData(alertingState);

			verifyZeroInteractions(alertingActionService);
			verify(alertingState, times(2)).isAlertActive();
			verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void noAlertActiveCountPositive() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(false);
			when(alertingState.getValidCount()).thenReturn(10);

			lifecycleManager.noData(alertingState);

			verifyZeroInteractions(alertingActionService);
			verify(alertingState, times(2)).isAlertActive();
			verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void alertActiveContinuousViolation() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);
			when(alertingState.getValidCount()).thenReturn(0);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			verify(alertingActionService, times(1)).alertOngoing(alertingState, Double.NaN);
			verifyNoMoreInteractions(alertingActionService);
			verify(alertingState, times(2)).isAlertActive();
			verify(alertingState).setValidCount(0);
			verify(alertingState).getValidCount();
			verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void alertActiveDataWasValid() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);
			when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.noData(alertingState);

			verify(alertingState, times(2)).isAlertActive();
			verify(alertingState, times(2)).getValidCount();
			verify(alertingState).setValidCount(2);
			verifyNoMoreInteractions(alertingState);
			verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void resetActiveAlert() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			when(alertingState.isAlertActive()).thenReturn(true);
			when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			verify(alertingState, times(2)).isAlertActive();
			verify(alertingState, times(2)).getValidCount();
			verifyNoMoreInteractions(alertingState);
			verify(alertingActionService).alertEnding(alertingState);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.noData(null);

			verifyZeroInteractions(alertingActionService);
		}
	}
}
