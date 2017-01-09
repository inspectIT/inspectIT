package rocks.inspectit.agent.java.tracing.core.adapter;

import io.opentracing.propagation.Format;

/**
 * Client request adapter that works together with the
 * {@link rocks.inspectit.agent.java.tracing.core.ClientInterceptor} in order to correctly mark
 * client request. The requestAdapter is responsible providing open tracing format and carrier, as
 * well as for providing the basic information about the request as propagation type, reference type
 * and tags associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @param <C>
 *            type of the carrier provided by this adapter
 * @author Ivan Senic
 *
 */
public interface ClientRequestAdapter<C> extends RequestAdapter {

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
	 * Carrier for injecting the baggage.
	 *
	 * @return carrier
	 */
	C getCarrier();
}
