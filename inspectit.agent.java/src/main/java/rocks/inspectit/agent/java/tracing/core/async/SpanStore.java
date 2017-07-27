package rocks.inspectit.agent.java.tracing.core.async;

import java.util.Map;

import org.apache.commons.collections.MapUtils;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.tracing.core.adapter.TagsProvidingAdapter;

/**
 * Span store which can be used to store a span and interact (e.g. starting and stopping) with it.
 *
 * @author Marius Oehler
 *
 */
public class SpanStore {

	/**
	 * The stored span.
	 */
	private SpanImpl span;

	/**
	 * Gets {@link #span}.
	 *
	 * @return {@link #span}
	 */
	public SpanImpl getSpan() {
		return this.span;
	}

	/**
	 * Sets {@link #span}.
	 *
	 * @param span
	 *            New value for {@link #span}
	 */
	public void storeSpan(SpanImpl span) {
		this.span = span;
	}

	/**
	 * Starts the stored span if it has not been started already. The method is null-safe.
	 *
	 * @return Returns <code>true</code> if the span could have been started.
	 */
	public boolean startSpan() {
		if ((span != null) && !span.isStarted()) {
			span.start();
			return true;
		}
		return false;
	}

	/**
	 * Stops the stored span if it has not been stopped already. The method is null-safe.
	 *
	 * @return Returns <code>true</code> if the span could have been stopped.
	 */
	public boolean finishSpan() {
		if ((span != null) && !span.isFinished()) {
			span.finish();
			return true;
		}
		return false;
	}

	/**
	 * Stops the stored span if it has not been stopped already and adds the tags from the provider
	 * to the span. The method is null-safe.
	 *
	 * @param tagsProvidingAdapter
	 *            Provides map of tags to add before finishing the span. Must not be
	 *            <code>null</code>.
	 * @return Returns <code>true</code> if the span could have been stopped.
	 */
	public boolean finishSpan(TagsProvidingAdapter tagsProvidingAdapter) {
		if ((span != null) && !span.isFinished()) {
			// add all tags
			Map<String, String> tags = tagsProvidingAdapter.getTags();
			if (MapUtils.isNotEmpty(tags)) {
				for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
					span.setTag(tagEntry.getKey(), tagEntry.getValue());
				}
			}

			// then finish
			span.finish();
			return true;
		}
		return false;
	}

}
