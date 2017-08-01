package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * @author Isabel Vico Peinado
 *
 */
@ProxyFor(implementedInterfaces = "org.apache.http.HttpRequestInterceptor")
public class HttpRequestInterceptorProxy implements IProxySubject {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private static final ReflectionCache CACHE = new ReflectionCache();

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
		SpanStore spanStore = (SpanStore) CACHE.invokeMethod(context.getClass(), "getAttribute", new Class<?>[] { String.class }, context, new Object[] { "spanStore" }, null);
		spanStore.startSpan();
	}
}
