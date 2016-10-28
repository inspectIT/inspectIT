package rocks.inspectit.server.instrumentation.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.instrumentation.config.job.AbstractConfigurationChangeJob;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Abstract class for all configuration change listener.
 *
 * @author Marius Oehler
 *
 * @param <T>
 *            the type of the event to listen to
 */
public abstract class AbstractConfigurationChangeListener<T extends ApplicationEvent> implements ApplicationListener<T> {

	/**
	 * Logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * NextGenInstrumentationManager needed for cache map.
	 */
	@Autowired
	protected NextGenInstrumentationManager nextGenInstrumentationManager;

	/**
	 * Executor for dealing with configuration updates.
	 */
	@Autowired
	@Qualifier("agentServiceExecutorService")
	private ExecutorService executor;

	/**
	 * Executes the given {@link Collection} of {@link AbstractConfigurationChangeJob}s. The method
	 * blocks until all jobs have been ended.
	 *
	 * @param jobs
	 *            the {@link Collection} of {@link AbstractConfigurationChangeJob}s to execute
	 */
	@SuppressWarnings("rawtypes")
	protected void executeJobs(Collection<AbstractConfigurationChangeJob> jobs) {
		List<Future> futures = new ArrayList<>();

		for (AbstractConfigurationChangeJob job : jobs) {
			futures.add(executor.submit(job));
		}

		for (Future future : futures) {
			try {
				future.get(1L, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				if (log.isDebugEnabled()) {
					log.debug("Waiting for future was interrupted.", e);
				}
			} catch (ExecutionException e) {
				if (log.isErrorEnabled()) {
					log.error("Exception has been thrown while waiting for future.", e);
				}
			} catch (TimeoutException e) {
				if (log.isDebugEnabled()) {
					log.debug("Waiting for future was interrupted by the timeout.", e);
				}
			}
		}
	}
}
