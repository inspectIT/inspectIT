package rocks.inspectit.agent.java.tracing.core.adapter;

import rocks.inspectit.shared.all.tracing.data.ReferenceType;

/**
 * Client request requestAdapter works together with the
 * {@link rocks.inspectit.agent.java.tracing.core.ClientInterceptor} in order to correctly mark
 * client request. The requestAdapter is responsible providing {@link BaggageInjectAdapter} and for
 * providing the basic information about the request as propagation type, reference type and tags
 * associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @author Ivan Senic
 *
 */
public interface ClientRequestAdapter extends RequestAdapter {

	/**
	 * Returns reference type for the request. If request is synchronous it's expected that
	 * {@link ReferenceType#CHILD_OF} is returned. If request is asynchronous it's expected that
	 * {@link ReferenceType#FOLLOW_FROM} is returned.
	 *
	 * @return Returns reference type for the request.
	 */
	ReferenceType getReferenceType();

	/**
	 * Return the {@link BaggageInjectAdapter} so that tracing information can be added to the
	 * client request.
	 *
	 * @return {@link BaggageInjectAdapter}, must not be <code>null</code>
	 */
	BaggageInjectAdapter getBaggageInjectAdapter();
}
