package rocks.inspectit.server.influx;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.InfluxAvailabilityChecker.InfluxAvailabilityListener;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 *
 * Tests the {@link InfluxAvailabilityChecker} class.
 *
 * @author Marius Oehler
 *
 */
public class InfluxAvailabilityCheckerTest extends TestBase {

	@InjectMocks
	InfluxAvailabilityChecker availabilityChecker;

	@Mock
	Logger log;

	@Mock
	InfluxDB influxDb;

	@Mock
	InfluxAvailabilityListener listener;

	@Mock
	ScheduledExecutorService executorService;

	/**
	 * Tests the {@link InfluxAvailabilityChecker#activate()} method.
	 */
	public static class Activate extends InfluxAvailabilityCheckerTest {

		@Test
		public void activate() {
			availabilityChecker.activate();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void activateTwice() {
			Mockito.when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(Mockito.mock(ScheduledFuture.class));

			availabilityChecker.activate();
			availabilityChecker.activate();

			Mockito.verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void activateAfterDeactivation() {
			ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
			Mockito.when(scheduledFuture.isDone()).thenReturn(false, true);
			Mockito.when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);

			availabilityChecker.activate();
			availabilityChecker.deactivate();
			availabilityChecker.activate();

			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(scheduledFuture, times(2)).isDone();
			Mockito.verify(scheduledFuture).cancel(false);
			Mockito.verifyNoMoreInteractions(scheduledFuture);
			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}
	}

	/**
	 * Tests the {@link InfluxAvailabilityChecker#deactivate()} method.
	 */
	public static class Deactivate extends InfluxAvailabilityCheckerTest {

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void cancelFuture() {
			ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
			Mockito.when(scheduledFuture.isDone()).thenReturn(false);
			Mockito.when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);
			availabilityChecker.activate();

			availabilityChecker.deactivate();

			Mockito.verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(scheduledFuture).isDone();
			Mockito.verify(scheduledFuture).cancel(false);
			Mockito.verifyNoMoreInteractions(scheduledFuture);
			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		public void futureNull() {
			availabilityChecker.deactivate();

			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void futureIsDone() {
			ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
			Mockito.when(scheduledFuture.isDone()).thenReturn(true);
			Mockito.when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);
			availabilityChecker.activate();

			availabilityChecker.deactivate();

			Mockito.verify(scheduledFuture).isDone();
			Mockito.verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verifyNoMoreInteractions(scheduledFuture);
			Mockito.verifyZeroInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}
	}

	/**
	 * Tests the {@link InfluxAvailabilityChecker#run()} method.
	 */
	public static class Run extends InfluxAvailabilityCheckerTest {

		@Test
		public void isAvailable() {
			availabilityChecker.activate();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailable() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class);

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		public void isAvailableAndWasAvailable() {
			availabilityChecker.activate();
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isAvailableAndWasNotAvailable() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class).thenReturn(Mockito.mock(Pong.class));
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(listener).onReconnection();
			Mockito.verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailableAndWasAvailable() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenReturn(Mockito.mock(Pong.class)).thenThrow(Exception.class);
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(listener).onDisconnection();
			Mockito.verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailableAndWasNotAvailable() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class);
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY * 2), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void resetExecutionDelay() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class).thenThrow(Exception.class).thenThrow(Exception.class).thenReturn(Mockito.mock(Pong.class));

			availabilityChecker.run();
			availabilityChecker.run();
			availabilityChecker.run();
			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY * 2), any(TimeUnit.class));
			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY * 3), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(4)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(listener).onReconnection();
			Mockito.verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void unexcpectedException() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class).thenReturn(Mockito.mock(Pong.class));
			Mockito.doThrow(Exception.class).when(listener).onReconnection();
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(listener).onReconnection();
			Mockito.verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void listenerIsNull() {
			availabilityChecker.activate();
			Mockito.when(influxDb.ping()).thenThrow(Exception.class).thenReturn(Mockito.mock(Pong.class));
			availabilityChecker.setAvailabilityListener(null);
			availabilityChecker.run();

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
		}

		@Test
		public void influxIsNull() {
			availabilityChecker.activate();
			availabilityChecker.setInflux(null);

			availabilityChecker.run();

			Mockito.verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
			Mockito.verifyZeroInteractions(influxDb);
		}

		@Test
		public void isNotActive() {
			availabilityChecker.run();

			Mockito.verify(influxDb).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(listener);
			Mockito.verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void stateChangedAfterReactivation() {
			Mockito.when(influxDb.ping()).thenReturn(Mockito.mock(Pong.class)).thenThrow(Exception.class);
			ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
			Mockito.when(scheduledFuture.isDone()).thenReturn(false, true);
			Mockito.when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);

			availabilityChecker.activate();
			availabilityChecker.run();
			availabilityChecker.deactivate();
			availabilityChecker.activate();
			availabilityChecker.run();

			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			Mockito.verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(InfluxAvailabilityChecker.EXECUTION_DELAY), any(TimeUnit.class));
			Mockito.verifyNoMoreInteractions(executorService);
			Mockito.verify(influxDb, times(2)).ping();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(scheduledFuture, times(2)).isDone();
			Mockito.verify(scheduledFuture).cancel(false);
			Mockito.verifyNoMoreInteractions(scheduledFuture);
			Mockito.verifyZeroInteractions(listener);
		}
	}
}
