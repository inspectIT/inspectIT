package rocks.inspectit.agent.java.sdk.opentracing.propagation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.sdk.opentracing.internal.constants.PropagationConstants;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.util.ConversionUtils;

/**
 * Abstract {@link Propagator} that knows what is injected and extracted from the carrier.
 * Implementing classes must only provide {@link #injectBaggage(Object, String, String)} and
 * {@link #extractBaggage(Object)} methods, where they can deal with any needed operations as
 * encoding for example.
 *
 * @param <C>
 *            type of carrier
 * @author Ivan Senic
 *
 */
public abstract class AbstractPropagator<C> implements Propagator<C> {

	/**
	 * Injects one baggage item to the carrier. Sub-class is responsible of providing exactly this
	 * key/value once {@link #extractBaggage(Object)} is called, but can wrap the key/value in what
	 * ever they want.
	 *
	 * @param carrier
	 *            carrier
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	protected abstract void injectBaggage(C carrier, String key, String value);

	/**
	 * Gets all baggage (not only tracing related) from the carrier. For example the HTTP carrier
	 * would here provide all headers. Subclasses can deal here with additional operations as
	 * encoding.
	 *
	 * @param carrier
	 *            carrier
	 * @return Itarbale
	 */
	protected abstract Iterable<Entry<String, String>> extractBaggage(C carrier);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inject(SpanContextImpl spanContext, C carrier) {
		if (null == spanContext) {
			return;
		}

		injectBaggage(carrier, PropagationConstants.SPAN_ID, ConversionUtils.toHexString(spanContext.getId()));
		injectBaggage(carrier, PropagationConstants.TRACE_ID, ConversionUtils.toHexString(spanContext.getTraceId()));
		Iterable<Entry<String, String>> baggageItems = spanContext.baggageItems();
		if (null != baggageItems) {
			for (Map.Entry<String, String> e : baggageItems) {
				injectBaggage(carrier, PropagationConstants.INSPECTIT_BAGGAGE_PREFIX + e.getKey(), e.getValue());
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextImpl extract(C carrier) {
		Iterable<Entry<String, String>> iterable = extractBaggage(carrier);
		if ((null == iterable) || (null == iterable.iterator())) {
			return null;
		}

		Map<String, String> passedBaggage = new HashMap<String, String>();
		String idFromBaggage = null;
		String traceIdFromBaggage = null;
		// iterate over the baggage
		for (Entry<String, String> e : iterable) {
			String key = e.getKey();
			if (PropagationConstants.SPAN_ID.equals(key)) {
				idFromBaggage = e.getValue();
			} else if (PropagationConstants.TRACE_ID.equals(key)) {
				traceIdFromBaggage = e.getValue();
			} else if (key.startsWith(PropagationConstants.INSPECTIT_BAGGAGE_PREFIX)) {
				String realKey = key.substring(PropagationConstants.INSPECTIT_BAGGAGE_PREFIX.length());
				passedBaggage.put(realKey, e.getValue());
			}
		}

		// at least span id and trace id are needed, if they are not passed return null
		if (notEmpty(idFromBaggage) && notEmpty(traceIdFromBaggage)) {
			try {
				long id = ConversionUtils.parseHexStringSafe(idFromBaggage);
				long traceId = ConversionUtils.parseHexStringSafe(traceIdFromBaggage);
				return new SpanContextImpl(id, traceId, 0, null, passedBaggage);
			} catch (NumberFormatException e) {
				// ids were not parsable
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Small utility to check if string is not empty.
	 *
	 * @param s
	 *            String
	 * @return If string is not null and has at least 1 char
	 */
	private boolean notEmpty(String s) {
		return (null != s) && !s.isEmpty();
	}

}
