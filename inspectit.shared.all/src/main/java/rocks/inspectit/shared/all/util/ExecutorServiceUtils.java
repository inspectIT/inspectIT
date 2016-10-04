package rocks.inspectit.shared.all.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for easy work with {@link ExecutorService}s.
 *
 * @author Marius Oehler
 *
 */
public final class ExecutorServiceUtils {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtils.class);

	/**
	 * Hidden constructor.
	 */
	private ExecutorServiceUtils() {
	}

	/**
	 * Shutdown the given {@link ExecutorService}. If the {@link ExecutorService} does not
	 * {@link ExecutorService} during the specified timeout it will be tried to force (cancel
	 * currently running threads) the shutdown.
	 *
	 * @param service
	 *            {@link ExecutorService} to shutdown
	 * @param timeout
	 *            timeout to wait before canceling the running thread
	 * @param timeUnit
	 *            {@link TimeUnit} of the timeout
	 */
	public static void shutdownExecutor(ExecutorService service, long timeout, TimeUnit timeUnit) {
		// shutdown service
		service.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!service.awaitTermination(timeout, timeUnit)) {
				// Cancel currently executing tasks
				service.shutdownNow();
				// Wait a while for tasks to respond to being canceled
				if (!service.awaitTermination(timeout, timeUnit)) {
					if (LOG.isErrorEnabled()) {
						LOG.error("Executor service did not terminate.");
					}
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			service.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
