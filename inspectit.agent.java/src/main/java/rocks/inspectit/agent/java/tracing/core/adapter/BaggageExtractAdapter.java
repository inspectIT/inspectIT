package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Extract adapters can be used to get the baggage item that were transfered with the request.
 * Usually this is tracing data like span, trace and parent ids, but other baggage might also be
 * transfered.
 * <p>
 * This class was inspired by opentracing.io and openzipking.io implementations.
 *
 * @author Ivan Senic
 *
 */
public interface BaggageExtractAdapter {

	/**
	 * Returns baggage item with provided key if it exists.
	 *
	 * @param key
	 *            Baggage key
	 * @return Baggage value or <code>null</code> if it does not exists.
	 */
	String getBaggageItem(String key);

}
