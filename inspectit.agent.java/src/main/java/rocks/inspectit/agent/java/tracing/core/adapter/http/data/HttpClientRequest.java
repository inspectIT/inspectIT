package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;

/**
 * Our own interface to represent HTTP client request.
 *
 * @author Ivan Senic
 *
 */
public interface HttpClientRequest extends HttpRequest, BaggageInjectAdapter {

}
