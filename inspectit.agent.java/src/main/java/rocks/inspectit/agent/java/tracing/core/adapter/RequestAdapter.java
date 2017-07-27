package rocks.inspectit.agent.java.tracing.core.adapter;

import io.opentracing.propagation.Format;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Generic request requestAdapter. The requestAdapter is responsible providing basic information
 * about the request as propagation type and tags associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @param <C>
 *            type of the carrier provided by this adapter
 * @author Ivan Senic
 *
 */
public interface RequestAdapter<C> extends TagsProvidingAdapter {

	/**
	 * Returns propagation type for the request.
	 *
	 * @return Returns propagation type for the request.
	 */
	PropagationType getPropagationType();

	/**
	 * Returns reference type for the request. If request is synchronous it's expected that
	 * {@link io.opentracing.References#CHILD_OF} is returned. If request is asynchronous it's
	 * expected that {@link io.opentracing.References#FOLLOWS_FROM} is returned.
	 *
	 * @return Returns reference type for the request.
	 */
	String getReferenceType();

	/**
	 * Format that the carrier supports.
	 *
	 * @return Format that the carrier supports.
	 */
	Format<C> getFormat();

	/**
	 * Carrier for extracting the baggage.
	 *
	 * @return carrier
	 */
	C getCarrier();

}
