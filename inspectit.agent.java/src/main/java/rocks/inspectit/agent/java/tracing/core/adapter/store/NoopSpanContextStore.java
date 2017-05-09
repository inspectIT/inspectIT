package rocks.inspectit.agent.java.tracing.core.adapter.store;

import io.opentracing.SpanContext;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;

/**
 * Noop span context store. Ignores any set operation and always provides <code>null</code> as
 * result of the get.
 *
 * @author Ivan Senic
 *
 */
public final class NoopSpanContextStore implements SpanContextStore {

	/**
	 * Instance for usage.
	 */
	public static final SpanContextStore INSTANCE = new NoopSpanContextStore();

	/**
	 * Private. Use {@link #INSTANCE}.
	 */
	private NoopSpanContextStore() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContext getSpanContext() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSpanContext(SpanContext spanContext) {
	}

}
