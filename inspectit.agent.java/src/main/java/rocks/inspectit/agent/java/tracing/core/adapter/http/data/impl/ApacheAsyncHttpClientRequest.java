package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.ClientHttpRequest;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Isabel Vico Peinado
 *
 */
public class ApacheAsyncHttpClientRequest implements ClientHttpRequest, SpanStoreAdapter {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Name of the method of the request.
	 */
	private String method;

	/**
	 * URL of the request.
	 */
	@SuppressWarnings("PMD.AvoidStringBufferField")
	private StringBuilder url;
	/**
	 * Async http request object, instance of org.apache.http.protocol.HttpContext.
	 */
	private final Object httpContext;

	/**
	 * @param httpContext
	 *            Http context object, instance of org.apache.http.protocol.HttpContext.
	 * @param cache
	 *            reflection cache to use
	 */
	public ApacheAsyncHttpClientRequest(Object httpContext, ReflectionCache cache) {
		this.httpContext = httpContext;
		this.cache = cache;
	}

	/**
	 * Set URI to get it later.
	 *
	 * @param httpRequest
	 *            httpRequest to get the URI properly.
	 */
	public void setUri(Object httpRequest) {
		url = new StringBuilder();

		Object requestLine = cache.invokeMethod(httpRequest.getClass(), "getRequestLine", new Class<?>[] {}, httpRequest, new Object[] {}, null);
		if (null != requestLine) {
			Object uri = cache.invokeMethod(requestLine.getClass(), "getUri", new Class<?>[] {}, requestLine, new Object[] {}, null);
			if (null != uri) {
				url.append(uri);
			}
		}
	}

	/**
	 * Set method from the request.
	 *
	 * @param httpRequest
	 *            httpRequest to get the method properly.
	 */
	public void setMethod(Object httpRequest) {
		Object requestLine = cache.invokeMethod(httpRequest.getClass(), "getRequestLine", new Class<?>[] {}, httpRequest, new Object[] {}, null);
		if (null != requestLine) {
			method = (String) cache.invokeMethod(requestLine.getClass(), "getMethod", new Class<?>[] {}, requestLine, new Object[] {}, null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startClientSpan() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		return url.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		return method;
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
		cache.invokeMethod(httpContext.getClass(), "setRequestHeader", new Class<?>[] { String.class, String.class }, httpContext, new Object[] { key, value }, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanStore getSpanStore() {
		throw new UnsupportedOperationException("Apache async request does not support span store retrieving as it uses proxied listener for interception.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSpanStore(SpanStore spanStore) {
		cache.invokeMethod(httpContext.getClass(), "setAttribute", new Class<?>[] { String.class, Object.class }, httpContext, new Object[] { "spanStore", spanStore }, null);
	}
}
