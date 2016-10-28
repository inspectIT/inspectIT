package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.net.URL;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} and {@link HttpResponse} implementation that works with
 * {@link java.net.HttpURLConnection}.
 *
 * @author Ivan Senic
 *
 */
public class UrlConnectionHttpClientRequestResponse implements HttpClientRequest, HttpResponse {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Http url connection, instance of java.net.HttpURLConnection.
	 */
	private final Object urlConnection;

	/**
	 * @param urlConnection
	 *            Http url connection, instance of java.net.HttpURLConnection.
	 * @param cache
	 *            reflection cache to use
	 */
	public UrlConnectionHttpClientRequestResponse(Object urlConnection, ReflectionCache cache) {
		this.urlConnection = urlConnection;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUri() {
		URL url = (URL) cache.invokeMethod(urlConnection.getClass(), "getURL", new Class<?>[] {}, urlConnection, new Object[] {}, null);
		if (null != url) {
			return url.toString();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putBaggageItem(String header, String value) {
		cache.invokeMethod(urlConnection.getClass(), "setRequestProperty", new Class<?>[] { String.class, String.class }, urlConnection, new Object[] { header, value }, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return (Integer) cache.invokeMethod(urlConnection.getClass(), "getResponseCode", new Class<?>[] {}, urlConnection, new Object[] {}, Integer.valueOf(0));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not used currently.
	 */
	public String getHttpMethod() {
		return (String) cache.invokeMethod(urlConnection.getClass(), "getRequestMethod", new Class<?>[] {}, urlConnection, new Object[] {}, null);
	}


}