package rocks.inspectit.agent.java.tracing.core.adapter.http;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.RequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.shared.all.tracing.constants.Tag;
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
	public Map<Tag, String> getTags() {
		String uri = httpRequest.getUri();
		if (null != uri) {
			return Collections.<Tag, String> singletonMap(Tag.Http.URL, uri);
		} else {
			return Collections.emptyMap();
		}
	}

}
