package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;

import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.JettyEventListenerProxy;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} implementation that works with Jetty Http client up to version 8.
 * We expect {@link org.eclipse.jetty.client.HttpExchange} as data provider.
 *
 * @author Ivan Senic
 *
 */
public class JettyHttpClientV61HttpClientRequest implements HttpRequest, SpanStoreAdapter {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Jetty http exchange object, instance of org.eclipse.jetty.client.HttpExchange.
	 */
	private final Object jettyHttpExchange;

	/**
	 * {@link IRuntimeLinker} used to proxy jetty's event listener.
	 */
	private final IRuntimeLinker runtimeLinker;

	/**
	 * @param jettyHttpExchange
	 *            Jetty http exchange object, instance of org.eclipse.jetty.client.HttpExchange.
	 * @param runtimeLinker
	 *            {@link IRuntimeLinker} used to proxy jetty's event listener.
	 * @param cache
	 *            reflection cache to use
	 */
	public JettyHttpClientV61HttpClientRequest(Object jettyHttpExchange, IRuntimeLinker runtimeLinker, ReflectionCache cache) {
		this.jettyHttpExchange = jettyHttpExchange;
		this.cache = cache;
		this.runtimeLinker = runtimeLinker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startClientSpan() {
		// http exchange does not provide easy way to find out if our headers have already be
		// inserted, so we assume it's true always
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		// jetty does not provide any method that returns complete URL, thus we need to construct it
		// on our own completely
		StringBuilder url = new StringBuilder();

		// first scheme
		// can not go through interface as we are getting concrete implementation
		Object scheme = cache.invokeMethod(jettyHttpExchange.getClass(), "getScheme", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
		if (null != scheme) {
			// call to array() to get the bytes, if returned use for constructing string
			byte[] schemeBytes = (byte[]) cache.invokeMethod(scheme.getClass(), "array", new Class<?>[] {}, scheme, new Object[] {}, null);
			if (ArrayUtils.isNotEmpty(schemeBytes)) {
				url.append(new String(schemeBytes));
				url.append("://");
			}
		}

		// then address
		Object address = cache.invokeMethod(jettyHttpExchange.getClass(), "getAddress", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
		if (null != address) {
			url.append(address);
		}

		// then uri
		Object uri = cache.invokeMethod(jettyHttpExchange.getClass(), "getRequestURI", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);
		if (null != uri) {
			url.append(uri);
		}

		if (url.length() > 0) {
			return url.toString();
		} else {
			return null;
		}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanStore getSpanStore() {
		// Jetty request does not support span store retrieving as it uses proxied listener for
		// interception
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSpanStore(SpanStore spanStore) {
		// get original request listener
		Object originalListener = cache.invokeMethod(jettyHttpExchange.getClass(), "getEventListener", new Class<?>[] {}, jettyHttpExchange, new Object[] {}, null);

		// create proxy for this listener
		JettyEventListenerProxy listenerProxy = new JettyEventListenerProxy(originalListener, spanStore);
		Object proxyObject = runtimeLinker.createProxy(JettyEventListenerProxy.class, listenerProxy, jettyHttpExchange.getClass().getClassLoader());

		// find the interface event listener interface, it's in the super-class of the proxy
		Class<?> eventListenerClass = proxyObject.getClass().getSuperclass().getInterfaces()[0];

		// replace with our listener
		cache.invokeMethod(jettyHttpExchange.getClass(), "setEventListener", new Class<?>[] { eventListenerClass }, jettyHttpExchange, new Object[] { proxyObject }, null);
	}

}
