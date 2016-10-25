package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class AlertingSchedulerTest extends TestBase {

	/**
	 * Test the {@link AlertingScheduler#init()} method.
	 *
	 */
	public static class Init extends AlertingSchedulerTest {

		@InjectMocks
		AlertingScheduler alertingScheduler;

		@Mock
		Logger logger;

		@Test
		public void testInit() {
			AlertingScheduler schedulerSpy = Mockito.spy(alertingScheduler);
			doNothing().when(schedulerSpy).updateState();

			schedulerSpy.init();

			verify(schedulerSpy, times(1)).updateState();
		}
	}

	/**
	 * Test the {@link AlertingScheduler#run()} method.
	 */
	public static class Run extends AlertingSchedulerTest {

		@InjectMocks
		AlertingScheduler alertingScheduler;

		@Mock
		Logger logger;

		@Mock
		ThresholdChecker thresholdChecker;

		@Mock
		AlertingState stateOne;

		@Mock
		AlertingState stateTwo;

		@Mock
		AlertingDefinition definitionOne;

		@Mock
		AlertingDefinition definitionTwo;

		@Test
		public void runThresholdCheck() throws Exception {
			when(definitionOne.getTimeRange(any(TimeUnit.class))).thenReturn(1L);
			when(definitionTwo.getTimeRange(any(TimeUnit.class))).thenReturn(3600000L);
			when(stateOne.getAlertingDefinition()).thenReturn(definitionOne);
			when(stateTwo.getAlertingDefinition()).thenReturn(definitionTwo);
			when(stateOne.getLastCheckTime()).thenReturn(System.currentTimeMillis() - 10L);
			when(stateTwo.getLastCheckTime()).thenReturn(System.currentTimeMillis() - 10L);
			alertingScheduler.alertingStates = Arrays.asList(stateOne, stateTwo);

			alertingScheduler.run();

			verify(thresholdChecker, times(1)).checkThreshold(stateOne);
			verify(thresholdChecker, times(0)).checkThreshold(stateTwo);
		}
	}

	/**
	 * Test the {@link AlertingScheduler#updateState()} method.
	 */
	public static class UpdateState extends AlertingSchedulerTest {

		@InjectMocks
		AlertingScheduler alertingScheduler;

		@Mock
		Logger logger;

		@Mock
		ScheduledExecutorService executorService;

		@SuppressWarnings("rawtypes")
		ScheduledFuture futureMock = mock(ScheduledFuture.class);

		@Test
		@SuppressWarnings({ "unchecked" })
		public void setActiveOnce() throws Exception {
			alertingScheduler.active = true;
			when(futureMock.isDone()).thenReturn(false);
			when(executorService.scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), any(TimeUnit.class))).thenReturn(futureMock);

			alertingScheduler.updateState();
			alertingScheduler.updateState();

			verify(executorService, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), any(TimeUnit.class));
		}

		@Test
		public void activateWhenDone() {
			alertingScheduler.active = true;
			alertingScheduler.scheduledFuture = futureMock;
			when(futureMock.isDone()).thenReturn(true);

			alertingScheduler.updateState();

			verify(executorService, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Long.class), any(Long.class), any(TimeUnit.class));
		}

		@Test
		public void disableScheduler() throws Exception {
			alertingScheduler.active = false;
			alertingScheduler.scheduledFuture = futureMock;

			alertingScheduler.updateState();

			verify(futureMock, times(1)).cancel(false);
		}

		@Test
		public void disableWhenNull() throws Exception {
			alertingScheduler.active = false;

			alertingScheduler.updateState();
		}
	}

	/**
	 * Test the
	 * {@link AlertingScheduler#onApplicationEvent(rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent)}
	 * method.
	 *
	 */
	public static class OnApplicationEvent extends AlertingSchedulerTest {

		@InjectMocks
		AlertingScheduler alertingScheduler;

		@Mock
		Logger logger;

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
		}

		@Test
		public void loadingEvent() {
			ArgumentCaptor<AlertingState> stateCapture = ArgumentCaptor.forClass(AlertingState.class);
			when(event.getType()).thenReturn(AlertDefinitionEventType.LOADED);
			when(event.getAlertingDefinitions()).thenReturn(Arrays.asList(definitionOne, definitionTwo));

			alertingScheduler.onApplicationEvent(event);

			verify(alertingStates, times(1)).clear();
			verify(alertingStates, times(2)).add(stateCapture.capture());
			assertThat(stateCapture.getAllValues().get(0).getAlertingDefinition(), equalTo(definitionOne));
			assertThat(stateCapture.getAllValues().get(1).getAlertingDefinition(), equalTo(definitionTwo));
		}

		@Test
		public void createEvent() {
			ArgumentCaptor<AlertingState> stateCapture = ArgumentCaptor.forClass(AlertingState.class);
			when(event.getType()).thenReturn(AlertDefinitionEventType.ADDED);
			when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			verify(alertingStates, times(1)).add(stateCapture.capture());
			assertThat(stateCapture.getValue().getAlertingDefinition(), equalTo(definitionOne));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedEvent() {
			when(definitionOne.getId()).thenReturn("id");
			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);
			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);
			when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			verify(alertingStates, times(1)).remove(stateMock);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedEventAlertActive() {
			when(definitionOne.getId()).thenReturn("id");

			Alert alertMock = mock(Alert.class);

			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlert()).thenReturn(alertMock);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);

			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);

			when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			when(event.getFirst()).thenReturn(definitionOne);
			ArgumentCaptor<AlertClosingReason> reasonCapture = ArgumentCaptor.forClass(AlertClosingReason.class);

			alertingScheduler.onApplicationEvent(event);

			verify(alertingStates, times(1)).remove(stateMock);
			verify(alertMock, times(1)).close(any(Long.class), reasonCapture.capture());
			assertThat(reasonCapture.getValue(), equalTo(AlertClosingReason.ALERTING_DEFINITION_DELETED));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void deletedUnknownEvent() {
			when(definitionOne.getId()).thenReturn("id");

			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);

			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);

			when(event.getType()).thenReturn(AlertDefinitionEventType.REMOVED);
			when(event.getFirst()).thenReturn(definitionTwo);

			alertingScheduler.onApplicationEvent(event);

			verify(iteratorMock, times(0)).remove();
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateEvent() {
			when(definitionOne.getId()).thenReturn("id");

			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);

			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);

			when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			verify(stateMock, times(1)).setAlertingDefinition(definitionOne);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateEventAlertActive() {
			when(definitionOne.getId()).thenReturn("id");

			Alert alertMock = mock(Alert.class);

			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlert()).thenReturn(alertMock);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);

			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);

			when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			when(event.getFirst()).thenReturn(definitionOne);

			alertingScheduler.onApplicationEvent(event);

			verify(stateMock, times(1)).setAlertingDefinition(definitionOne);
			verify(alertMock, times(1)).setAlertingDefinition(definitionOne);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void updateUnknownEvent() {
			when(definitionOne.getId()).thenReturn("id");

			Alert alertMock = mock(Alert.class);

			AlertingState stateMock = mock(AlertingState.class);
			when(stateMock.getAlert()).thenReturn(alertMock);
			when(stateMock.getAlertingDefinition()).thenReturn(definitionOne);

			Iterator<AlertingState> iteratorMock = mock(Iterator.class);
			when(alertingStates.iterator()).thenReturn(iteratorMock);
			when(iteratorMock.hasNext()).thenReturn(true, false);
			when(iteratorMock.next()).thenReturn(stateMock);

			when(event.getType()).thenReturn(AlertDefinitionEventType.UPDATE);
			when(event.getFirst()).thenReturn(definitionTwo);

			alertingScheduler.onApplicationEvent(event);

			verify(stateMock, never()).setAlertingDefinition(any(AlertingDefinition.class));
			verify(alertMock, never()).setAlertingDefinition(any(AlertingDefinition.class));
		}
	}
}
