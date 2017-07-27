package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import java.io.IOException;

import rocks.inspectit.agent.java.eum.reflection.CachedMethod;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * The proxy of the Jetty request event listener. This proxy extends the
 * {@link org.eclipse.jetty.client.HttpEventListenerWrapper} object and intercepts methods that are
 * interesting for the manipulation of the span responsible for this request. All calls are
 * delegated also to the original listener.
 * <p>
 * Currently we have the following interception points:
 * <ul>
 * <li>onRequestCommitted() - start the span
 * <li>onResponseStatus() - capture the response status
 * <li>onResponseComplete() - finish the span
 * <li>onConnectionFailed() and onException() - handle the exceptional situations
 * </ul>
 *
 * @author Ivan Senic
 *
 */
@ProxyFor(superClass = "org.eclipse.jetty.client.HttpEventListenerWrapper", constructorParameterTypes = { "org.eclipse.jetty.client.HttpEventListener", "boolean" })
public class JettyEventListenerProxy implements IProxySubject, HttpResponse {

	/**
	 * Original jetty request event listener.
	 */
	private Object originalListener;

	/**
	 * Span store that provides span that can be enriched.
	 */
	private SpanStore spanStore;

	/**
	 * Response code status.
	 */
	private int status;

	/**
	 * @param originalListener
	 *            Original jetty request event listener.
	 * @param spanStore
	 *            Span store that provides span that can be enriched.
	 */
	public JettyEventListenerProxy(Object originalListener, SpanStore spanStore) {
		this.originalListener = originalListener;
		this.spanStore = spanStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getProxyConstructorArguments() {
		// the wrapper should delegate the calls only if original listener is not null
		boolean delegating = null != originalListener;
		return new Object[] { originalListener, delegating };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return status;
	}

	/**
	 * Request committed event. This is earliest place we can start a span.
	 *
	 * @throws IOException
	 *             IOException
	 */
	@ProxyMethod()
	public void onRequestCommitted() throws IOException {
		spanStore.startSpan();

		if (null != originalListener) {
			WHttpEventListenerWrapper.ON_REQUEST_COMMITED.call(originalListener);
		}
	}

	/**
	 * Response status event. We can capture status here.
	 *
	 * @param version
	 *            version
	 * @param status
	 *            status
	 * @param reason
	 *            reason
	 * @throws IOException
	 *             IOException
	 */
	@ProxyMethod(parameterTypes = { "org.eclipse.jetty.io.Buffer", "int", "org.eclipse.jetty.io.Buffer" })
	public void onResponseStatus(Object version, int status, Object reason) throws IOException {
		this.status = status;

		if (null != originalListener) {
			WHttpEventListenerWrapper.ON_RESPONSE_STATUS.call(originalListener, version, status, reason);
		}
	}

	/**
	 * Response complete event. We can finish span here.
	 *
	 * @throws IOException
	 *             IOException
	 */
	@ProxyMethod
	public void onResponseComplete() throws IOException {
		spanStore.finishSpan(new HttpResponseAdapter(this));

		if (null != originalListener) {
			WHttpEventListenerWrapper.ON_RESPONSE_COMPLETE.call(originalListener);
		}
	}

	/**
	 * Connection failed.
	 *
	 * @param ex
	 *            Throwable
	 */
	@ProxyMethod(parameterTypes = "java.lang.Throwable")
	public void onConnectionFailed(Throwable ex) {
		handleThrowable(ex);

		if (null != originalListener) {
			WHttpEventListenerWrapper.ON_CONNECTION_FAILED.call(originalListener, ex);
		}
	}

	/**
	 * Exception occurred.
	 *
	 * @param ex
	 *            Throwable
	 */
	@ProxyMethod(parameterTypes = "java.lang.Throwable")
	public void onException(Throwable ex) {
		handleThrowable(ex);

		if (null != originalListener) {
			WHttpEventListenerWrapper.ON_EXCEPTION.call(originalListener, ex);
		}
	}

	/**
	 * Handling exceptional situations.
	 *
	 * @param throwable
	 *            Throwable
	 */
	private void handleThrowable(Throwable throwable) {
		spanStore.finishSpan(new ThrowableAwareResponseAdapter(throwable.getClass().getSimpleName()));
	}

	/**
	 * Gets {@link #originalListener}.
	 *
	 * @return {@link #originalListener}
	 */
	public Object getOriginalListener() {
		return this.originalListener;
	}

	/**
	 * Gets {@link #spanStore}.
	 *
	 * @return {@link #spanStore}
	 */
	public SpanStore getSpanStore() {
		return this.spanStore;
	}

	/**
	 * Wrapper for the org.eclipse.jetty.client.HttpEventListenerWrapper.
	 *
	 * @author Ivan Senic
	 *
	 */
	private interface WHttpEventListenerWrapper {

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener}.
		 */
		String CLAZZ = "org.eclipse.jetty.client.HttpEventListener";

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener#onRequestCommited()}.
		 */
		CachedMethod<Void> ON_REQUEST_COMMITED = new CachedMethod<Void>(CLAZZ, "onRequestCommitted");

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener#onResponseStatus()}.
		 */
		CachedMethod<Void> ON_RESPONSE_STATUS = new CachedMethod<Void>(CLAZZ, "onResponseStatus", "org.eclipse.jetty.io.Buffer", "int", "org.eclipse.jetty.io.Buffer");

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener#onResponseComplete()}.
		 */
		CachedMethod<Void> ON_RESPONSE_COMPLETE = new CachedMethod<Void>(CLAZZ, "onResponseComplete");

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener#onConnectionFailed()}.
		 */
		CachedMethod<Void> ON_CONNECTION_FAILED = new CachedMethod<Void>(CLAZZ, "onConnectionFailed", Throwable.class);

		/**
		 * See {@link org.eclipse.jetty.client.HttpEventListener#onException()}.
		 */
		CachedMethod<Void> ON_EXCEPTION = new CachedMethod<Void>(CLAZZ, "onException", Throwable.class);
	}

}
