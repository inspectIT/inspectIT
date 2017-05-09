package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.client.RemoteClientSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.ApacheHttpClientV40HttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.ApacheHttpClientV40HttpResponse;

/**
 * Remote client sensor for intercepting HTTP calls made with Apache HTTP client. Targeted to work
 * with all 4.x versions of Apache HTTP client. Currently supports only sync calls.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code org.apache.http.impl.client.CloseableHttpClient#doExecute(org.apache.http.HttpHost, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)}
 * <li>{@code org.apache.http.client.HttpClient#execute(org.apache.http.HttpHost, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)}
 * <li>{@code org.apache.http.client.RequestDirector#execute(org.apache.http.HttpHost, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)}
 * </ul>
 *
 * @author Ivan Senic
 */
public class ApacheHttpClientV40Sensor extends RemoteClientSensor implements ClientAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientRequestAdapter<TextMap> getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// Apache HTTP request is second parameter
		Object httpRequest = parameters[1];
		HttpRequest request = new ApacheHttpClientV40HttpClientRequest(httpRequest, CACHE);
		return new HttpClientRequestAdapter(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		// Apache HTTP response is result of method invocation
		if (exception) {
			// no delegation as we depend on result
			return new ThrowableAwareResponseAdapter(result.getClass().getSimpleName());
		} else {
			Object httpResponse = result;
			HttpResponse response = new ApacheHttpClientV40HttpResponse(httpResponse, CACHE);
			return new HttpResponseAdapter(response);
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
