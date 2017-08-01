package rocks.inspectit.agent.java.tracing.core.adapter.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
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
public abstract class HttpRequestAdapter implements RequestAdapter<TextMap> {

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
		String url = httpRequest.getUrl();
		String method = httpRequest.getHttpMethod();

		if ((null == url) && (null == method)) {
			return Collections.emptyMap();
		}

		Map<String, String> tags = new HashMap<String, String>(2, 1f);
		if ((null != url) && !url.isEmpty()) {
			tags.put(Tags.HTTP_URL.getKey(), url);
		}
		if (null != method) {
			tags.put(Tags.HTTP_METHOD.getKey(), method);
		}
		return tags;
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
