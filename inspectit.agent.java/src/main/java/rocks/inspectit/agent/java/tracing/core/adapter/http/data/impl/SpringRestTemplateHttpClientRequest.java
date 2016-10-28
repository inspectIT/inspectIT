package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.net.URI;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} that works with Spring Rest Template HTTP request. Expects
 * {@link org.springframework.http.HttpRequest}.
 *
 * @author Ivan Senic
 *
 */
public class SpringRestTemplateHttpClientRequest implements HttpClientRequest {

	/**
	 * FQN of the org.springframework.http.HttpMessage.
	 */
	private static final String ORG_SPRINGFRAMEWORK_HTTP_HTTP_MESSAGE = "org.springframework.http.HttpMessage";

	/**
	 * FQN of the org.springframework.http.HttpRequest.
	 */
	private static final String ORG_SPRINGFRAMEWORK_HTTP_HTTP_REQUEST = "org.springframework.http.HttpRequest";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Spring http request, instance of org.springframework.http.HttpRequest.
	 */
	private final Object springHttpRequest;

	/**
	 * @param cache
	 *            Reflection cache to use for method invocation.
	 * @param springHttpRequest
	 *            Spring http request, instance of org.springframework.http.HttpRequest.
	 */
	public SpringRestTemplateHttpClientRequest(Object springHttpRequest, ReflectionCache cache) {
		this.springHttpRequest = springHttpRequest;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUri() {
		URI uri = (URI) cache.invokeMethod(springHttpRequest.getClass(), "getURI", new Class[] {}, springHttpRequest, new Object[] {}, null, ORG_SPRINGFRAMEWORK_HTTP_HTTP_REQUEST);
		if (null != uri) {
			return uri.toString();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putBaggageItem(String key, String value) {
		Object headers = cache.invokeMethod(springHttpRequest.getClass(), "getHeaders", new Class[] {}, springHttpRequest, new Object[] {}, null, ORG_SPRINGFRAMEWORK_HTTP_HTTP_MESSAGE);
		if (null != headers) {
			cache.invokeMethod(headers.getClass(), "set", new Class[] { String.class, String.class }, headers, new Object[] { key, value }, null);
		}
	}

}
