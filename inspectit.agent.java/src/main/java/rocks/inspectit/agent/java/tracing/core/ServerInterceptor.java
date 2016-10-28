package rocks.inspectit.agent.java.tracing.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Server interceptor provide easy way to handle server request and response. It internally calls
 * {@link Tracer} to correctly create or update current span on the server. To use the interceptor
 * one must provide {@link ServerRequestAdapter} to handle the request and {@link ResponseAdapter}
 * to handle the response.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ServerInterceptor {

	/**
	 * {@link Tracer}.
	 */
	@Autowired
	private final Tracer tracer;

	/**
	 * Default constructor.
	 *
	 * @param tracer
	 *            {@link Tracer} to use.
	 */
	@Autowired
	public ServerInterceptor(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * Handles the {@link ServerRequestAdapter}. This method should be called when new server
	 * request is received.
	 * <p>
	 * Note that returned span will not have the start time-stamp set. It's responsibility of the
	 * caller to correctly set the time-stamp.
	 *
	 * @param requestAdapter
	 *            {@link ClientRequestAdapter} providing necessary information.
	 * @return Current span that was created..
	 */
	public ServerSpan handleRequest(ServerRequestAdapter requestAdapter) {
		// eject data from request
		BaggageExtractAdapter baggageExtractAdapter = requestAdapter.getBaggageExtractAdapter();
		SpanIdent spanIdent = tracer.extractFromRequest(baggageExtractAdapter);

		if (null == spanIdent) {
			// if nothing is passed create new one
			spanIdent = SpanIdent.build();
		}

		ServerSpan span = new ServerSpan();
		span.setSpanIdent(spanIdent);
		span.setPropagationType(requestAdapter.getPropagationType());

		// handle tags
		Map<Tag, String> tags = requestAdapter.getTags();
		if (MapUtils.isNotEmpty(tags)) {
			span.addAllTags(tags);
		}

		tracer.updateCurrentSpan(span);

		return span;
	}

	/**
	 * Handles the {@link ResponseAdapter}. This method should be called before sending the response
	 * on the server.
	 * <p>
	 * Note that returned span will not have the duration set. It's responsibility of the caller to
	 * correctly set the duration on the returned span.
	 *
	 * @param responseAdapter
	 *            {@link ResponseAdapter} providing necessary information. Can not be
	 *            <code>null</code>.
	 * @return Current span or <code>null</code> if span was not created at the first place.
	 */
	public ServerSpan handleResponse(ResponseAdapter responseAdapter) {
		// remove current span
		ServerSpan span = tracer.removeCurrentSpan();
		if (null == span) {
			return null;
		}

		// handle tags
		Map<Tag, String> tags = responseAdapter.getTags();
		if (MapUtils.isNotEmpty(tags)) {
			span.addAllTags(tags);
		}

		return span;
	}
}
