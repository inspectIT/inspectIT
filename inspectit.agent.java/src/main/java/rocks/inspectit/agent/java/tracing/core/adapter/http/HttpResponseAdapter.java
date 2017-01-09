package rocks.inspectit.agent.java.tracing.core.adapter.http;

import java.util.Collections;
import java.util.Map;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;

/**
 * The base {@link ResponseAdapter} for all synchronous HTTP client responses.
 *
 * @author Ivan Senic
 */
public class HttpResponseAdapter implements ResponseAdapter {

	/**
	 * HTTP response to read data from.
	 */
	private HttpResponse httpResponse;

	/**
	 * Default constructor.
	 *
	 * @param httpResponse
	 *            HTTP response to read data from.
	 */
	public HttpResponseAdapter(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		int status = httpResponse.getStatus();
		if (status > 0) {
			return Collections.<String, String> singletonMap(Tags.HTTP_STATUS.getKey(), String.valueOf(status));
		} else {
			return Collections.emptyMap();
		}
	}

}
