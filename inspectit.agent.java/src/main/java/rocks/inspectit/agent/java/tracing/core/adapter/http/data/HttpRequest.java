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
	 * Defines if the request can be traced at this point of time and new span should be started.
	 * The reasons not to start can be that the trace information is already set, or connection has
	 * already been made.
	 * <p>
	 * If returned <code>true</code> it's expected that http request does not yet have tracing
	 * information in the headers and that headers can be inserted.
	 *
	 * @return <code>true</code> if interceptor should start new request, <code>false</code>
	 *         otherwise.
	 */
	boolean startClientSpan();

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
