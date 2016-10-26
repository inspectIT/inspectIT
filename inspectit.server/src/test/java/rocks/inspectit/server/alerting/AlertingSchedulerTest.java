package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent;
import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent.AlertDefinitionEventType;
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

			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activate() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			Mockito.when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;

			alertingScheduler.updateState();

			Mockito.verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activateWhenActive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			Mockito.when(future.isDone()).thenReturn(false);
			Mockito.when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			Mockito.verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(future).isDone();
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void activateWhenInactive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			Mockito.when(future.isDone()).thenReturn(true);
			Mockito.when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			Mockito.verify(executorService, times(2)).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(future).isDone();
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void deactivateWhenActive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			Mockito.when(future.isDone()).thenReturn(false);
			Mockito.when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();
			alertingScheduler.active = false;

			alertingScheduler.updateState();

			Mockito.verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(future).isDone();
			Mockito.verify(future).cancel(false);
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verifyZeroInteractions(thresholdChecker);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void deactivateWhenInactive() {
			ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
			Mockito.when(future.isDone()).thenReturn(false, true);
			Mockito.when(executorService.scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES)).thenReturn(future);
			alertingScheduler.active = true;
			alertingScheduler.updateState();
			alertingScheduler.active = false;
			alertingScheduler.updateState();

			alertingScheduler.updateState();

			Mockito.verify(executorService).scheduleAtFixedRate(alertingScheduler, 0L, AlertingScheduler.CHECK_INTERVAL, TimeUnit.MINUTES);
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(future, times(2)).isDone();
			Mockito.verify(future).cancel(false);
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verifyZeroInteractions(thresholdChecker);
		}
	}

	/**
	 * Test the {@link AlertingScheduler#run()} method.
	 */
	public static class Run extends AlertingSchedulerTest {

		@Mock
		List<AlertingState> alertingStates;

		@Test
		@SuppressWarnings("unchecked")
		public void checkExistingAlertingStates() throws Exception {
			AlertingState stateOne = mock(AlertingState.class);
			AlertingState stateTwo = mock(AlertingState.class);
			AlertingDefinition definitionOne = mock(AlertingDefinition.class);
			AlertingDefinition definitionTwo = mock(AlertingDefinition.class);
			Iterator<AlertingState> iterator = mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, true, false);
			Mockito.when(iterator.next()).thenReturn(stateOne, stateTwo);
			Mockito.when(alertingStates.iterator()).thenReturn(iterator);
			Mockito.when(definitionOne.getTimeRange(any(TimeUnit.class))).thenReturn(1L);
			Mockito.when(definitionTwo.getTimeRange(any(TimeUnit.class))).thenReturn(3600000L);
			Mockito.when(stateOne.getAlertingDefinition()).thenReturn(definitionOne);
			Mockito.when(stateTwo.getAlertingDefinition()).thenReturn(definitionTwo);
			Mockito.when(stateOne.getLastCheckTime()).thenReturn(System.currentTimeMillis() - 10L);
			Mockito.when(stateTwo.getLastCheckTime()).thenReturn(System.currentTimeMillis() - 10L);

			alertingScheduler.run();

			Mockito.verify(thresholdChecker, times(1)).checkThreshold(stateOne);
			Mockito.verify(thresholdChecker, times(0)).checkThreshold(stateTwo);
			Mockito.verifyNoMoreInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noAlertingStates() throws Exception {
			Iterator<AlertingState> iterator = mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(false);
			Mockito.when(alertingStates.iterator()).thenReturn(iterator);

			alertingScheduler.run();

			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noAlertingDefinition() throws Exception {
			AlertingState stateOne = mock(AlertingState.class);
			Iterator<AlertingState> iterator = mock(Iterator.class);
			Mockito.when(iterator.hasNext()).thenReturn(true, false);
			Mockito.when(iterator.next()).thenReturn(stateOne);
			Mockito.when(alertingStates.iterator()).thenReturn(iterator);
			Mockito.when(stateOne.getLastCheckTime()).thenReturn(System.currentTimeMillis() - 10L);

			alertingScheduler.run();

			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
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
		AbstractAlertingDefinitionEvent event;

		@Mock
		List<AlertingState> alertingStates;

		@Mock
		AlertingDefinition definitionOne;

		@Mock
		AlertingDefinition definitionTwo;

		@Test
		public void nullEvent() {
			alertingScheduler.onApplicationEvent(null);

			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
			Mockito.verifyZeroInteractions(alertingStates);
		}

		@Test
		public void loadingEvent() {
			ArgumentCaptor<AlertingState> stateCapture = ArgumentCaptor.forClass(AlertingState.class);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.LOADED);
			Mockito.when(event.getAlertingDefinitions()).thenReturn(Arrays.asList(definitionOne, definitionTwo));

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(alertingStates, times(1)).clear();
			Mockito.verify(alertingStates, times(2)).add(stateCapture.capture());
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
			assertThat(stateCapture.getAllValues().get(0).getAlertingDefinition(), equalTo(definitionOne));
			assertThat(stateCapture.getAllValues().get(1).getAlertingDefinition(), equalTo(definitionTwo));
		}

		@Test
		public void createEvent() {
			ArgumentCaptor<AlertingState> stateCapture = ArgumentCaptor.forClass(AlertingState.class);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.ADDED);
			Mockito.when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(alertingStates, times(1)).add(stateCapture.capture());
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
			assertThat(stateCapture.getValue().getAlertingDefinition(), equalTo(definitionOne));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedEvent() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			Mockito.when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verify(iteratorMock).remove();
			Mockito.verifyNoMoreInteractions(iteratorMock);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedEventAlertActive() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			Alert alertMock = mock(Alert.class);
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlert()).thenReturn(alertMock);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			Mockito.when(event.getFirst()).thenReturn(definitionOne);
			ArgumentCaptor<AlertClosingReason> reasonCapture = ArgumentCaptor.forClass(AlertClosingReason.class);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verify(iteratorMock).remove();
			Mockito.verifyNoMoreInteractions(iteratorMock);
			Mockito.verify(alertMock, times(1)).close(any(Long.class), reasonCapture.capture());
			assertThat(reasonCapture.getValue(), equalTo(AlertClosingReason.ALERTING_DEFINITION_DELETED));
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedUnknownEvent() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			Mockito.when(event.getFirst()).thenReturn(definitionTwo);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock, times(2)).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verifyNoMoreInteractions(iteratorMock);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateEvent() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			Mockito.when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(stateMock, times(1)).setAlertingDefinition(definitionOne);
			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verifyNoMoreInteractions(iteratorMock);
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateEventAlertActive() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			Alert alertMock = mock(Alert.class);
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlert()).thenReturn(alertMock);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			Mockito.when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(stateMock, times(1)).setAlertingDefinition(definitionOne);
			Mockito.verify(alertMock, times(1)).setAlertingDefinition(definitionOne);
			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateUnknownEvent() {
			Mockito.when(definitionOne.getId()).thenReturn("id");
			Alert alertMock = mock(Alert.class);
			AlertingState stateMock = mock(AlertingState.class);
			Mockito.when(stateMock.getAlert()).thenReturn(alertMock);
			Mockito.when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			Mockito.when(alertingStates.iterator()).thenReturn(iteratorMock);
			Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
			Mockito.when(iteratorMock.next()).thenReturn(stateMock);
			Mockito.when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			Mockito.when(event.getFirst()).thenReturn(definitionTwo);

			alertingScheduler.onApplicationEvent(event);

			Mockito.verify(stateMock, never()).setAlertingDefinition(any(AlertingDefinition.class));
			Mockito.verify(alertMock, never()).setAlertingDefinition(any(AlertingDefinition.class));
			Mockito.verify(alertingStates).iterator();
			Mockito.verifyNoMoreInteractions(alertingStates);
			Mockito.verify(iteratorMock, times(2)).hasNext();
			Mockito.verify(iteratorMock).next();
			Mockito.verifyZeroInteractions(thresholdChecker);
			Mockito.verifyZeroInteractions(executorService);
		}
	}
}
