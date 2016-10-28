package rocks.inspectit.agent.java.tracing.core.adapter.http.data;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;

/**
 * Our own interface to represent HTTP server request.
 *
 * @author Ivan Senic
 *
 */
public interface HttpServerRequest extends HttpRequest, BaggageExtractAdapter {

}
