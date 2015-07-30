package info.novatec.inspectit.indexing.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import info.novatec.inspectit.spring.logger.Log;

/**
 * Logs uncaught exceptions.
 * @author Tobias Angerstein
 *
 */
@Component (value = "forkJoinPoolExceptionHandler")
public class ForkJoinUncaughtExceptionHandler implements UncaughtExceptionHandler {	

	/**
	 * Logger.
	 */
	@Log
	Logger logger;
	
	/**
	 * Logs the uncaught exception.
	 * {@inheritDoc}
	 */
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Uncaught Exception occured in ForkJoinPool in thread " + t.getName(), e);
		
	}

}
