package rocks.inspectit.agent.java.sdk.opentracing.internal.propagation;

import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.AbstractPropagator;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.Propagator;

/**
 * Simple {@link Propagator} for the {@link TextMap}. Only delegates to the {@link TextMap}.
 *
 * @author Ivan Senic
 *
 */
public class TextMapPropagator extends AbstractPropagator<TextMap> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void injectBaggage(TextMap carrier, String key, String value) {
		carrier.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Iterable<Entry<String, String>> extractBaggage(TextMap carrier) {
		return carrier;
	}

}
