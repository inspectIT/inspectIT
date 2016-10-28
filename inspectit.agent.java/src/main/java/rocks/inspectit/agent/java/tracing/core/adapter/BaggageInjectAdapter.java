package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Inject adapters can be used to set the baggage item that are going to be transfered with the
 * request. Usually this is tracing data like span, trace and parent ids, but other baggage might
 * also be transfered.
 * <p>
 * This class was inspired by opentracing.io and openzipking.io implementations.
 *
 * @author Ivan Senic
 *
 */
public interface BaggageInjectAdapter {


	/**
	 * Puts baggage item with provided key and value.
	 *
	 * @param key
	 *            Baggage key
	 * @param value
	 *            Baggage value
	 */
	void putBaggageItem(String key, String value);

}
