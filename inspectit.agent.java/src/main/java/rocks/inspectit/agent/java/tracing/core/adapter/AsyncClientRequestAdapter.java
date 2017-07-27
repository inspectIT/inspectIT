package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Adapter for the asynchronous client requests.
 *
 * @param <C>
 *            type of the carrier provided by this adapter
 * @author Ivan Senic
 *
 */
public interface AsyncClientRequestAdapter<C> extends ClientRequestAdapter<C> {

	/**
	 * Provides span store adapter for storing and querying span store objects. It's expected that
	 * all async client requests adapters that have reference should provide this adapter.
	 *
	 * @return {@link SpanStoreAdapter}
	 */
	SpanStoreAdapter getSpanStoreAdapter();
}
