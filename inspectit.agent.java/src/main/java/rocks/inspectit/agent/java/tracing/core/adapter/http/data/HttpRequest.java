package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

import io.opentracing.propagation.TextMap;

/**
 * The interface to represent HTTP request. This means it needs to provide information about request
 * as URI and HTTP method, as well as implement the {@link TextMap} in order to propagate/get the
 * tracing information and baggage with/from the request.
 *
 * @author Ivan Senic
 *
 */
public interface HttpRequest extends TextMap {

	/**
	 * Returns URI of the request.
	 *
	 * @return Returns URI of the request.
	 */
	String getUri();

	/**
	 * Returns the HTTP method being execited (get, post, etc).
	 *
	 * @return Returns the HTTP method being execited (get, post, etc).
	 */
	String getHttpMethod();
}
