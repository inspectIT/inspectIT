package rocks.inspectit.agent.java.tracing.core.adapter.empty;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.tracing.constants.Tag;

/**
 * {@link ResponseAdapter} that does not provide any information.
 *
 * @author Ivan Senic
 *
 */
public final class EmptyResponseAdapter implements ResponseAdapter {

	/**
	 * Static instance for usage.
	 */
	public static final EmptyResponseAdapter INSTANCE = new EmptyResponseAdapter();

	/**
	 * Private constructor, use {@link #INSTANCE}.
	 */
	private EmptyResponseAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Tag, String> getTags() {
		return Collections.emptyMap();
	}

}
