package rocks.inspectit.agent.java.tracing.core.adapter;

import java.util.Map;

/**
 * Adapter that can provide tags. Tags can be anything that describe more the client/server request
 * or response. For example it can be HTTP URI and method invoked for the request, or status code
 * for the response. Different adapters will provide different types of the tags based on the
 * technology that they support.
 *
 * @author Ivan Senic
 *
 */
public interface TagsProvidingAdapter {

	/**
	 * Return tags available to this request/response adapter.
	 *
	 * @return Return tags available to this request/response adapter.
	 */
	Map<String, String> getTags();
}
