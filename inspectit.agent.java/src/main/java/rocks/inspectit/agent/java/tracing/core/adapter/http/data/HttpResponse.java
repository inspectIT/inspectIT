package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

/**
 * The interface to represent HTTP request. Provides only the status of the response.
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
