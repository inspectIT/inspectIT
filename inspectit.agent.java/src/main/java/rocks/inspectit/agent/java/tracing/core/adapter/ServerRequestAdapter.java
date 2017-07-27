package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Server request requestAdapter works together with the
 * {@link rocks.inspectit.agent.java.tracing.core.ServerInterceptor} in order to correctly retrieve
 * data from the server request. The requestAdapter is responsible for providing opentracing format
 * and carrier that can provide baggage that is propagated as well as for providing the basic
 * information about the request as propagation type and tags associated with the request.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @param <C>
 *            type of the carrier provided by this adapter
 * @author Ivan Senic
 *
 */
public interface ServerRequestAdapter<C> extends RequestAdapter<C> {

	/**
	 * Optionally provides store adapter for storing and querying span contexts. Implementations
	 * that do not provide context store must return
	 * {@link rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore}.
	 *
	 * @return {@link SpanContextStore}
	 */
	SpanContextStore getSpanContextStore();

}
