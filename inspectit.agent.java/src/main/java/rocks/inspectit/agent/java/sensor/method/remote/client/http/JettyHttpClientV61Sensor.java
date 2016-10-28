package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.AsyncHttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.AsyncHttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.JettyHttpClientV61HttpClientRequest;

/**
 * Remote client sensor for intercepting HTTP calls made with Jetty HTTP client. Targeted to work
 * with versions 6.x-8.x of Jetty HTTP client.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code org.mortbay.jetty.client.HttpClient#send(org.mortbay.jetty.client.HttpExchange)}
 * <li>{@code org.eclipse.jetty.client.HttpClient#send(org.eclipse.jetty.client.HttpExchange)}
 * </ul>
 *
 * @author Ivan Senic
 */
public class JettyHttpClientV61Sensor extends RemoteClientSensor implements ClientAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientRequestAdapter getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// http exchange is first parameter
		Object httpExchange = parameters[0];
		HttpClientRequest request = new JettyHttpClientV61HttpClientRequest(httpExchange, CACHE);
		return new AsyncHttpClientRequestAdapter(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// async client, empty response adapter
		HttpResponse response = AsyncHttpResponse.INSTANCE;
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
