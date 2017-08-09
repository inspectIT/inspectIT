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
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.ClientHttpRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.SpringRestTemplateHttpClientRequest;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.SpringRestTemplateHttpResponse;

/**
 * Remote client sensor for intercepting HTTP synchronous calls made with Spring Rest Template
 * wrapper.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code org.springframework.http.client.ClientHttpRequest#execute()}
 * </ul>
 *
 * @author Ivan Senic
 */
public class SpringRestTemplateClientSensor extends RemoteClientSensor implements ClientAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientAdapterProvider getClientAdapterProvider() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientRequestAdapter<TextMap> getClientRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object request = object;
		ClientHttpRequest clientRequest = new SpringRestTemplateHttpClientRequest(request, CACHE);
		return new HttpClientRequestAdapter(clientRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getClientResponseAdapter(Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		if (exception) {
			// we can not delegate here as result is used
			return new ThrowableAwareResponseAdapter(result.getClass().getSimpleName());
		}
		Object response = result;
		HttpResponse httpResponse = new SpringRestTemplateHttpResponse(response, CACHE);
		return new HttpResponseAdapter(httpResponse);
	}

}
