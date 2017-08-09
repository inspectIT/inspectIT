package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

import io.opentracing.propagation.TextMap;

/**
 * The interface to represent HTTP request. This means it needs to provide information about request
 * as URL and HTTP method, as well as implement the {@link TextMap} in order to propagate/get the
 * tracing information and baggage with/from the request.
 *
 * @author Ivan Senic
 *
 */
public interface HttpRequest extends TextMap {

	/**
	 * Returns URL of the request.
	 *
	 * @return Returns URL of the request.
	 */
	String getUrl();

	/**
	 * Returns the HTTP method being executed (get, post, etc).
	 *
	 * @return Returns the HTTP method being executed (get, post, etc).
	 */
	String getHttpMethod();
}
