package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;

/**
 * Asynchronous HTTP response.
 *
 * @author Ivan Senic
 *
 */
public final class AsyncHttpResponse implements HttpResponse {

	/**
	 * Instance for usage.
	 */
	public static final AsyncHttpResponse INSTANCE = new AsyncHttpResponse();

	/**
	 * Private constructor, no initialization.
	 */
	private AsyncHttpResponse() {
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * Cannot read response code since is asynchronous connection.
	 */
	@Override
	public int getStatus() {
		return 0;
	}

}