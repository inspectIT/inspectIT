package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.ClientHttpRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} that works with Spring Rest Template HTTP request. Expects
 * {@link org.springframework.http.HttpRequest}.
 *
 * @author Ivan Senic
 *
 */
public class SpringRestTemplateHttpClientRequest implements ClientHttpRequest {

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
	public boolean startClientSpan() {
		// always start span when creating new http request
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		// String template provides complete URL with the getURI() method
		Object uri = cache.invokeMethod(springHttpRequest.getClass(), "getURI", new Class[] {}, springHttpRequest, new Object[] {}, null, ORG_SPRINGFRAMEWORK_HTTP_HTTP_REQUEST);
		if (null != uri) {
			return uri.toString();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		Object method = cache.invokeMethod(springHttpRequest.getClass(), "getMethod", new Class[] {}, springHttpRequest, new Object[] {}, null, ORG_SPRINGFRAMEWORK_HTTP_HTTP_REQUEST);
		if (null != method) {
			return method.toString();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Entry<String, String>> iterator() {
		throw new UnsupportedOperationException("Client request does not provide baggage iterator.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String key, String value) {
		Object headers = getHttpHeaders();
		if (null != headers) {
			cache.invokeMethod(headers.getClass(), "set", new Class[] { String.class, String.class }, headers, new Object[] { key, value }, null);
		}
	}

	private Object getHttpHeaders() {
		return cache.invokeMethod(springHttpRequest.getClass(), "getHeaders", new Class[] {}, springHttpRequest, new Object[] {}, null, ORG_SPRINGFRAMEWORK_HTTP_HTTP_MESSAGE);
	}

}
