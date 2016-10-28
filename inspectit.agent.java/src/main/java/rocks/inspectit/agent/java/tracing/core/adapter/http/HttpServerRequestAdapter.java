package rocks.inspectit.agent.java.tracing.core.adapter.http;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpServerRequest;

/**
 * The {@link ServerRequestAdapter} for all synchronous HTTP server requests.
 *
 * @author Ivan Senic
 */
public class HttpServerRequestAdapter extends HttpRequestAdapter implements ServerRequestAdapter {

	/**
	 * Http server request providing data we need.
	 */
	private HttpServerRequest httpServerRequest;

	/**
	 * Default constructor.
	 *
	 * @param httpServerRequest
	 *            Http server request providing data we need.
	 */
	public HttpServerRequestAdapter(HttpServerRequest httpServerRequest) {
		super(httpServerRequest);
		this.httpServerRequest = httpServerRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageExtractAdapter getBaggageExtractAdapter() {
		return httpServerRequest;
	}

}
