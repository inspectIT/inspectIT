package rocks.inspectit.agent.java.tracing.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * Client interceptor provide easy way to handle client request and response. It internally calls
 * {@link TracerImpl} to correctly create new client spans. To use the interceptor one must provide
 * {@link ClientRequestAdapter} to handle the request and {@link ResponseAdapter} to handle the
 * response.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ClientInterceptor {

	/**
	 * {@link TracerImpl}.
	 */
	private final TracerImpl tracer;

	/**
	 * Default constructor.
	 *
	 * @param tracer
	 *            {@link TracerImpl} to use.
	 */
	@Autowired
	public ClientInterceptor(TracerImpl tracer) {
		this.tracer = tracer;
	}

	/**
	 * Handles the {@link ClientRequestAdapter}. This method should be called when new client
	 * request is created.
	 *
	 * @param <C>
	 *            type of carrier adapter is providing
	 * @param requestAdapter
	 *            {@link ClientRequestAdapter} providing necessary information.
	 * @return Created span
	 */
	public <C> SpanImpl handleRequest(ClientRequestAdapter<C> requestAdapter) {
		// create span from the current context
		SpanBuilderImpl builder = tracer.buildSpan(null, requestAdapter.getReferenceType(), true);

		// set as client
		builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

		// set no reporting as we will do it ourselves
		builder.doNotReport();

		// set propagation type
		if (null != requestAdapter.getPropagationType()) {
			builder.withTag(ExtraTags.PROPAGATION_TYPE, requestAdapter.getPropagationType().toString());
		}

		// handle custom tags
		Map<String, String> tags = requestAdapter.getTags();
		if (MapUtils.isNotEmpty(tags)) {
			for (Map.Entry<String, String> e : tags.entrySet()) {
				builder.withTag(e.getKey(), e.getValue());
			}
		}

		// start
		SpanImpl span = builder.start();

		// inject here as the context is created when span is started
		tracer.inject(span.context(), requestAdapter.getFormat(), requestAdapter.getCarrier());

		return span;
	}

	/**
	 * Handles the {@link ResponseAdapter}. This method should be called when the response on the
	 * client is received.
	 *
	 * @param span
	 *            Span to finish.
	 * @param responseAdapter
	 *            {@link ResponseAdapter} providing necessary information.
	 * @return Finished span (same as given parameter instance).
	 */
	public SpanImpl handleResponse(SpanImpl span, ResponseAdapter responseAdapter) {
		if (null == span) {
			return null;
		}

		// handle tags
		Map<String, String> tags = responseAdapter.getTags();
		if (MapUtils.isNotEmpty(tags)) {
			for (Map.Entry<String, String> e : tags.entrySet()) {
				span.setTag(e.getKey(), e.getValue());
			}
		}

		// finish and return
		span.finish();
		return span;
	}

}
