package rocks.inspectit.agent.java.sensor.method.remote.server.http;

import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sensor.method.remote.server.RemoteServerSensor;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.JavaHttpResponse;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.JavaHttpServerRequest;

/**
 * Remote server sensor for intercepting HTTP calls using java HTTP servlet request and response.
 * <p>
 * Targeted instrumentation method:
 * <ul>
 * <li>{@code javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}
 * <li>{@code javax.servlet.FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}
 * <li>{@code javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * </ul>
 *
 * @author Ivan Senic
 */
public class JavaHttpRemoteServerSensor extends RemoteServerSensor implements ServerAdapterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ServerAdapterProvider getServerAdapterProvider() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerRequestAdapter<TextMap> getServerRequestAdapter(Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// request is first parameter
		Object httpServletRequest = parameters[0];
		JavaHttpServerRequest serverRequest = new JavaHttpServerRequest(httpServletRequest, CACHE);
		return new HttpServerRequestAdapter(serverRequest, serverRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAdapter getServerResponseAdapter(Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// response is second parameter
		Object httpServletResponse = parameters[1];
		JavaHttpResponse httpResponse = new JavaHttpResponse(httpServletResponse, CACHE);
		return new HttpResponseAdapter(httpResponse);
	}

}
