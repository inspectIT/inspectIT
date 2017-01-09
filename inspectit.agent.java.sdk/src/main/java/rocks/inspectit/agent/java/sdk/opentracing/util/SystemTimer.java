package rocks.inspectit.agent.java.sdk.opentracing.util;

import rocks.inspectit.agent.java.sdk.opentracing.Timer;

/**
 * {@link Timer} implementation that uses {@link java.lang.System} for timing.
 *
 * @author Ivan Senic
 *
 */
public class SystemTimer implements Timer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCurrentTimeMicroseconds() {
		return System.currentTimeMillis() * 1000L;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCurrentNanoTime() {
		return System.nanoTime();
	}

}
