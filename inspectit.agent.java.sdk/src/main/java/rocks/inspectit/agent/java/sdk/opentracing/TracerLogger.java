package rocks.inspectit.agent.java.sdk.opentracing;

/**
 * Interface that provides the logging ability to the tracer. This interface is defined in order to
 * not introduce any dependencies.
 * <p>
 * This SDK only provides the {@link rocks.inspectit.agent.java.sdk.opentracing.noop.NoopLogger} as
 * the implementation. However, if the inspectit agent is active on the target application, the
 * "real" SLF4J logger will be used which does the logging. In this case the initialization of the
 * logger and the tracer is done by inspectIT and can be obtained in
 * {@link rocks.inspectit.agent.java.sdk.opentracing.TracerProvider}.
 *
 * @author Ivan Senic
 *
 */
public interface TracerLogger {

	/**
	 * Log message in info level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void info(String message);

	/**
	 * Log message in debug level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void debug(String message);

	/**
	 * Log message in warn level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void warn(String message);

	/**
	 * Log message in warn level with throwable.
	 *
	 * @param message
	 *            Message to log.
	 * @param t
	 *            Throwable to log.
	 */
	void warn(String message, Throwable t);

	/**
	 * Log message in error level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void error(String message);

	/**
	 * Log message in error level with throwable.
	 *
	 * @param message
	 *            Message to log.
	 * @param t
	 *            Throwable to log.
	 */
	void error(String message, Throwable t);

}
