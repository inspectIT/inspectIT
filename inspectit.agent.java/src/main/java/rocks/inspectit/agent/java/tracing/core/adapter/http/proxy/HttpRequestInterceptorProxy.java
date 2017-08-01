package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.store.ApacheHttpContextSpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * Proxy-class to wrap instances of the {@link org.apache.http.HttpRequestInterceptor} class.
 *
 * @author Isabel Vico Peinado
 *
 */
@ProxyFor(implementedInterfaces = "org.apache.http.HttpRequestInterceptor")
public class HttpRequestInterceptorProxy implements IProxySubject {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getProxyConstructorArguments() {
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
	}

	/**
	 * Request process. This is earliest place we can start a span.
	 *
	 * @param request
	 *            Original request
	 * @param context
	 *            Original Context
	 */
	@ProxyMethod(parameterTypes = { "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext" })
	public void process(Object request, Object context) {
		SpanStoreAdapter spanStoreAdapter = new ApacheHttpContextSpanStoreAdapter(context);
		SpanStore spanStore = spanStoreAdapter.getSpanStore();

		if (spanStore != null) {
			spanStore.startSpan();
		}
	}
}
