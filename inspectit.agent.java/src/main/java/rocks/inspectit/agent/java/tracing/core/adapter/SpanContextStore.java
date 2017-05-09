package rocks.inspectit.agent.java.tracing.core.adapter;

import io.opentracing.SpanContext;

/**
 * Span context stores enables storing and retrieving of the span context. Implementations provide a
 * way to store {@link SpanContext} object temporarily.
 *
 * @author Ivan Senic
 *
 */
public interface SpanContextStore {

	/**
	 * Returns the {@link SpanContext} that was previously saved with the store.
	 *
	 * @return Returns the {@link SpanContext} that was previously saved with the store.
	 */
	SpanContext getSpanContext();

	/**
	 * Sets the span context to the store.
	 *
	 * @param spanContext
	 *            {@link SpanContext} to be saved.
	 */
	void setSpanContext(SpanContext spanContext);

	/**
	 * Useful constants.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface Constants {

		/**
		 * Constant for span context key. Can be used for storing in maps or map like data
		 * structures.
		 */
		String ID = "rocks.inspectit.sc";
	}
}
