package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.References;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.ClientHttpRequest;

/**
 * The {@link ClientRequestAdapter} for all asynchronous HTTP client requests.
 *
 * @author Ivan Senic
 *
 */
public class AsyncHttpClientRequestAdapter extends HttpClientRequestAdapter implements AsyncClientRequestAdapter<TextMap> {

	/**
	 * Span store adapter.
	 */
	private SpanStoreAdapter spanStoreAdapter;

	/**
	 * Default constructor.
	 *
	 * @param httpClientRequest
	 *            Client HTTP request from which we can read data.
	 * @param spanStoreAdapter
	 *            As this is asynchronous request we must provide {@link SpanStoreAdapter} for being
	 *            able to start/stop span in later point of time.
	 */
	public AsyncHttpClientRequestAdapter(ClientHttpRequest httpClientRequest, SpanStoreAdapter spanStoreAdapter) {
		super(httpClientRequest);
		this.spanStoreAdapter = spanStoreAdapter;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Async requests are always having type of {@link io.opentracing.ReferenceType#FOLLOWS_FROM}.
	 */
	@Override
	public String getReferenceType() {
		return References.FOLLOWS_FROM;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanStoreAdapter getSpanStoreAdapter() {
		return spanStoreAdapter;
	}

}
