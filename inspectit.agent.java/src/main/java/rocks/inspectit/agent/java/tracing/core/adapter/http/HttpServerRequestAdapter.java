package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;

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
	 * Default constructor.
	 *
	 * @param httpRequest
	 *            Http request providing data we need.
	 */
	public HttpServerRequestAdapter(HttpRequest httpRequest) {
		super(httpRequest);
		this.httpRequest = httpRequest;
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

}
