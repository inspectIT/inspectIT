package rocks.inspectit.agent.java.sdk.opentracing.internal;

/**
 * Provides {@link TracerLogger} for any class that wants to log details related to tracing.
 *
 * @author Ivan Senic
 *
 */
public interface TracerLoggerProvider {

	/**
	 * Returns {@link TracerLogger} for the given class.
	 *
	 * @param clazz
	 *            Class to get logger for.
	 * @return Returns {@link TracerLogger} for the given class.
	 */
	TracerLogger getTraceLogger(Class<?> clazz);

}
