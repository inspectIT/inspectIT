package rocks.inspectit.agent.java.tracing.core.adapter.store;

import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * {@link SpanStoreAdapter} for the Apache's HttpContext.
 *
 * @author Marius Oehler
 *
 */
public class ApacheHttpContextSpanStoreAdapter implements SpanStoreAdapter {

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * The HTTP context to use for storing a span store.
	 */
	private final Object httpContext;

	/**
	 * Constructor.
	 *
	 * @param httpContext
	 *            the HTTP context to use for storing a span store
	 */
	public ApacheHttpContextSpanStoreAdapter(Object httpContext) {
		this.httpContext = httpContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpanStore getSpanStore() {
		try {
			return (SpanStore) cache.invokeMethod(httpContext.getClass(), "getAttribute", new Class<?>[] { String.class }, httpContext, new Object[] { SpanStoreAdapter.Constants.ID }, null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSpanStore(SpanStore spanStore) {
		cache.invokeMethod(httpContext.getClass(), "setAttribute", new Class<?>[] { String.class, Object.class }, httpContext, new Object[] { SpanStoreAdapter.Constants.ID, spanStore }, null);
	}
}
