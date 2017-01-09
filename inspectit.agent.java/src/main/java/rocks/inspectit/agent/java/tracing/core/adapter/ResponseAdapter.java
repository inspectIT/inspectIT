package rocks.inspectit.agent.java.tracing.core.adapter;

/**
 * Response requestAdapter works together with the
 * {@link rocks.inspectit.agent.java.tracing.core.ClientInterceptor} or
 * {@link rocks.inspectit.agent.java.tracing.core.ServerInterceptor} in order to correctly mark
 * response receiving or client or server. The requestAdapter is only responsible for providing tags
 * associated with the response.
 * <p>
 * This class is inspired by the Zipkin/Brave implementation, but is adapted to our needs.
 *
 * @author Ivan Senic
 *
 */
public interface ResponseAdapter extends TagsProvidingAdapter {

}
