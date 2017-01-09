package rocks.inspectit.agent.java.tracing.core;

import org.slf4j.Logger;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;

/**
 * Tracer logger that uses SLF4J for logging the tracing related messages.
 *
 * @author Ivan Senic
 *
 */
public class Slf4jTracerLogger implements TracerLogger {

	/**
	 * SLF4J Logger.
	 */
	private Logger logger;

	/**
	 * Default constructor.
	 *
	 * @param logger
	 */
	public Slf4jTracerLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

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
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
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
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
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
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
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
