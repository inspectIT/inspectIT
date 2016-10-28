package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The implementation of the {@link HttpResponse} that works with the
 * {@link org.apache.http.HttpResponse}.
 * 
 * @author Ivan Senic
 *
 */
public class ApacheHttpClientV40HttpResponse implements HttpResponse {

	/**
	 * FQN of the org.apache.http.StatusLine.
	 */
	private static final String ORG_APACHE_HTTP_STATUS_LINE_FQN = "org.apache.http.StatusLine";

	/**
	 * FQN of the org.apache.http.HttpResponse.
	 */
	private static final String ORG_APACHE_HTTP_HTTP_RESPONSE_FQN = "org.apache.http.HttpResponse";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Apache http response, instance of org.apache.http.HttpResponse.
	 */
	final Object apacheHttpResponse;

	/**
	 * @param apacheHttpResponse
	 *            Apache http response, instance of org.apache.http.HttpResponse.
	 * @param cache
	 *            reflection cache to use
	 */
	public ApacheHttpClientV40HttpResponse(Object apacheHttpResponse, ReflectionCache cache) {
		this.apacheHttpResponse = apacheHttpResponse;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		int result = 0;
		Object statusLine = cache.invokeMethod(apacheHttpResponse.getClass(), "getStatusLine", new Class<?>[] {}, apacheHttpResponse, new Object[] {}, null, ORG_APACHE_HTTP_HTTP_RESPONSE_FQN);
		if (null != statusLine) {
			result = (Integer) cache.invokeMethod(statusLine.getClass(), "getStatusCode", new Class<?>[] {}, statusLine, new Object[] {}, Integer.valueOf(0), ORG_APACHE_HTTP_STATUS_LINE_FQN);
		}
		return result;
	}

}