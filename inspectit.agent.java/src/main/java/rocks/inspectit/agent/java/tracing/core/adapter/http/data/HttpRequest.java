package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

/**
 * Our own interface to represent HTTP request.
 *
 * @author Ivan Senic
 *
 */
public interface HttpRequest {

	/**
	 * Returns URI of the request.
	 *
	 * @return Returns URI of the request.
	 */
	String getUri();
}
