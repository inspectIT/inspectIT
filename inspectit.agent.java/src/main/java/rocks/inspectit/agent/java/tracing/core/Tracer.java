package rocks.inspectit.agent.java.tracing.core;

import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.shared.all.tracing.constants.Headers;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.all.tracing.util.ConversionUtil;

/**
 * Tracer is a simple component that provides basic methods on creating server/client spans. The
 * tracer also knows how to inject the tracing data to a request and vice versa. It's advised to use
 * {@link ClientInterceptor} and {@link ServerInterceptor} to handle requests/responses if possible.
 *
 * @author Ivan Senic
 *
 */
@Component
public class Tracer {

	/**
	 * {@link ThreadLocalSpanState}.
	 */
	private final ThreadLocalSpanState spanState = new ThreadLocalSpanState();

	/**
	 * Creates new {@link ServerSpan} that will be set as current span for the trace on the server.
	 * If the span for the trace already exists, that one will be returned.
	 *
	 * @return Current base span for the trace.
	 */
	public ServerSpan getOrCreateCurrentSpan() {
		ServerSpan current = spanState.getCurrentSpan();
		if (null == current) {
			current = new ServerSpan();
			current.setSpanIdent(SpanIdent.build());
			updateCurrentSpan(current);
		}
		return current;
	}

	/**
	 * Gets current span.
	 *
	 * @return Current span for the trace.
	 */
	public ServerSpan getCurrentSpan() {
		return spanState.getCurrentSpan();
	}

	/**
	 * Updates the state of the trace, by updating the current span.
	 * <p>
	 * The tracer is only responsible for the switching the state in the {@link #spanState}.
	 *
	 * @param span
	 *            Span to update with.
	 */
	public void updateCurrentSpan(ServerSpan span) {
		// make sure updated span has the span ident
		if ((null != span) && (null == span.getSpanIdent())) {
			throw new IllegalArgumentException("Update span does not have span ident set.");
		}

		// set update
		spanState.setCurrentSpan(span);
	}

	/**
	 * Clears current span.
	 *
	 * @return Returns span that was cleared from the state.
	 */
	public ServerSpan removeCurrentSpan() {
		ServerSpan currentSpan = spanState.getCurrentSpan();
		this.updateCurrentSpan(null);
		return currentSpan;
	}

	/**
	 * Creates new client {@link ClientSpan} that has the parent correct set based on the current
	 * span of current thread.
	 *
	 * @return {@link ClientSpan} containing only the set {@link SpanIdent}.
	 */
	public ClientSpan createClientSpan() {
		// get the current span
		Span currentSpan = this.getCurrentSpan();

		// create child ident
		SpanIdent childIdent = null;
		if (null != currentSpan) {
			childIdent = SpanIdent.build(currentSpan.getSpanIdent());
		} else {
			childIdent = SpanIdent.build();
		}

		// create new client span
		ClientSpan span = new ClientSpan();
		span.setSpanIdent(childIdent);

		// set in the state
		spanState.setCurrentClientSpan(span);

		return span;
	}

	/**
	 * Removes the current client span.
	 *
	 * @return Span that was current set as the client span.
	 */
	public ClientSpan removeClientSpan() {
		ClientSpan currentClientSpan = spanState.getCurrentClientSpan();
		spanState.setCurrentClientSpan(null);
		return currentClientSpan;
	}

	/**
	 * {@inheritDoc}
	 */
	public void injectToRequest(BaggageInjectAdapter injectAdapter, SpanIdent spanIdent) {
		if (null == spanIdent) {
			// in future here we can handle the tracing / debug data
			return;
		}

		injectAdapter.putBaggageItem(Headers.SPAN_ID, ConversionUtil.toHexString(spanIdent.getId()));
		injectAdapter.putBaggageItem(Headers.TRACE_ID, ConversionUtil.toHexString(spanIdent.getTraceId()));
		injectAdapter.putBaggageItem(Headers.SPAN_PARENT_ID, ConversionUtil.toHexString(spanIdent.getParentId()));
	}

	/**
	 * {@inheritDoc}
	 */
	public SpanIdent extractFromRequest(BaggageExtractAdapter extractAdapter) {
		String spanIdString = extractAdapter.getBaggageItem(Headers.SPAN_ID);
		String traceIdString = extractAdapter.getBaggageItem(Headers.TRACE_ID);
		String parentSpanIdString = extractAdapter.getBaggageItem(Headers.SPAN_PARENT_ID);

		if ((null != spanIdString) && (null != traceIdString)) {
			long spanId = ConversionUtil.parseHexStringSafe(spanIdString);
			long traceId = ConversionUtil.parseHexStringSafe(traceIdString);
			long parentSpanId = ConversionUtil.parseHexStringSafe(parentSpanIdString);

			return new SpanIdent(spanId, traceId, parentSpanId);
		}

		return null;
	}

}