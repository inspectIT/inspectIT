package rocks.inspectit.agent.java.tracing.core.adapter.http;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.shared.all.tracing.constants.Tag;

/**
 * The base {@link ResponseAdapter} for all synchronous HTTP client requests.
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
	public Map<Tag, String> getTags() {
		int status = httpResponse.getStatus();
		if (status > 0) {
			return Collections.<Tag, String> singletonMap(Tag.Http.STATUS, String.valueOf(status));
		} else {
			return Collections.emptyMap();
		}
	}

}
