package rocks.inspectit.agent.java.tracing.core.adapter.http;

import io.opentracing.References;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;

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
	public AsyncHttpClientRequestAdapter(HttpRequest httpClientRequest) {
		super(httpClientRequest);
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

}
