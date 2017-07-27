package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import org.springframework.beans.factory.annotation.Autowired;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteAsyncClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.AsyncHttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.JettyHttpClientV61HttpClientRequest;

/**
 * Remote async client sensor for intercepting HTTP calls made with Jetty HTTP client. Targeted to
 * work with versions 7.x-8.x of Jetty HTTP client.
 * <p>
 * Targeted instrumentation method: names
 * <ul>
 * <li>{@code org.eclipse.jetty.client.HttpClient#send(org.eclipse.jetty.client.HttpExchange)}
 * </ul>
 *
 * @author Ivan Senic
 */
public class JettyHttpClientV61Sensor extends RemoteAsyncClientSensor implements AsyncClientAdapterProvider {

	/**
	 * {@link IRuntimeLinker} used to proxy jetty's event listener.
	 */
	@Autowired
	private IRuntimeLinker runtimeLinker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AsyncClientRequestAdapter<TextMap> getAsyncClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// http exchange is first parameter
		Object httpExchange = parameters[0];
		JettyHttpClientV61HttpClientRequest request = new JettyHttpClientV61HttpClientRequest(httpExchange, runtimeLinker, CACHE);
		return new AsyncHttpClientRequestAdapter(request, request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AsyncClientAdapterProvider getAsyncClientAdapterProvider() {
		return this;
	}

}
