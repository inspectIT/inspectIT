package rocks.inspectit.shared.all.util;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test for the {@link ExecutorServiceUtils} class.
 *
 * @author Marius Oehler
 *
 */
public class ExecutorServiceUtilsTest extends TestBase {

	/**
	 * Tests the {@link ExecutorServiceUtils#shutdownExecutor(ExecutorService, long, TimeUnit)}
	 * method.
	 *
	 */
	public static class Shutdown extends ExecutorServiceUtilsTest {

		@Test
		public void successfulShutdown() throws InterruptedException {
			ExecutorService executorService = mock(ExecutorService.class);
			when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

			ExecutorServiceUtils.shutdownExecutor(executorService, 1, TimeUnit.MINUTES);

			verify(executorService).shutdown();
			verify(executorService).awaitTermination(1, TimeUnit.MINUTES);
			verifyNoMoreInteractions(executorService);
		}

		@Test
		public void shutdownAfterTimeout() throws InterruptedException {
			ExecutorService executorService = mock(ExecutorService.class);
			when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(false, true);

			ExecutorServiceUtils.shutdownExecutor(executorService, 1, TimeUnit.MINUTES);

			verify(executorService).shutdown();
			verify(executorService, times(2)).awaitTermination(1, TimeUnit.MINUTES);
			verify(executorService).shutdownNow();
			verifyNoMoreInteractions(executorService);
		}

		@Test
		public void failingShutdown() throws InterruptedException {
			ExecutorService executorService = mock(ExecutorService.class);
			when(executorService.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(false, false);

			ExecutorServiceUtils.shutdownExecutor(executorService, 1, TimeUnit.MINUTES);

			verify(executorService).shutdown();
			verify(executorService, times(2)).awaitTermination(1, TimeUnit.MINUTES);
			verify(executorService).shutdownNow();
			verifyNoMoreInteractions(executorService);
		}

		@Test
		public void shutdownInterrupted() throws InterruptedException {
			ExecutorService executorService = mock(ExecutorService.class);
			doThrow(new InterruptedException()).when(executorService).awaitTermination(1, TimeUnit.MINUTES);

			ExecutorServiceUtils.shutdownExecutor(executorService, 1, TimeUnit.MINUTES);

			verify(executorService).shutdown();
			verify(executorService, times(1)).awaitTermination(1, TimeUnit.MINUTES);
			verify(executorService).shutdownNow();
			verifyNoMoreInteractions(executorService);
		}
	}

}
