package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Server request requestAdapter works together with the
 * {@link rocks.inspectit.agent.java.tracing.core.ServerInterceptor} in order to correctly retrieve
 * data from the server request. The requestAdapter is responsible for ejecting the span ident information
 * from the request and to provide basic information about the request as propagation type,
 * reference type and tags associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @author Ivan Senic
 *
 */
public interface ServerRequestAdapter extends RequestAdapter {

	/**
	 * Return the {@link BaggageExtractAdapter} so that tracing information can be read from the
	 * server request.
	 *
	 * @return {@link BaggageExtractAdapter}, must not be <code>null</code>
	 */
	BaggageExtractAdapter getBaggageExtractAdapter();

}
