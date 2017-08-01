package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteAsyncClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.AsyncHttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.ApacheHttpClientV40HttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.store.ApacheHttpContextSpanStoreAdapter;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 *
 * Remote client sensor for intercepting asynchronous HTTP calls made with Apache HTTP client.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code org.apache.http.nio.client.HttpAsyncClient#execute(org.apache.http.HttpHost org.apache.http.HttpRequest org.apache.http.protocol.HttpContext org.apache.http.concurrent.FutureCallback)}
 * </ul>
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class ApacheAsyncHttpClientSensor extends RemoteAsyncClientSensor implements AsyncClientAdapterProvider {

	/**
	 * The reflection cache to use in this class.
	 */
	private static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AsyncClientRequestAdapter<TextMap> getAsyncClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object httpContext = parameters[2];
		SpanStoreAdapter spanStoreAdapter = new ApacheHttpContextSpanStoreAdapter(httpContext);

		Object httpRequest = parameters[1];
		ApacheHttpClientV40HttpClientRequest clientRequest = new ApacheHttpClientV40HttpClientRequest(httpRequest, CACHE);

		return new AsyncHttpClientRequestAdapter(clientRequest, spanStoreAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AsyncClientAdapterProvider getAsyncClientAdapterProvider() {
		return this;
	}
}
