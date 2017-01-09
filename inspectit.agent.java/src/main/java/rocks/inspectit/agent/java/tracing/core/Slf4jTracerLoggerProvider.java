package rocks.inspectit.agent.java.tracing.core;

import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;
import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLoggerProvider;

/**
 * Provider of the {@link Slf4jTracerLogger}s.
 *
 * @author Ivan Senic
 *
 */
public class Slf4jTracerLoggerProvider implements TracerLoggerProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TracerLogger getTraceLogger(Class<?> clazz) {
		return new Slf4jTracerLogger(LoggerFactory.getLogger(clazz));
	}

}
