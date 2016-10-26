package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent.AlertingDefinitionCreatedEvent;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent.AlertingDefinitionDeletedEvent;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent.AlertingDefinitionLoadedEvent;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent.AlertingDefinitionUpdateEvent;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.shared.cs.communication.data.cmr.AlertClosingReason;

/**
 * Tests for the {@link AlertingScheduler}.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AlertingSchedulerTest extends TestBase {

	@InjectMocks
	AlertingScheduler alertingScheduler;

	@Mock
	Logger logger;

	@Mock
	ThresholdChecker thresholdChecker;

	@Mock
	ScheduledExecutorService executorService;

	/**
	 * Test the {@link AlertingScheduler#updateState()} method.
	 */
	public static class UpdateState extends AlertingSchedulerTest {

		@Test
		public void deactivate() {
			alertingScheduler.active = false;

			alertingScheduler.updateState();

			verifyZeroInteractions(thresholdChecker);
			verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activate() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;

			alertingScheduler.updateState();

			verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
			verifyZeroInteractions(thresholdChecker);
			verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activateWhenActive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			when(future.isDone()).thenReturn(false);
			when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
			verify(future).isDone();
			verifyNoMoreInteractions(future);
			verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activateWhenInactive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			when(future.isDone()).thenReturn(true);
			when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			verify(executorService, times(2)).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
			verify(future).isDone();
			verifyNoMoreInteractions(future);
			verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void deactivateWhenActive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			when(future.isDone()).thenReturn(false);
			when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();
			alertingScheduler.active = false;

			alertingScheduler.updateState();

			verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
			verify(future).isDone();
			verify(future).cancel(false);
			verifyNoMoreInteractions(future);
			verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void deactivateWhenInactive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			when(future.isDone()).thenReturn(false, true);
			when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();
			alertingScheduler.active = false;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
			verify(future, times(2)).isDone();
			verify(future).cancel(false);
			verifyNoMoreInteractions(future);
			verifyZeroInteractions(thresholdChecker);
		}
	}

	/**
	 * Test the {@link AlertingScheduler#run()} method.
	 */
	public static class Run extends AlertingSchedulerTest {

		@Test
		public void checkExistingAlertingStates() throws Exception {
			AlertingDefinition definitionOne = mock(AlertingDefinition.class);
			AlertingDefinition definitionTwo = mock(AlertingDefinition.class);
			when(definitionOne.getTimeRange(any(TimeUnit.class))).thenReturn(1L);
			when(definitionTwo.getTimeRange(any(TimeUnit.class))).thenReturn(3600000L);
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionTwo));
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					((AlertingState) invocation.getArguments()[0]).setLastCheckTime(System.currentTimeMillis());
					return null;
				}
			}).when(thresholdChecker).checkThreshold(any(AlertingState.class));

			alertingScheduler.run(); // both are checked
			Thread.sleep(10);
			alertingScheduler.run(); // only first is checked

			ArgumentCaptor<AlertingState> stateCaptor = ArgumentCaptor.forClass(AlertingState.class);
			verify(thresholdChecker, times(3)).checkThreshold(stateCaptor.capture());
			verifyNoMoreInteractions(thresholdChecker);
			verifyZeroInteractions(executorService);
			assertThat(stateCaptor.getAllValues().get(0).getAlertingDefinition(), equalTo(definitionOne));
			assertThat(stateCaptor.getAllValues().get(1).getAlertingDefinition(), equalTo(definitionTwo));
			assertThat(stateCaptor.getAllValues().get(2).getAlertingDefinition(), equalTo(definitionOne));
		}

		@Test
		public void noAlertingStates() throws Exception {
			alertingScheduler.run();

			verifyZeroInteractions(thresholdChecker);
			verifyZeroInteractions(executorService);
		}

		@Test
		public void thresholdCheckerThrowsException() throws Exception {
			AlertingDefinition definitionOne = mock(AlertingDefinition.class);
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			doThrow(RuntimeException.class).when(thresholdChecker).checkThreshold(any(AlertingState.class));

			alertingScheduler.run();

			verify(thresholdChecker).checkThreshold(any(AlertingState.class));
			verifyNoMoreInteractions(thresholdChecker);
			verifyZeroInteractions(executorService);
		}
	}

	/**
	 * Test the
	 * {@link AlertingScheduler#onApplicationEvent(rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent)}
	 * method.
	 *
	 */
	public static class OnApplicationEvent extends AlertingSchedulerTest {

		@Mock
		AlertingDefinition definitionOne;

		@Mock
		AlertingDefinition definitionTwo;

		@SuppressWarnings("unchecked")
		private List<AlertingState> getAlertingStates() {
			try {
				Field field = AlertingScheduler.class.getDeclaredField("alertingStates");
				field.setAccessible(true);
				return (List<AlertingState>) field.get(alertingScheduler);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Test
		public void nullEvent() {
			alertingScheduler.onApplicationEvent(null);

			verifyZeroInteractions(thresholdChecker, executorService);
		}

		@Test
		public void loadingEvent() {
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionLoadedEvent(this, Arrays.asList(definitionOne, definitionTwo));

			alertingScheduler.onApplicationEvent(event);

			verifyZeroInteractions(thresholdChecker, executorService, definitionOne, definitionTwo);
			assertThat(getAlertingStates(), hasSize(2));
			assertThat(getAlertingStates().get(0).getAlertingDefinition(), equalTo(definitionOne));
			assertThat(getAlertingStates().get(1).getAlertingDefinition(), equalTo(definitionTwo));
		}

		@Test
		public void createEvent() {
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionCreatedEvent(this, definitionOne);

			alertingScheduler.onApplicationEvent(event);

			verifyZeroInteractions(thresholdChecker, executorService, definitionOne);
			assertThat(getAlertingStates(), hasSize(1));
			assertThat(getAlertingStates().get(0).getAlertingDefinition(), equalTo(definitionOne));
		}

		@Test
		public void deletedEvent() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			assertThat(getAlertingStates(), hasSize(1));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionDeletedEvent(this, definitionOne);
			when(definitionOne.getId()).thenReturn("id");

			alertingScheduler.onApplicationEvent(event);

			verify(definitionOne, times(2)).getId();
			verifyNoMoreInteractions(definitionOne);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(0));
		}

		@Test
		public void deletedEventAlertActive() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			assertThat(getAlertingStates(), hasSize(1));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionDeletedEvent(this, definitionOne);
			when(definitionOne.getId()).thenReturn("id");
			Alert alertMock = mock(Alert.class);
			// set manually because it would done by the threshold checker which is also mocked
			getAlertingStates().get(0).setAlert(alertMock);

			alertingScheduler.onApplicationEvent(event);

			ArgumentCaptor<AlertClosingReason> reasonCapture = ArgumentCaptor.forClass(AlertClosingReason.class);
			verify(alertMock, times(1)).close(any(Long.class), reasonCapture.capture());
			verify(definitionOne, times(2)).getId();
			verifyNoMoreInteractions(definitionOne);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(0));
			assertThat(reasonCapture.getValue(), equalTo(AlertClosingReason.ALERTING_DEFINITION_DELETED));
		}

		@Test
		public void deletedUnknownEvent() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			assertThat(getAlertingStates(), hasSize(1));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionDeletedEvent(this, definitionTwo);
			when(definitionOne.getId()).thenReturn("id");

			alertingScheduler.onApplicationEvent(event);

			verify(definitionOne).getId();
			verify(definitionTwo).getId();
			verifyNoMoreInteractions(definitionOne, definitionTwo);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(1));
		}

		@Test
		public void updateEvent() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			assertThat(getAlertingStates().get(0).getAlertingDefinition(), equalTo(definitionOne));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionUpdateEvent(this, definitionTwo);
			when(definitionOne.getId()).thenReturn("id");
			when(definitionTwo.getId()).thenReturn("id");

			alertingScheduler.onApplicationEvent(event);

			verify(definitionOne).getId();
			verify(definitionTwo).getId();
			verifyNoMoreInteractions(definitionOne, definitionTwo);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(1));
			assertThat(getAlertingStates().get(0).getAlertingDefinition(), equalTo(definitionTwo));
		}

		@Test
		public void updateEventAlertActive() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionUpdateEvent(this, definitionOne);
			when(definitionOne.getId()).thenReturn("id");
			Alert alertMock = mock(Alert.class);
			// set manually because it would done by the threshold checker which is also mocked
			getAlertingStates().get(0).setAlert(alertMock);

			alertingScheduler.onApplicationEvent(event);


			verify(alertMock, times(1)).setAlertingDefinition(definitionOne);
			verify(definitionOne, times(2)).getId();
			verifyNoMoreInteractions(definitionOne);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(1));
		}

		@Test
		public void updateUnknownEvent() {
			alertingScheduler.onApplicationEvent(new AlertingDefinitionCreatedEvent(this, definitionOne));
			AbstractAlertingDefinitionEvent event = new AlertingDefinitionUpdateEvent(this, definitionTwo);
			when(definitionOne.getId()).thenReturn("id");
			when(definitionTwo.getId()).thenReturn("id_2");

			alertingScheduler.onApplicationEvent(event);

			assertThat(getAlertingStates(), hasSize(1));
			assertThat(getAlertingStates().get(0).getAlertingDefinition(), equalTo(definitionOne));
			verify(definitionOne).getId();
			verify(definitionTwo).getId();
			verifyNoMoreInteractions(definitionOne, definitionTwo);
			verifyZeroInteractions(thresholdChecker, executorService);
			assertThat(getAlertingStates(), hasSize(1));
		}
	}
}
