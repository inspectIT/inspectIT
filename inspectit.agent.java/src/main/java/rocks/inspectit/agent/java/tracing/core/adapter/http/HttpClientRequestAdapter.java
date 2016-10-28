package rocks.inspectit.agent.java.tracing.core.adapter.http;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;

/**
 * The {@link ClientRequestAdapter} for all synchronous HTTP client requests.
 *
 * @author Ivan Senic
 */
public class HttpClientRequestAdapter extends HttpRequestAdapter implements ClientRequestAdapter {

	/**
	 * Http client request providing data we need.
	 */
	private HttpClientRequest httpClientRequest;

	/**
	 * Default constructor.
	 *
	 * @param httpClientRequest
	 *            HTTP request from which we can read data.
	 */
	public HttpClientRequestAdapter(HttpClientRequest httpClientRequest) {
		super(httpClientRequest);
		this.httpClientRequest = httpClientRequest;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * As this requestAdapter supports only sync HTTP calls we use {@value ReferenceType#CHILD_OF}.
	 */
	@Override
	public ReferenceType getReferenceType() {
		return ReferenceType.CHILD_OF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageInjectAdapter getBaggageInjectAdapter() {
		return httpClientRequest;
	}

}
