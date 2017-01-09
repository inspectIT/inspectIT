package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The implementation of the {@link HttpClientRequest} that works with the
 * {@link org.apache.http.HttpRequest}.
 *
 * @author Ivan Senic
 *
 */
public class ApacheHttpClientV40HttpClientRequest implements HttpRequest {

	/**
	 * FQN of the org.apache.http.HttpMessage.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_MESSAGE_FQN = "org.apache.http.HttpMessage";

	/**
	 * FQN of the org.apache.http.RequestLine.
	 */
	private static final String ORG_APACHE_HTTP_REQUEST_LINE_FQN = "org.apache.http.RequestLine";

	/**
	 * FQN of the org.apache.http.HttpRequest.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_REQUEST_FQN = "org.apache.http.HttpRequest";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Apache http request, instance of org.apache.http.HttpRequest.
	 */
	private final Object apacheHttpRequest;

	/**
	 * @param apacheHttpRequest
	 *            Apache http request, instance of org.apache.http.HttpRequest.
	 * @param cache
	 *            reflection cache to use
	 */
	public ApacheHttpClientV40HttpClientRequest(Object apacheHttpRequest, ReflectionCache cache) {
		this.apacheHttpRequest = apacheHttpRequest;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		// Apache provides complete URL as the URI (no other option)
		Object requestLine = getRequestLine();
		if (null != requestLine) {
			return (String) cache.invokeMethod(requestLine.getClass(), "getUri", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		Object requestLine = getRequestLine();
		if (null != requestLine) {
			return (String) cache.invokeMethod(requestLine.getClass(), "getMethod", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
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
		cache.invokeMethod(apacheHttpRequest.getClass(), "setHeader", new Class<?>[] { String.class, String.class }, apacheHttpRequest, new Object[] { key, value }, null,
				ORG_APACHE_HTTP_HTTP_MESSAGE_FQN);
	}

	/**
	 * @return Returns the request line from the request.
	 */
	private Object getRequestLine() {
		return cache.invokeMethod(apacheHttpRequest.getClass(), "getRequestLine", new Class<?>[] {}, apacheHttpRequest, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_REQUEST_FQN);
	}

}