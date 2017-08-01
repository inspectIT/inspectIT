package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * Span stores adapter enables storing and retrieving {@link SpanStore}. Implementations provide a
 * way to store {@link SpanStore} object temporarily.
 *
 * @author Ivan Senic
 *
 */
public interface SpanStoreAdapter {

	/**
	 * Returns the {@link SpanStore} that was previously saved with the adapter.
	 *
	 * @return Returns the {@link SpanStore} that was previously saved with the adapter.
	 */
	SpanStore getSpanStore();

	/**
	 * Sets the span store.
	 *
	 * @param spanStore
	 *            {@link SpanStore} to be saved.
	 */
	void setSpanStore(SpanStore spanStore);

	/**
	 * Useful constants.
	 *
	 * @author Ivan Senic
	 *
	 */
	interface Constants {

		/**
		 * Constant for span store key. Can be used for storing in maps or map like data structures.
		 */
		String ID = "rocks.inspectit.spanstore";

		/**
		 * Constant for the CANCEL tag which indicates whether a Span has been canceled.
		 */
		String CANCEL = "rocks.inspectit.cancel";
	}
}
