package rocks.inspectit.agent.java.sdk.opentracing.internal.noop;

import rocks.inspectit.agent.java.sdk.opentracing.TracerLogger;

/**
 * No-operation logger that implement the {@link TracerLogger} interface. Simply ignores all the log
 * calls.
 *
 * @author Ivan Senic
 *
 */
public class NoopLogger implements TracerLogger {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(String message) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(String message) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message, Throwable t) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message, Throwable t) {
	}

}
