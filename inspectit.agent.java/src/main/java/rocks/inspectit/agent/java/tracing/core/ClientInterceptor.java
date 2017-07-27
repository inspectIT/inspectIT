package rocks.inspectit.agent.java.tracing.core;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * Client interceptor provide easy way to handle client request and response. It internally calls
 * {@link TracerImpl} to correctly create new client spans. To use the interceptor one must provide
 * {@link ClientRequestAdapter} to handle the request and {@link ResponseAdapter} to handle the
 * response in case of synchronous request. With asynchronous request it's only required to provide
 * the {@link AsyncClientRequestAdapter}.
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
		SpanBuilderImpl builder = handleRequestInternal(requestAdapter);

		// nothing to do if builder was not created
		if (null == builder) {
			return null;
		}

		// set no reporting as we will do it ourselves in case on sync requests
		builder.doNotReport();

		// start
		SpanImpl span = builder.start();

		// inject here as the context is created when span is started
		tracer.inject(span.context(), requestAdapter.getFormat(), requestAdapter.getCarrier());

		return span;
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
	public <C> SpanImpl handleAsyncRequest(AsyncClientRequestAdapter<C> requestAdapter) {
		SpanBuilderImpl builder = handleRequestInternal(requestAdapter);

		// nothing to do if builder was not created
		if (null == builder) {
			return null;
		}

		// just build the span and store it for later start
		SpanImpl span = builder.build();
		SpanStore spanStore = new SpanStore();
		spanStore.storeSpan(span);
		requestAdapter.getSpanStoreAdapter().setSpanStore(spanStore);

		// inject here as the context is created when span is started
		tracer.inject(span.context(), requestAdapter.getFormat(), requestAdapter.getCarrier());

		return span;
	}

	/**
	 * Part of handling the request no matter if it's asynchronous or not.
	 *
	 * @param <C>
	 *            type of carrier adapter is providing
	 * @param requestAdapter
	 *            {@link ClientRequestAdapter} providing necessary information.
	 * @return Created span builder
	 */
	private <C> SpanBuilderImpl handleRequestInternal(ClientRequestAdapter<C> requestAdapter) {
		if (!requestAdapter.startClientSpan()) {
			return null;
		}

		// create span from the current context
		// using null for operation name as we can distinguish spans without it
		String referenceType = requestAdapter.getReferenceType();
		SpanBuilderImpl builder = tracer.buildSpan(null, referenceType, true);

		// set as client
		builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

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

		return builder;
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
