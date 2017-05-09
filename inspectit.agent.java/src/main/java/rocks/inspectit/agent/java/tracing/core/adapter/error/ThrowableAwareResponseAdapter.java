package rocks.inspectit.agent.java.tracing.core.adapter.error;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * Response adapter that can be used to report error as part of the tags. If {@link #throwableType}
 * is specified then also add the extra tag about the exact throwable that occurred.
 * <p>
 * It's also possible to specify delegate adapter in case when some information can be read from
 * them even in exceptional situations. Usually this is the case when adapter does not depend on the
 * result of the method invocation, but rather parameters.
 *
 * @author Ivan Senic
 *
 */
public class ThrowableAwareResponseAdapter implements ResponseAdapter {

	/**
	 * Response adapter to delegate calls to anyway pick-up tags from this adapter as well. If
	 * <code>null</code> ignored.
	 */
	private final ResponseAdapter delegateAdapter;

	/**
	 * Throwable type. If specified will be added to the tags.
	 */
	private final String throwableType;

	/**
	 * No-argument constructor. Both fields set to <code>null</code>.
	 */
	public ThrowableAwareResponseAdapter() {
		this(null, null);
	}

	/**
	 * Constructor that only sets {@link #throwableType}.
	 *
	 * @param throwableType
	 *            hrowable type. If specified will be added to the tags.
	 */
	public ThrowableAwareResponseAdapter(String throwableType) {
		this(null, throwableType);
	}

	/**
	 * @param delegateAdapter
	 *            Response adapter to delegate tags providing if there is no error.
	 * @param throwableType
	 *            Throwable type. If specified will be added to the tags.
	 */
	public ThrowableAwareResponseAdapter(ResponseAdapter delegateAdapter, String throwableType) {
		this.delegateAdapter = delegateAdapter;
		this.throwableType = throwableType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		// first exception tags
		Map<String, String> tags = new HashMap<String, String>(2);
		tags.put(Tags.ERROR.getKey(), String.valueOf(true));

		if (StringUtils.isNotEmpty(throwableType)) {
			tags.put(ExtraTags.THROWABLE_TYPE, throwableType);
		}

		// then delegate ones if enabled
		if (null != delegateAdapter) {
			tags.putAll(delegateAdapter.getTags());
		}

		return tags;
	}

}
