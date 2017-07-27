package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.References;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;

/**
 * The {@link ServerRequestAdapter} for all synchronous HTTP server requests.
 *
 * @author Ivan Senic
 */
public class HttpServerRequestAdapter extends HttpRequestAdapter implements ServerRequestAdapter<TextMap> {

	/**
	 * Span context store.
	 */
	private SpanContextStore spanContextStore;

	/**
	 * Constructor that accepts implementation that is at the same time the {@link HttpRequest} and
	 * {@link SpanContextStore}.
	 *
	 * @param <T>
	 *            type of parameter
	 * @param requestWithContextStore
	 *            Http request that is at the same time context store as well.
	 */
	public <T extends HttpRequest & SpanContextStore> HttpServerRequestAdapter(T requestWithContextStore) {
		this(requestWithContextStore, requestWithContextStore);
	}

	/**
	 * Default constructor.
	 *
	 * @param httpRequest
	 *            Http request providing data we need.
	 * @param spanContextStore
	 *            {@link SpanContextStore}. Must not be <code>null</code>, rather use
	 *            {@link rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore}
	 *            instance in case you don't want to provide context store.
	 */
	public HttpServerRequestAdapter(HttpRequest httpRequest, SpanContextStore spanContextStore) {
		super(httpRequest);
		this.spanContextStore = spanContextStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextStore getSpanContextStore() {
		return spanContextStore;
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceType() {
		return References.CHILD_OF;
	}

}
