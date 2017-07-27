package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.References;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;

/**
 * The {@link ClientRequestAdapter} for all synchronous HTTP client requests.
 *
 * @author Ivan Senic
 */
public class HttpClientRequestAdapter extends HttpRequestAdapter implements ClientRequestAdapter<TextMap> {

	/**
	 * Http request providing data we need.
	 */
	private HttpRequest httpRequest;

	/**
	 * Default constructor.
	 *
	 * @param httpRequest
	 *            HTTP request from which we can read data.
	 */
	public HttpClientRequestAdapter(HttpRequest httpRequest) {
		super(httpRequest);
		this.httpRequest = httpRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startClientSpan() {
		return httpRequest.startClientSpan();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * As this requestAdapter supports only sync HTTP calls we use {@value References.CHILD_OF}.
	 */
	@Override
	public String getReferenceType() {
		return References.CHILD_OF;
	}

}
