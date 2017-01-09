package rocks.inspectit.agent.java.sdk.opentracing;

/**
 * Timer interface for measuring.
 *
 * @author Ivan Senic
 *
 */
public interface Timer {

	/**
	 * Returns current time in microseconds (microseconds since epoh).
	 *
	 * @return Returns current time in microseconds (microseconds since epoh).
	 */
	long getCurrentTimeMicroseconds();

	/**
	 * Returns the current nano time. Simple implementation can use
	 * {@link java.lang.System#nanoTime()}.
	 *
	 * @return Returns the current nano time.
	 */
	long getCurrentNanoTime();
}
