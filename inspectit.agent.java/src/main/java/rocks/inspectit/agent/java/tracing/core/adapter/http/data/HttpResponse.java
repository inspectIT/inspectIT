package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

/**
 * Our own interface to represent HTTP response.
 * 
 * @author Ivan Senic
 *
 */
public interface HttpResponse {

	/**
	 * Returns the status of the response.
	 *
	 * @return Returns the status of the response.
	 */
	int getStatus();
}
