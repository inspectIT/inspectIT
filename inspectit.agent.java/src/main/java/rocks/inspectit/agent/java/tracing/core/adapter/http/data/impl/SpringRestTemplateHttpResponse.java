package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpResponse} implementation that works with Spring Rest Template HTTP response.
 * Expects {@link org.springframework.http.client.ClientHttpResponse}.
 *
 * @author Ivan Senic
 *
 */
public class SpringRestTemplateHttpResponse implements HttpResponse {

	/**
	 * FQN of the org.springframework.http.client.ClientHttpResponse.
	 */
	private static final String ORG_SPRINGFRAMEWORK_HTTP_CLIENT_CLIENT_HTTP_RESPONSE = "org.springframework.http.client.ClientHttpResponse";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Spring http response, instance of org.springframework.http.client.ClientHttpResponse.
	 */
	private final Object springClientHttpResponse;

	/**
	 * @param springClientHttpResponse
	 *            Spring http response, instance of
	 *            org.springframework.http.client.ClientHttpResponse.
	 * @param cache
	 *            Reflection cache to use for method invocation.
	 */
	public SpringRestTemplateHttpResponse(Object springClientHttpResponse, ReflectionCache cache) {
		this.springClientHttpResponse = springClientHttpResponse;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return (Integer) cache.invokeMethod(springClientHttpResponse.getClass(), "getRawStatusCode", new Class[] {}, springClientHttpResponse, new Object[] {}, Integer.valueOf(0),
				ORG_SPRINGFRAMEWORK_HTTP_CLIENT_CLIENT_HTTP_RESPONSE);
	}

}
