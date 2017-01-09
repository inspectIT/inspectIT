package rocks.inspectit.agent.java.sdk.opentracing.constants;

/**
 * Propagation constants.
 * 
 * @author Ivan Senic
 *
 */
public interface PropagationConstants {

	/**
	 * Prefix for the propagation baggage.
	 */
	String INSPECTIT_PREFIX = "x-insp-";

	/**
	 * Header name for the span id.
	 */
	String SPAN_ID = INSPECTIT_PREFIX + "spanid";

	/**
	 * Header name for the trace id.
	 */
	String TRACE_ID = INSPECTIT_PREFIX + "traceid";

	/**
	 * Prefix for the propagation baggage.
	 */
	String INSPECTIT_BAGGAGE_PREFIX = INSPECTIT_PREFIX + "baggage-";
}
