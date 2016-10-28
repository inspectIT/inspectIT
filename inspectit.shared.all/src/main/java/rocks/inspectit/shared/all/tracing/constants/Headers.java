package rocks.inspectit.shared.all.tracing.constants;

/**
 * @author Ivan Senic
 *
 */
public interface Headers {

	/**
	 * Header name for the span id.
	 */
	String SPAN_ID = "X-inspectIT-SpanId";

	/**
	 * Header name for the trace id.
	 */
	String TRACE_ID = "X-inspectIT-TraceId";

	/**
	 * Header name for the span parent id.
	 */
	String SPAN_PARENT_ID = "X-inspectIT-ParentSpanId";
}
