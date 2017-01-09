package rocks.inspectit.agent.java.sdk.opentracing.internal;

/**
 * Interface that provides the logging ability to the tracer. This interface is defined in order to
 * not introduce any dependencies.
 * <p>
 * This SDK only provides the
 * {@link rocks.inspectit.agent.java.sdk.opentracing.internal.noop.NoopLogger} as the
 * implementation. However, if the inspectIT agent is active on the target application, the "real"
 * SLF4J logger will be used which does the logging. In this case the initialization of the logger
 * and the tracer is done by inspectIT and can be obtained in
 * {@link rocks.inspectit.agent.java.sdk.opentracing.TracerProvider}.
 *
 * @author Ivan Senic
 *
 */
public interface TracerLogger {

	/**
	 * Defines if the info level is enabled.
	 *
	 * @return If the info level is enabled.
	 */
	boolean isInfoEnabled();

	/**
	 * Log message in info level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void info(String message);

	/**
	 * Defines if the debug level is enabled.
	 *
	 * @return If the debug level is enabled.
	 */
	boolean isDebugEnabled();

	/**
	 * Log message in debug level.
	 *
	 * @param message
	 *            Message to log.
	 */
	void debug(String message);

	/**
	 * Defines if the warn level is enabled.
	 *
	 * @return If the warn level is enabled.
	 */
	boolean isWarnEnabled();

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
	 * Defines if the error level is enabled.
	 *
	 * @return If the error level is enabled.
	 */
	boolean isErrorEnabled();

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
