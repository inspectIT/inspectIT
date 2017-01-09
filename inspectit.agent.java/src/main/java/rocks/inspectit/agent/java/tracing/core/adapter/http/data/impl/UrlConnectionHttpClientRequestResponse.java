package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * The {@link HttpClientRequest} and {@link HttpResponse} implementation that works with
 * {@link java.net.HttpURLConnection}.
 *
 * @author Ivan Senic
 *
 */
public class UrlConnectionHttpClientRequestResponse implements HttpRequest, HttpResponse {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Http url connection, instance of java.net.HttpURLConnection.
	 */
	private final Object urlConnection;

	/**
	 * Default constructor.
	 *
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
	public String getUrl() {
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
	public int getStatus() {
		return (Integer) cache.invokeMethod(urlConnection.getClass(), "getResponseCode", new Class<?>[] {}, urlConnection, new Object[] {}, Integer.valueOf(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		return (String) cache.invokeMethod(urlConnection.getClass(), "getRequestMethod", new Class<?>[] {}, urlConnection, new Object[] {}, null);
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
		cache.invokeMethod(urlConnection.getClass(), "setRequestProperty", new Class<?>[] { String.class, String.class }, urlConnection, new Object[] { key, value }, null);
	}


}