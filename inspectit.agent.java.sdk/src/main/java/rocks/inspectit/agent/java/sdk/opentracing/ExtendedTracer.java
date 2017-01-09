package rocks.inspectit.agent.java.sdk.opentracing;

import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.Propagator;

/**
 * This interface defines additional method that inspectIT tracer provides for usage.
 * <p>
 * There are additional two methods for creating a {@link SpanBuilder} where user can choose if
 * current thread context should be referenced or not.
 * <p>
 * The tracer allows registration of the {@link Propagator} for a specific format. This method
 * allows overwriting of the tracer default propagators. Note that inspectIT tracer by default uses
 * {@link rocks.inspectit.agent.java.sdk.opentracing.propagation.TextMapPropagator} for the
 * {@link io.opentracing.propagation.Format.Builtin.TEXT_MAP} and
 * {@link rocks.inspectit.agent.java.sdk.opentracing.propagation.UrlEncodingPropagator} for the
 * {@link io.opentracing.propagation.Format.Builtin.HTTP_HEADERS} format.
 *
 * @author Ivan Senic
 *
 */
public interface ExtendedTracer extends Tracer {

	/**
	 * Registers propagator. This method allows overwriting of the tracer default propagators. Note
	 * that inspectIT tracer by default uses
	 * {@link rocks.inspectit.agent.java.sdk.opentracing.propagation.TextMapPropagator} for the
	 * {@link io.opentracing.propagation.Format.Builtin.TEXT_MAP} and
	 * {@link rocks.inspectit.agent.java.sdk.opentracing.propagation.UrlEncodingPropagator} for the
	 * {@link io.opentracing.propagation.Format.Builtin.HTTP_HEADERS} format.
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

}
