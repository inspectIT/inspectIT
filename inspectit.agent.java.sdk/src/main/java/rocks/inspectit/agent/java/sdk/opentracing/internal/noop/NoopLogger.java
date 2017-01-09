package rocks.inspectit.agent.java.sdk.opentracing.internal.noop;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;

/**
 * No-operation logger that implement the {@link TracerLogger} interface. Simply ignores all the log
 * calls.
 *
 * @author Ivan Senic
 *
 */
public final class NoopLogger implements TracerLogger {

	/**
	 * Instance for usage.
	 */
	public static final NoopLogger INSTANCE = new NoopLogger();

	/**
	 * Private, use {@link #INSTANCE}.
	 */
	private NoopLogger() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled() {
		return false;
	}

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
	public boolean isDebugEnabled() {
		return false;
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
	public boolean isWarnEnabled() {
		return false;
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
	public boolean isErrorEnabled() {
		return false;
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
