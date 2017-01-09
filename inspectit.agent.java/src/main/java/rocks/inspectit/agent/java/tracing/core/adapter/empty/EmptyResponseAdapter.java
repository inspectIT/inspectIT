package rocks.inspectit.agent.java.tracing.core.adapter.empty;

import java.util.Collections;
import java.util.Map;

import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;

/**
 * {@link ResponseAdapter} that does not provide any information (tags).
 * <p>
 * This adapter is used for the asynchronous responses as in this case thread is not waiting for the
 * response and can not provide any information on it success or data.
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
	public Map<String, String> getTags() {
		return Collections.emptyMap();
	}

}
