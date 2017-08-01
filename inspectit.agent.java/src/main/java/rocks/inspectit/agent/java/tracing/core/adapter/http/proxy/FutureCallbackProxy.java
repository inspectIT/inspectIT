package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;


import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * @author Isabel Vico Peinado
 *
 */
@ProxyFor(implementedInterfaces = "org.apache.http.concurrent.FutureCallback")
public class FutureCallbackProxy implements IProxySubject, HttpResponse {

	/**
	 * Span store that provides span that can be enriched.
	 */
	private SpanStore spanStore;

	/**
	 * @param spanStore
	 *            Span store that provides span.
	 */
	public FutureCallbackProxy(SpanStore spanStore) {
		this.spanStore = spanStore;
	}

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
	 * Completed method for FutureCallback.
	 *
	 * @param response
	 *            Response of the request.
	 */
	@ProxyMethod(parameterTypes = { "java.lang.Object" })
	public void completed(Object response) {
		spanStore.finishSpan(new HttpResponseAdapter(this));
	}

	/**
	 * Failed method of FutureCallback.
	 *
	 * @param exception
	 *            Exception thrown when the request failed.
	 */
	@ProxyMethod(parameterTypes = { "java.lang.Exception" })
	public void failed(Object exception) {
		spanStore.finishSpan(new ThrowableAwareResponseAdapter(exception.getClass().getSimpleName()));
	}

	/**
	 * Cancelled method for FutureCallback.
	 */
	@ProxyMethod()
	public void cancelled() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return 0;
	}
}
