package rocks.inspectit.agent.java.tracing.core.adapter.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.tracing.core.adapter.RequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * The base {@link RequestAdapter} for all synchronous HTTP requests.
 *
 * @author Ivan Senic
 *
 */
public class HttpRequestAdapter implements RequestAdapter {

	/**
	 * Http server request providing data we need.
	 */
	private HttpRequest httpRequest;

	/**
	 * Default constructor.
	 *
	 * @param httpRequest
	 *            Http server request providing data we need.
	 */
	public HttpRequestAdapter(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.HTTP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		String uri = httpRequest.getUri();
		String method = httpRequest.getHttpMethod();

		if ((null == uri) && (null == method)) {
			return Collections.emptyMap();
		}

		Map<String, String> tags = new HashMap<String, String>(2, 1f);
		if (null != uri) {
			tags.put(Tags.HTTP_URL.getKey(), uri);
		}
		if (null != method) {
			tags.put(Tags.HTTP_METHOD.getKey(), method);
		}
		return tags;
	}

}
