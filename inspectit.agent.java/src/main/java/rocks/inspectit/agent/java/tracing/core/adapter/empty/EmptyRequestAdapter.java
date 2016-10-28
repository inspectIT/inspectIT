package rocks.inspectit.agent.java.tracing.core.adapter.empty;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.BaggageExtractAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.BaggageInjectAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;

/**
 * Empty request adapter can be used as {@link ServerRequestAdapter} or
 * {@link ClientRequestAdapter}. It provides no information and it's baggage adapters are doing
 * nothing (can not inject nor extract data).
 *
 * @author Ivan Senic
 *
 */
public final class EmptyRequestAdapter implements ServerRequestAdapter, ClientRequestAdapter, BaggageExtractAdapter, BaggageInjectAdapter {

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
	public ReferenceType getReferenceType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Tag, String> getTags() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageExtractAdapter getBaggageExtractAdapter() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaggageInjectAdapter getBaggageInjectAdapter() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putBaggageItem(String key, String value) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaggageItem(String key) {
		return null;
	}

}
