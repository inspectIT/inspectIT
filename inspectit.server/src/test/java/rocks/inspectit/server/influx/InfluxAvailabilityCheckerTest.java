package rocks.inspectit.server.influx;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void activateTwice() {
			when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));

			availabilityChecker.activate();
			availabilityChecker.activate();

			verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void activateAfterDeactivation() {
			ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
			when(scheduledFuture.isDone()).thenReturn(false, true);
			when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);

			availabilityChecker.activate();
			availabilityChecker.deactivate();
			availabilityChecker.activate();

			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(scheduledFuture, times(2)).isDone();
			verify(scheduledFuture).cancel(false);
			verifyNoMoreInteractions(scheduledFuture);
			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
		}
	}

	/**
	 * Tests the {@link InfluxAvailabilityChecker#deactivate()} method.
	 */
	public static class Deactivate extends InfluxAvailabilityCheckerTest {

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void cancelFuture() {
			ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
			when(scheduledFuture.isDone()).thenReturn(false);
			when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);
			availabilityChecker.activate();

			availabilityChecker.deactivate();

			verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(scheduledFuture).isDone();
			verify(scheduledFuture).cancel(false);
			verifyNoMoreInteractions(scheduledFuture);
			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		public void futureNull() {
			availabilityChecker.deactivate();

			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
			verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void futureIsDone() {
			ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
			when(scheduledFuture.isDone()).thenReturn(true);
			when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);
			availabilityChecker.activate();

			availabilityChecker.deactivate();

			verify(scheduledFuture).isDone();
			verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verifyNoMoreInteractions(scheduledFuture);
			verifyZeroInteractions(influxDb);
			verifyZeroInteractions(listener);
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

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb).ping();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailable() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenThrow(Exception.class);

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb).ping();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		public void isAvailableAndWasAvailable() {
			availabilityChecker.activate();
			availabilityChecker.run();

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isAvailableAndWasNotAvailable() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenThrow(Exception.class).thenReturn(mock(Pong.class));
			availabilityChecker.run();

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(15L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verify(listener).onReconnection();
			verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailableAndWasAvailable() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenReturn(mock(Pong.class)).thenThrow(Exception.class);
			availabilityChecker.run();

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verify(listener).onDisconnection();
			verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void isNotAvailableAndWasNotAvailable() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenThrow(Exception.class);
			availabilityChecker.run();

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(15L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void resetExecutionDelay() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenThrow(Exception.class).thenThrow(Exception.class).thenThrow(Exception.class).thenThrow(Exception.class).thenThrow(Exception.class).thenThrow(Exception.class)
			.thenThrow(Exception.class).thenReturn(mock(Pong.class));

			availabilityChecker.run(); // 15 delay
			availabilityChecker.run(); // 30 delay
			availabilityChecker.run(); // 60 delay
			availabilityChecker.run(); // 600 delay
			availabilityChecker.run(); // 1800 delay
			availabilityChecker.run(); // 3600 delay
			availabilityChecker.run(); // 3600 delay
			availabilityChecker.run(); // 5 delay

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(15L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(30L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(60L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(600L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(1800L), any(TimeUnit.class));
			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(3600L), any(TimeUnit.class));
			verify(executorService).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(8)).ping();
			verifyNoMoreInteractions(influxDb);
			verify(listener).onReconnection();
			verifyNoMoreInteractions(listener);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void unexcpectedException() {
			availabilityChecker.activate();
			when(influxDb.ping()).thenThrow(Exception.class).thenReturn(mock(Pong.class));
			doThrow(Exception.class).when(listener).onReconnection();
			availabilityChecker.run();

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verify(listener).onReconnection();
			verifyNoMoreInteractions(listener);
		}

		@Test
		public void influxIsNull() {
			availabilityChecker.activate();
			availabilityChecker.setInflux(null);

			availabilityChecker.run();

			verify(executorService).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService, times(1)).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
			verifyZeroInteractions(influxDb);
		}

		@Test
		public void isNotActive() {
			availabilityChecker.run();

			verify(influxDb).ping();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(listener);
			verifyZeroInteractions(executorService);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void stateChangedAfterReactivation() {
			when(influxDb.ping()).thenReturn(mock(Pong.class)).thenThrow(Exception.class);
			ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
			when(scheduledFuture.isDone()).thenReturn(false, true);
			when(executorService.schedule(any(Runnable.class), any(Long.class), any(TimeUnit.class))).thenReturn(scheduledFuture);

			availabilityChecker.activate();
			availabilityChecker.run();
			availabilityChecker.deactivate();
			availabilityChecker.activate();
			availabilityChecker.run();

			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(0L), any(TimeUnit.class));
			verify(executorService, times(2)).schedule(eq(availabilityChecker), eq(5L), any(TimeUnit.class));
			verifyNoMoreInteractions(executorService);
			verify(influxDb, times(2)).ping();
			verifyNoMoreInteractions(influxDb);
			verify(scheduledFuture, times(2)).isDone();
			verify(scheduledFuture).cancel(false);
			verifyNoMoreInteractions(scheduledFuture);
			verifyZeroInteractions(listener);
		}
	}
}
