package rocks.inspectit.agent.java.tracing.core.adapter;

import java.util.Map;

import rocks.inspectit.shared.all.tracing.constants.Tag;

/**
 * Adapter that can provide tags.
 *
 * @author Ivan Senic
 *
 */
public interface TagsProvidingAdapter {

	/**
	 * Return tags available to this requestAdapter.
	 *
	 * @return Return tags available to this requestAdapter.
	 */
	Map<Tag, String> getTags();
}
