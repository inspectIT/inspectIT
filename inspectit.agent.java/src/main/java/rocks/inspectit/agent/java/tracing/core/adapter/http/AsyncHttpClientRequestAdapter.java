package rocks.inspectit.agent.java.tracing.core.adapter.http;

import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;

/**
 * The {@link ClientRequestAdapter} for all asynchronous HTTP client requests.
 * 
 * @author Ivan Senic
 *
 */
public class AsyncHttpClientRequestAdapter extends HttpClientRequestAdapter {

	/**
	 * Default constructor.
	 *
	 * @param httpClientRequest
	 *            HTTP request from which we can read data.
	 */
	public AsyncHttpClientRequestAdapter(HttpClientRequest httpClientRequest) {
		super(httpClientRequest);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Async requests are always having type of {@link ReferenceType#FOLLOW_FROM}.
	 */
	@Override
	public ReferenceType getReferenceType() {
		return ReferenceType.FOLLOW_FROM;
	}

}
