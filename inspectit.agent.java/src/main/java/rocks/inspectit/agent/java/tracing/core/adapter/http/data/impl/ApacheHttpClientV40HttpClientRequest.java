package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The implementation of the {@link HttpClientRequest} that works with the
 * {@link org.apache.http.HttpRequest}.
 *
 * @author Ivan Senic
 *
 */
public class ApacheHttpClientV40HttpClientRequest implements HttpClientRequest {

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
	public String getUri() {
		Object requestLine = cache.invokeMethod(apacheHttpRequest.getClass(), "getRequestLine", new Class<?>[] {}, apacheHttpRequest, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_REQUEST_FQN);
		if (null != requestLine) {
			return (String) cache.invokeMethod(requestLine.getClass(), "getUri", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putBaggageItem(String header, String value) {
		cache.invokeMethod(apacheHttpRequest.getClass(), "setHeader", new Class<?>[] { String.class, String.class }, apacheHttpRequest, new Object[] { header, value }, null,
				ORG_APACHE_HTTP_HTTP_MESSAGE_FQN);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not used currently.
	 */
	public String getHttpMethod() {
		Object requestLine = cache.invokeMethod(apacheHttpRequest.getClass(), "getRequestLine", new Class<?>[] {}, apacheHttpRequest, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_REQUEST_FQN);
		if (null != requestLine) {
			return (String) cache.invokeMethod(requestLine.getClass(), "getMethod", new Class<?>[] {}, requestLine, new Object[] {}, null, ORG_APACHE_HTTP_REQUEST_LINE_FQN);
		}
		return null;
	}

}