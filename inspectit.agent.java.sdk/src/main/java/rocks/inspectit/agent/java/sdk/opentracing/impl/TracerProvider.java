package rocks.inspectit.agent.java.sdk.opentracing.impl;

import rocks.inspectit.agent.java.sdk.opentracing.ExtendedTracer;
import rocks.inspectit.agent.java.sdk.opentracing.noop.NoopExtendedTracerImpl;

/**
 * The tracer provider allows getting of the {@link ExtendedTracer} instance.
 * <p>
 * If the inspectIT is running with the application where {@link TracerProvider} is used, the
 * <code>get</code> methods will always returned correctly initialized {@link ExtendedTracer}.
 * <p>
 * If the inspectIT is not running, then the caller can control if the returned tracer is noop
 * tracer or <code>null</code>.
 *
 * @author Ivan Senic
 *
 */
public final class TracerProvider {

	/**
	 * The initialized implementation of the {@link TracerImpl}.
	 */
	private static TracerImpl tracer; // NOPMD NOCHK

	/**
	 * No instantiation.
	 */
	private TracerProvider() {
	}

	/**
	 * Returns the {@link ExtendedTracer}. This method never returns <code>null</code>. If the
	 * inspectIT is running, this method will return the correctly initialized inspectIT tracer
	 * implementation that reports data to the inspectIT. If the inspectIT is not running with the
	 * application this method will return the noop tracer.
	 * <p>
	 * Same as calling {@link #get(true)}.
	 *
	 * @return {@link ExtendedTracer}
	 */
	public static ExtendedTracer get() {
		return get(true);
	}

	/**
	 * Returns the {@link ExtendedTracer}. If the inspectIT is running, this method will return the
	 * correctly initialized inspectIT tracer implementation that reports data to the inspectIT. If
	 * the inspectIT is not running with the application then value of <code>noopFailback</code>
	 * will be used to determine the return.
	 *
	 * @param noopFailback
	 *            Controls what is returned if the inspectIT tracer is not available. Passing
	 *            <code>true</code> will return the noop tracer, while passing <code>false</code>
	 *            will return <code>null</code>.
	 * @return {@link ExtendedTracer}
	 */
	public static ExtendedTracer get(boolean noopFailback) {
		if (null != tracer) {
			return tracer;
		} else if (noopFailback) {
			return NoopExtendedTracerImpl.INSTANCE;
		} else {
			return null;
		}
	}

	/**
	 * Sets {@link #tracer}.
	 *
	 * @param tracer
	 *            New value for {@link #tracer}
	 */
	static void set(TracerImpl tracer) {
		TracerProvider.tracer = tracer;
	}

}
