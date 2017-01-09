package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Generic request requestAdapter. The requestAdapter is responsible providing basic information
 * about the request as propagation type and tags associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @author Ivan Senic
 *
 */
public interface RequestAdapter extends TagsProvidingAdapter {

	/**
	 * Returns propagation type for the request.
	 *
	 * @return Returns propagation type for the request.
	 */
	PropagationType getPropagationType();

}
