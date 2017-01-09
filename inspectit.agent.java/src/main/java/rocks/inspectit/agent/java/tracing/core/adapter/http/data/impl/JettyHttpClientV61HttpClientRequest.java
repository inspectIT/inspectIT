package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} implementation that works with Jetty Http client up to version 8.
 * We expected either {@link org.mortbay.jetty.client.HttpExchange} or
 * {@link org.eclipse.jetty.client.HttpExchange} as data provider.
 *
 * @author Ivan Senic
 *
 */
public class JettyHttpClientV61HttpClientRequest implements HttpRequest {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Jetty http exchange object, instance of org.mortbay.jetty.client.HttpExchange or
	 * org.eclipse.jetty.client.HttpExchange.
	 */
	private final Object jettyHttpExchange;

	/**
	 * @param jettyHttpExchange
	 *            Jetty http exchange object, instance of org.mortbay.jetty.client.HttpExchange or
	 *            org.eclipse.jetty.client.HttpExchange.
	 * @param cache
	 *            reflection cache to use
	 */
	public JettyHttpClientV61HttpClientRequest(Object jettyHttpExchange, ReflectionCache cache) {
		this.jettyHttpExchange = jettyHttpExchange;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUri() {
		// can not go through interface as we are getting concrete implementation
		return (String) cache.invokeMethod(jettyHttpExchange.getClass(), "getRequestURI", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		return (String) cache.invokeMethod(jettyHttpExchange.getClass(), "getMethod", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
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
		// can not go through interface as we are getting concrete implementation
		cache.invokeMethod(jettyHttpExchange.getClass(), "setRequestHeader", new Class<?>[] { String.class, String.class }, jettyHttpExchange, new Object[] { key, value }, null);
	}

}
