package rocks.inspectit.server.alerting;

import static org.mockito.Mockito.times;

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
			Mockito.when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.violation(alertingState, violationValue);

			Mockito.verify(alertingState).isAlertActive();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertingActionService).alertStarting(alertingState, violationValue);
			Mockito.verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertOngoing() {
			double violationValue = 100D;
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);

			lifecycleManager.violation(alertingState, violationValue);

			Mockito.verify(alertingState).isAlertActive();
			Mockito.verify(alertingState).setValidCount(0);
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertingActionService).alertOngoing(alertingState, violationValue);
			Mockito.verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.violation(null, 0);

			Mockito.verifyZeroInteractions(alertingActionService);
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
			Mockito.when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.valid(alertingState);

			Mockito.verify(alertingState).isAlertActive();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void alertActiveNoReset() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);
			Mockito.when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.valid(alertingState);

			Mockito.verify(alertingState).isAlertActive();
			Mockito.verify(alertingState).getValidCount();
			Mockito.verify(alertingState).setValidCount(2);
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void resetActiveAlert() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);
			Mockito.when(alertingState.getValidCount()).thenReturn(2);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.valid(alertingState);

			Mockito.verify(alertingState).isAlertActive();
			Mockito.verify(alertingState).getValidCount();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertingActionService).alertEnding(alertingState);
			Mockito.verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.valid(null);

			Mockito.verifyZeroInteractions(alertingActionService);
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
			Mockito.when(alertingState.isAlertActive()).thenReturn(false);

			lifecycleManager.noData(alertingState);

			Mockito.verifyZeroInteractions(alertingActionService);
			Mockito.verify(alertingState, times(2)).isAlertActive();
			Mockito.verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void noAlertActiveCountPositive() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(false);
			Mockito.when(alertingState.getValidCount()).thenReturn(10);

			lifecycleManager.noData(alertingState);

			Mockito.verifyZeroInteractions(alertingActionService);
			Mockito.verify(alertingState, times(2)).isAlertActive();
			Mockito.verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void alertActiveContinuousViolation() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);
			Mockito.when(alertingState.getValidCount()).thenReturn(0);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			Mockito.verify(alertingActionService, times(1)).alertOngoing(alertingState, Double.NaN);
			Mockito.verifyNoMoreInteractions(alertingActionService);
			Mockito.verify(alertingState, times(2)).isAlertActive();
			Mockito.verify(alertingState).setValidCount(0);
			Mockito.verify(alertingState).getValidCount();
			Mockito.verifyNoMoreInteractions(alertingState);
		}

		@Test
		public void alertActiveDataWasValid() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);
			Mockito.when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.noData(alertingState);

			Mockito.verify(alertingState, times(2)).isAlertActive();
			Mockito.verify(alertingState, times(2)).getValidCount();
			Mockito.verify(alertingState).setValidCount(2);
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verifyZeroInteractions(alertingActionService);
		}

		@Test
		public void resetActiveAlert() {
			AlertingState alertingState = Mockito.mock(AlertingState.class);
			Mockito.when(alertingState.isAlertActive()).thenReturn(true);
			Mockito.when(alertingState.getValidCount()).thenReturn(1);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			Mockito.verify(alertingState, times(2)).isAlertActive();
			Mockito.verify(alertingState, times(2)).getValidCount();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(alertingActionService).alertEnding(alertingState);
			Mockito.verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.noData(null);

			Mockito.verifyZeroInteractions(alertingActionService);
		}
	}
}
