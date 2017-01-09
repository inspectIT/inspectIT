package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;
import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLoggerProvider;
import rocks.inspectit.agent.java.sdk.opentracing.internal.noop.NoopLogger;

/**
 * Wraps the {@link TracerLoggerProvider} that will be used to provide {@link TracerLogger}.
 * <p>
 * In order to not introduce any logging dependencies to the SDK project, this class represents a
 * way to bridge the logging framework used in inspectIT to the SDK.
 *
 * @author Ivan Senic
 *
 */
public final class TracerLoggerWrapper {

	/**
	 * By default we use provider that returns the {@link NoopLogger}. If inspectIT agent is used
	 * then the provider will be overwritten with the one providing real logger instances.
	 */
	private static TracerLoggerProvider provider = new TracerLoggerProvider() {

		@Override
		public TracerLogger getTraceLogger(Class<?> clazz) {
			return NoopLogger.INSTANCE;
		}
	};

	/**
	 * Private constructor, only static methods.
	 */
	private TracerLoggerWrapper() {
	}

	/**
	 * Returns proper {@link TracerLogger} for the given class. If inspectIT agent is not used, then
	 * returned logger will be {@link NoopLogger}. Otherwise, inspectIT will do the bridging to the
	 * logging framework used in inspectIT (currently slf4j).
	 *
	 * @param clazz
	 *            Class to get logger for.
	 * @return {@link TracerLogger}
	 */
	public static TracerLogger getTraceLogger(Class<?> clazz) {
		return provider.getTraceLogger(clazz);
	}

	/**
	 * Sets {@link #provider}. Should be called only by inspectIT SDK classes.
	 *
	 * @param provider
	 *            New value for {@link #provider}
	 */
	public static void setProvider(TracerLoggerProvider provider) {
		if (null != provider) {
			TracerLoggerWrapper.provider = provider;
		}
	}

}
