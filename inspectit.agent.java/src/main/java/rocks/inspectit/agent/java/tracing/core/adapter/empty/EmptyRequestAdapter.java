package rocks.inspectit.agent.java.tracing.core.adapter.empty;

import java.util.Collections;
import java.util.Map;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Empty request adapter can be used as {@link ServerRequestAdapter} or
 * {@link ClientRequestAdapter}. It provides no information and it's baggage carriers can not inject
 * nor extract data.
 * <p>
 * This adapter is used for the
 * {@link rocks.inspectit.agent.java.sensor.method.remote.server.manual.ManualRemoteServerSensor},
 * since this type of the sensor is manually placed by the user and we can not provide any data.
 *
 * @author Ivan Senic
 *
 */
public final class EmptyRequestAdapter implements ServerRequestAdapter<TextMap> {

	/**
	 * Static instance for usage.
	 */
	public static final EmptyRequestAdapter INSTANCE = new EmptyRequestAdapter();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private EmptyRequestAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Format<TextMap> getFormat() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextMap getCarrier() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanContextStore getSpanContextStore() {
		return NoopSpanContextStore.INSTANCE;
	}

}
