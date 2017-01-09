package rocks.inspectit.agent.java.tracing.core;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Tracer logger that uses SLF4J for logging the tracing related messages.
 *
 * @author Ivan Senic
 *
 */
@Component
public class Slf4jTracerLogger implements TracerLogger {

	/**
	 * SLF4J Logger autowired.
	 */
	@Log
	Logger logger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(String message) {
		if (logger.isInfoEnabled()) {
			logger.info(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message) {
		if (logger.isWarnEnabled()) {
			logger.warn(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message, Throwable t) {
		if (logger.isWarnEnabled()) {
			logger.warn(message, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message) {
		if (logger.isErrorEnabled()) {
			logger.error(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message, Throwable t) {
		if (logger.isErrorEnabled()) {
			logger.error(message, t);
		}
	}

}
