package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

/**
 * The extension of the {@link HttpRequest} interface that represents the HTTP request from the
 * client side. This extension provides a method that enables checking if the new client span should
 * be started on the current {@link HttpRequest} request.
 *
 * @author Ivan Senic
 *
 */
public interface ClientHttpRequest extends HttpRequest {

	/**
	 * Defines if the request can be traced at this point of time and new span should be started.
	 * The reasons not to start can be that the trace information is already set, or connection has
	 * already been made.
	 *
	 * @return <code>true</code> if interceptor should start new request, <code>false</code>
	 *         otherwise.
	 */
	boolean startClientSpan();

}
