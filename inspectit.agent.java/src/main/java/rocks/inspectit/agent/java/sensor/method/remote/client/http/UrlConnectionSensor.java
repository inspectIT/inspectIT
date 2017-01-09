package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import java.net.HttpURLConnection;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.empty.EmptyResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
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
	public ClientRequestAdapter<TextMap> getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		if (urlConnection instanceof HttpURLConnection) {
			HttpRequest request = new UrlConnectionHttpClientRequestResponse((HttpURLConnection) urlConnection);
			return new HttpClientRequestAdapter(request);
		} else {
			return EmptyRequestAdapter.INSTANCE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		Object urlConnection = object;
		if (urlConnection instanceof HttpURLConnection) {
			HttpResponse response = new UrlConnectionHttpClientRequestResponse((HttpURLConnection) urlConnection);
			return new HttpResponseAdapter(response);
		} else {
			return EmptyResponseAdapter.INSTANCE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientAdapterProvider getClientAdapterProvider() {
		return this;
	}

}
