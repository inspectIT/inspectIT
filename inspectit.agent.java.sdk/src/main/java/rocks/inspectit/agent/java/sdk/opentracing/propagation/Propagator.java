package rocks.inspectit.agent.java.sdk.opentracing.propagation;

import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanContextImpl;

/**
 * Propagator interface to help with the injection and extraction of the span context.
 *
 * @param <C>
 *            type of carrier
 * @author Ivan Senic
 *
 */
public interface Propagator<C> {

	/**
	 * Injects the span context to the carrier.
	 *
	 * @param spanContext
	 *            context
	 * @param carrier
	 *            carrier
	 */
	void inject(SpanContextImpl spanContext, C carrier);

	/**
	 * Extract the span context from the carrier.
	 *
	 * @param carrier
	 *            carrier
	 * @return span context
	 */
	SpanContextImpl extract(C carrier);
}
