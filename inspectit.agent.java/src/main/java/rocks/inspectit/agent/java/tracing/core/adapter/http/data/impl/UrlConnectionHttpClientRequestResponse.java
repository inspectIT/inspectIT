package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.sdk.opentracing.internal.constants.PropagationConstants;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;

/**
 * The {@link HttpClientRequest} and {@link HttpResponse} implementation that works with
 * {@link java.net.HttpURLConnection}.
 *
 * @author Ivan Senic
 *
 */
public class UrlConnectionHttpClientRequestResponse implements HttpRequest, HttpResponse {

	/**
	 * Http url connection, instance of java.net.HttpURLConnection.
	 */
	private final HttpURLConnection urlConnection;

	/**
	 * Default constructor.
	 *
	 * @param urlConnection
	 *            Http url connection, instance of java.net.HttpURLConnection.
	 */
	public UrlConnectionHttpClientRequestResponse(HttpURLConnection urlConnection) {
		this.urlConnection = urlConnection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean startClientSpan() {
		// here we do a small trick we try to set the request method to what already is
		// this will throw an exception if the connection can not be "changed" at this point of time
		// this way we ensure that no new span will be started if we can not modify the request
		try {
			urlConnection.setRequestMethod(urlConnection.getRequestMethod());

			// make sure no inspectit data exists in the request
			// otherwise request was already correctly populated
			return null == urlConnection.getRequestProperty(PropagationConstants.SPAN_ID);
		} catch (Exception e) { // NOPMD
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		URL url = urlConnection.getURL();
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
		try {
			return urlConnection.getResponseCode();
		} catch (IOException e) {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		return urlConnection.getRequestMethod();
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
		urlConnection.setRequestProperty(key, value);
	}

}