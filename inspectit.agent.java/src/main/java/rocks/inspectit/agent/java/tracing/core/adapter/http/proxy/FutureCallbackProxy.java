package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.agent.java.eum.reflection.CachedMethod;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.TagsProvidingAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl.ApacheHttpClientV40HttpResponse;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Proxy-class to wrap instances of the {@link org.apache.http.concurrent.FutureCallback} class.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
@ProxyFor(implementedInterfaces = "org.apache.http.concurrent.FutureCallback")
public class FutureCallbackProxy implements IProxySubject {

	/**
	 * Reflection cache of this class.
	 */
	private static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * See {@link org.apache.http.concurrent.FutureCallback#completed(java.lang.Object)}.
	 */
	private static CachedMethod<Void> mCompleted = new CachedMethod<Void>("org.apache.http.concurrent.FutureCallback", "completed", Object.class);

	/**
	 * See {@link org.apache.http.concurrent.FutureCallback#failed(java.lang.Exception)}.
	 */
	private static CachedMethod<Void> mFailed = new CachedMethod<Void>("org.apache.http.concurrent.FutureCallback", "failed", Exception.class);

	/**
	 * See {@link org.apache.http.concurrent.FutureCallback#cancelled()}.
	 */
	private static CachedMethod<Void> mCancelled = new CachedMethod<Void>("org.apache.http.concurrent.FutureCallback", "cancelled");

	/**
	 * Span store adapter that provides a span store.
	 */
	private SpanStoreAdapter spanStoreAdapter;

	/**
	 * The original future callback.
	 */
	private Object originalCallback;

	/**
	 * Constructor.
	 *
	 * @param originalCallback
	 *            the original future callback
	 * @param spanStoreAdapter
	 *            Span store adapter that provides a span store.
	 */
	public FutureCallbackProxy(Object originalCallback, SpanStoreAdapter spanStoreAdapter) {
		this.originalCallback = originalCallback;
		this.spanStoreAdapter = spanStoreAdapter;
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
		SpanStore spanStore = spanStoreAdapter.getSpanStore();
		if (spanStore != null) {
			spanStore.finishSpan(new HttpResponseAdapter(new ApacheHttpClientV40HttpResponse(response, CACHE)));
		}

		if (originalCallback != null) {
			mCompleted.callSafe(originalCallback, response);
		}
	}

	/**
	 * Failed method of FutureCallback.
	 *
	 * @param exception
	 *            Exception thrown when the request failed.
	 */
	@ProxyMethod(parameterTypes = { "java.lang.Exception" })
	public void failed(Object exception) {
		SpanStore spanStore = spanStoreAdapter.getSpanStore();
		if (spanStore != null) {
			spanStore.finishSpan(new ThrowableAwareResponseAdapter(exception.getClass().getSimpleName()));
		}

		if (originalCallback != null) {
			mFailed.callSafe(originalCallback, exception);
		}
	}

	/**
	 * Cancelled method for FutureCallback.
	 */
	@ProxyMethod()
	public void cancelled() {
		SpanStore spanStore = spanStoreAdapter.getSpanStore();
		if (spanStore != null) {
			spanStore.finishSpan(new TagsProvidingAdapter() {
				@Override
				public Map<String, String> getTags() {
					return ImmutableMap.of(SpanStoreAdapter.Constants.CANCEL, "true");
				}
			});
		}

		if (originalCallback != null) {
			mCancelled.callSafe(originalCallback);
		}
	}
}
