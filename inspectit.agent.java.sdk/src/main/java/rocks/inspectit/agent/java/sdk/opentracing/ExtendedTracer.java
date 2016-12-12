package rocks.inspectit.agent.java.sdk.opentracing;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.Propagator;

/**
 * This interface defines additional methods that inspectIT tracer provides for usage.
 * <p>
 * There are additional two methods for creating a {@link SpanBuilder} where user can choose if
 * current thread context should be referenced or not.
 * <p>
 * The tracer allows registration of the {@link Propagator} for a specific format. This method
 * allows overwriting of the tracer default propagators. Note that inspectIT tracer by default uses
 * {@link rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.TextMapPropagator} for the
 * <code>io.opentracing.propagation.Format.Builtin.TEXT_MAP</code> and
 * {@link rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.UrlEncodingPropagator} for
 * the <code>io.opentracing.propagation.Format.Builtin.HTTP_HEADERS</code> format.
 *
 * @author Ivan Senic
 *
 */
public interface ExtendedTracer extends Tracer {

	/**
	 * Registers propagator. This method allows overwriting of the tracer default propagators. Note
	 * that inspectIT tracer by default uses
	 * {@link rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.TextMapPropagator} for
	 * the <code>io.opentracing.propagation.Format.Builtin.TEXT_MAP</code> and
	 * {@link rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.UrlEncodingPropagator}
	 * for the <code>io.opentracing.propagation.Format.Builtin.HTTP_HEADERS</code> format.
	 *
	 * @param <C>
	 *            format type
	 * @param format
	 *            opentracing {@link Format}
	 * @param propagator
	 *            {@link Propagator}
	 */
	<C> void registerPropagator(Format<C> format, Propagator<C> propagator);

	/**
	 * Sets the implementation of the {@link Timer} to use.
	 * <p>
	 * By default inspectIT tracer uses
	 * {@link rocks.inspectit.agent.java.sdk.opentracing.util.SystemTimer} that has millisecond
	 * start time precision. This done so inspectIT can be compatible with Java 6. Users can provide
	 * better timers if they run on higher Java versions or have third party dependencies that could
	 * do better.
	 *
	 * @param timer
	 *            {@link Timer} to set. Must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If timer provided is <code>null</code>.
	 */
	void setTimer(Timer timer) throws IllegalArgumentException;

	/**
	 * Builds span with no operation name. The thread context reference will added if the one exists
	 * as the CHILD_OF reference.
	 *
	 * @return {@link SpanBuilder}.
	 */
	SpanBuilder buildSpan();

	/**
	 * Creates {@link SpanBuilder} that optionally adds the reference to the current thread context
	 * span.
	 *
	 * @param operationName
	 *            Operation name of the span.
	 * @param referenceType
	 *            Reference type to the current context. Can be <code>null</code> if
	 *            <code>useThreadContext=false</code>
	 * @param useThreadContext
	 *            If thread context should be used.
	 * @return {@link SpanBuilder}.
	 */
	SpanBuilder buildSpan(String operationName, String referenceType, boolean useThreadContext);

	/**
	 * Returns the current thread span context if one exists. If the current thread has already
	 * started spans, this will return context of the last started span that is not finished yet.
	 * Otherwise this method returns <code>null</code>.
	 * <p>
	 * The noop tracer implementation should always return the
	 * {@link io.opentracing.NoopSpanContext} instance here.
	 *
	 * @return Context of the last started (non-finished) span by current thread or
	 *         <code>null</code> if thread has no such spans.
	 */
	SpanContext getCurrentContext();

}
