package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.UrlConnectionHttpClientRequestResponse;

/**
 * Remote client sensor for intercepting HTTP calls made with Java's {@link java.net.URLConnection}.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code java.net.HttpURLConnection#connect()}
 * <li>{@code java.net.HttpURLConnection#getInputStream()}
 * <li>{@code java.net.HttpURLConnection#getOutputStream()}
 * </ul>
 *
 * @author Ivan Senic
 */
public class UrlConnectionSensor extends RemoteClientSensor implements ClientAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientRequestAdapter getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		HttpClientRequest request = new UrlConnectionHttpClientRequestResponse(urlConnection, CACHE);
		return new HttpClientRequestAdapter(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		HttpResponse response = new UrlConnectionHttpClientRequestResponse(urlConnection, CACHE);
		return new HttpResponseAdapter(response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientAdapterProvider getClientAdapterProvider() {
		return this;
	}

}
