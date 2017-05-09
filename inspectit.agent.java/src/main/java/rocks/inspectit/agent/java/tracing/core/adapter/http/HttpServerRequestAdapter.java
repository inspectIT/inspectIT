package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore;

/**
 * The {@link ServerRequestAdapter} for all synchronous HTTP server requests.
 *
 * @author Ivan Senic
 */
public class HttpServerRequestAdapter extends HttpRequestAdapter implements ServerRequestAdapter<TextMap> {

	/**
	 * Http request providing data we need.
	 */
	private HttpRequest httpRequest;

	/**
	 * Span context store.
	 */
	private SpanContextStore spanContextStore;

	/**
	 * Constructor that sets only {@link #httpRequest}. For {@link #spanContextStore} the instance
	 * of {@link NoopSpanContextStore} will be used.
	 *
	 * @param httpRequest
	 *            Http request providing data we need.
	 */
	public HttpServerRequestAdapter(HttpRequest httpRequest) {
		this(httpRequest, NoopSpanContextStore.INSTANCE);
	}

	/**
	 * Default constructor.
	 *
	 * @param httpRequest
	 *            Http request providing data we need.
	 * @param spanContextStore
	 *            {@link SpanContextStore}. Must not be <code>null</code>.
	 */
	public HttpServerRequestAdapter(HttpRequest httpRequest, SpanContextStore spanContextStore) {
		super(httpRequest);
		this.httpRequest = httpRequest;
		this.spanContextStore = spanContextStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Format<TextMap> getFormat() {
		return Format.Builtin.HTTP_HEADERS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextMap getCarrier() {
		return httpRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextStore getSpanContextStore() {
		return spanContextStore;
	};

}
