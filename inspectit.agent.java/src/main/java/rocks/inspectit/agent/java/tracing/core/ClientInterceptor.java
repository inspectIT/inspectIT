package rocks.inspectit.agent.java.tracing.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;

/**
 * Client interceptor provide easy way to handle client request and response. It internally calls
 * {@link Tracer} to correctly create new client spans. To use the interceptor one must provide
 * {@link ClientRequestAdapter} to handle the request and {@link ResponseAdapter} to handle the
 * response.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ClientInterceptor {

	/**
	 * {@link Tracer}.
	 */
	private final Tracer tracer;

	/**
	 * Default constructor.
	 *
	 * @param tracer
	 *            {@link Tracer} to use.
	 */
	@Autowired
	public ClientInterceptor(Tracer tracer) {
		this.tracer = tracer;
	}

	/**
	 * Handles the {@link ClientRequestAdapter}. This method should be called when new client
	 * request is created.
	 * <p>
	 * Note that returned span will not have the start time-stamp set. It's responsibility of the
	 * caller to correctly set the time-stamp.
	 *
	 * @param requestAdapter
	 *            {@link ClientRequestAdapter} providing necessary information.
	 * @return Created span or <code>null</code> if no span was created.
	 */
	public ClientSpan handleRequest(ClientRequestAdapter requestAdapter) {
		// create new client span
		ClientSpan span = tracer.createClientSpan();

		// set properties
		span.setPropagationType(requestAdapter.getPropagationType());
		span.setReferenceType(requestAdapter.getReferenceType());

		// handle tags
		Map<Tag, String> tags = requestAdapter.getTags();
		if (MapUtils.isNotEmpty(tags)) {
			span.addAllTags(tags);
		}

		// inject to the request
		BaggageInjectAdapter baggageInjectAdapter = requestAdapter.getBaggageInjectAdapter();
		tracer.injectToRequest(baggageInjectAdapter, span.getSpanIdent());

		return span;
	}

	/**
	 * Handles the {@link ResponseAdapter}. This method should be called when we the response on the
	 * client is received.
	 * <p>
	 * Note that returned span will not have the duration set. It's responsibility of the caller to
	 * correctly set the duration on the returned span.
	 *
	 * @param responseAdapter
	 *            {@link ResponseAdapter} providing necessary information.
	 * @return Removed client span or <code>null</code> if no span was created..
	 */
	public ClientSpan handleResponse(ResponseAdapter responseAdapter) {
		// remove the current client span
		ClientSpan span = tracer.removeClientSpan();
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
