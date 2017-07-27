package rocks.inspectit.agent.java.tracing.core.listener;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;

/**
 * Listener for the firing of the async spans. The listener gets informed about the span context
 * that has been created, but the span might start and finish in later point.
 *
 * @author Ivan Senic
 *
 */
public interface IAsyncSpanContextListener {

	/**
	 * Informs the listener about creation of the async span by passing its the context.
	 *
	 * @param spanContextImpl
	 *            {@link SpanContextImpl}
	 */
	void asyncSpanContextCreated(SpanContextImpl spanContextImpl);
}
