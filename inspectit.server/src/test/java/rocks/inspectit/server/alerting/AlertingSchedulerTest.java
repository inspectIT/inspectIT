package rocks.inspectit.server.alerting;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Tests for the {@link AlertingScheduler}.
 *
 * @author Marius Oehler
 *
 */
public class AlertingSchedulerTest extends TestBase {

	@InjectMocks
	AlertingScheduler alertingScheduler;

	@Mock
	Logger logger;

	@Mock
	ScheduledExecutorService executorService;

	@Mock
	ScheduledFuture<?> scheduledFuture;

	@Mock
	ThresholdChecker thresholdChecker;

	AlertingDefinition firstAlertingDefinition;
	AlertingDefinition secondAlertingDefinition;
	AlertingState firstAlertingState;
	AlertingState secondAlertingState;

	List<AlertingState> alertingDefinitions;

	/**
	 * Initializes the used {@link AlertingDefinition} and return values of the mocks.
	 */
	@BeforeMethod
	public void init() {
		firstAlertingDefinition = new AlertingDefinition();
		firstAlertingDefinition.setId("first");
		firstAlertingDefinition.setName("first");
		firstAlertingDefinition.setTimeRange(1L, TimeUnit.MINUTES);

		secondAlertingDefinition = new AlertingDefinition();
		secondAlertingDefinition.setId("second");
		secondAlertingDefinition.setName("second");
		secondAlertingDefinition.setTimeRange(2L, TimeUnit.MINUTES);

		firstAlertingState = new AlertingState(firstAlertingDefinition);
		secondAlertingState = new AlertingState(secondAlertingDefinition);

		alertingDefinitions = Arrays.asList(firstAlertingState, secondAlertingState);
	}

	/**
	 * Test the {@link AlertingScheduler#run()} method.
	 */
	public static class Run extends AlertingSchedulerTest {
		@Test
		public void runThresholdCheck() throws Exception {
			Field f = AlertingScheduler.class.getDeclaredField("alertingStates");
			f.setAccessible(true);
			f.set(alertingScheduler, alertingDefinitions);

			alertingScheduler.run(); // initial run

			verify(thresholdChecker, times(1)).checkThreshold(firstAlertingState);
		}
	}

	/**
	 * Test the {@link AlertingScheduler#updateState()} method.
	 */
	public static class UpdateState extends AlertingSchedulerTest {
		@SuppressWarnings("unchecked")
		@Test
		public void setActiveOnce() throws Exception {
			when(executorService.scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));
			when(scheduledFuture.isDone()).thenReturn(true);

			Field iterationCounterField = AlertingScheduler.class.getDeclaredField("active");
			iterationCounterField.setAccessible(true);

			iterationCounterField.set(alertingScheduler, false);
			alertingScheduler.updateState();

			iterationCounterField.set(alertingScheduler, true);
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			verify(executorService, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), any(TimeUnit.class));
		}

		@Test
		public void disableScheduler() throws Exception {
			Field iterationCounterField = AlertingScheduler.class.getDeclaredField("active");
			iterationCounterField.setAccessible(true);

			iterationCounterField.set(alertingScheduler, true);
			alertingScheduler.updateState();

			iterationCounterField.set(alertingScheduler, false);
			alertingScheduler.updateState();

			verify(scheduledFuture, times(1)).cancel(any(Boolean.class));
		}

		@Test
		public void disableWhenNull() throws Exception {
			Field iterationCounterField = AlertingScheduler.class.getDeclaredField("active");
			iterationCounterField.setAccessible(true);
			iterationCounterField.set(alertingScheduler, false);

			alertingScheduler.updateState();

			alertingScheduler.updateState();
		}

		@Test
		public void disableWhenDisabledAndNull() throws Exception {
			Field iterationCounterField = AlertingScheduler.class.getDeclaredField("active");
			iterationCounterField.setAccessible(true);
			iterationCounterField.set(alertingScheduler, false);

			Field scheduledFutureField = AlertingScheduler.class.getDeclaredField("scheduledFuture");
			scheduledFutureField.setAccessible(true);
			scheduledFutureField.set(alertingScheduler, null);

			alertingScheduler.updateState();
		}
	}
}
