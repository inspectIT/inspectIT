package rocks.inspectit.server.alerting;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.action.AlertingActionService;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Tests the {@link AlertingStateLifecycleManager}.
 *
 * @author Alexander Wert
 *
 */
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
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(null);
			double value = 777.7;

			lifecycleManager.violation(alertingState, value);

			verify(alertingActionService, times(1)).alertStarting(alertingState, value);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertOngoing() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(new Alert(null, 0));
			double value = 777.7;

			lifecycleManager.violation(alertingState, value);

			verify(alertingActionService, times(1)).alertOngoing(alertingState, value);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.violation(null, 0);

			verifyNoMoreInteractions(alertingActionService);
		}
	}

	/**
	 * Tests the {@link AlertingStateLifecycleManager#valid(AlertingState)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Valid extends AlertingStateLifecycleManagerTest {

		@Test
		public void noAlertActive() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(null);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.valid(alertingState);

			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertActive() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(new Alert(null, 0));
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.valid(alertingState);

			verify(alertingActionService, times(1)).alertEnding(alertingState);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertActiveWaitForCounterDecrement() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(new Alert(null, 0));
			lifecycleManager.thresholdResetCount = 2;

			lifecycleManager.valid(alertingState);

			verifyNoMoreInteractions(alertingActionService);

			lifecycleManager.valid(alertingState);

			verifyNoMoreInteractions(alertingActionService);

			lifecycleManager.valid(alertingState);

			verify(alertingActionService, times(1)).alertEnding(alertingState);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.valid(null);

			verifyNoMoreInteractions(alertingActionService);
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
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(null);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void noAlertActiveValidCountPositive() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setValidCount(1);
			alertingState.setAlert(null);
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertActive() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(new Alert(null, 0));
			lifecycleManager.thresholdResetCount = 0;

			lifecycleManager.noData(alertingState);

			verify(alertingActionService, times(1)).alertOngoing(any(AlertingState.class), any(Double.class));
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertActiveValidCountPositive() {
			AlertingState alertingState = new AlertingState(new AlertingDefinition());
			alertingState.setAlert(new Alert(null, 0));
			alertingState.setValidCount(1);
			lifecycleManager.thresholdResetCount = 0;
			AlertingStateLifecycleManager lifecycleManagerSpy = spy(lifecycleManager);
			doNothing().when(lifecycleManagerSpy).valid(alertingState);

			lifecycleManagerSpy.noData(alertingState);

			verify(lifecycleManagerSpy, times(1)).valid(alertingState);
			verifyNoMoreInteractions(alertingActionService);
		}

		@Test
		public void alertingStateNull() {
			lifecycleManager.noData(null);

			verifyNoMoreInteractions(alertingActionService);
		}
	}
}
