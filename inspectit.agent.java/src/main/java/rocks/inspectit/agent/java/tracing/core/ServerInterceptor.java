package rocks.inspectit.agent.java.tracing.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

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
	 * {@link TracerImpl}.
	 */
	@Autowired
	private final TracerImpl tracer;

	/**
	 * Default constructor.
	 *
	 * @param tracer
	 *            {@link TracerImpl} to use.
	 */
	@Autowired
	public ServerInterceptor(TracerImpl tracer) {
		this.tracer = tracer;
	}

	/**
	 * Handles the {@link ServerRequestAdapter}. This method should be called when new server
	 * request is received.
	 *
	 * @param <C>
	 *            type of carrier adapter is providing
	 * @param requestAdapter
	 *            {@link ServerRequestAdapter} providing necessary information.
	 * @return Current span that was created.
	 */
	public <C> SpanImpl handleRequest(ServerRequestAdapter<C> requestAdapter) {
		// eject data from request
		SpanContext context = tracer.extract(requestAdapter.getFormat(), requestAdapter.getCarrier());

		// add reference to the passed context
		SpanBuilderImpl builder = tracer.buildSpan();
		builder.asChildOf(context);

		// set as server
		builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

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

		return builder.start();
	}

	/**
	 * Handles the {@link ResponseAdapter}. This method should be called before sending the response
	 * on the server.
	 *
	 * @param span
	 *            Span to finish.
	 * @param responseAdapter
	 *            {@link ResponseAdapter} providing necessary information. Can not be
	 *            <code>null</code>.
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
