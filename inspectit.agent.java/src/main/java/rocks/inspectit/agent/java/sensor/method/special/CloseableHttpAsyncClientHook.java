package rocks.inspectit.agent.java.sensor.method.special;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.FutureCallbackProxy;
import rocks.inspectit.agent.java.tracing.core.adapter.store.ApacheHttpContextSpanStoreAdapter;
import rocks.inspectit.agent.java.util.ClassReference;

/**
 * Hook that intercepts the execute method of the {@link CloseableHttpAsyncClient}.
 *
 * @author Isabel Vico Peinado
 *
 */
public class CloseableHttpAsyncClientHook implements ISpecialHook {

	/**
	 * The FQN of the future callback which will be proxied.
	 */
	private static final String FUTURE_CALLBACK_FQN = "org.apache.http.concurrent.FutureCallback";

	/**
	 * {@link IRuntimeLinker} used to proxy the interceptor of the class.
	 */
	private final IRuntimeLinker runtimeLinker;

	/**
	 * Reference to the future callback class.
	 */
	private ClassReference futureCallbackClass;

	/**
	 * Default constructor.
	 *
	 * @param runtimeLinker
	 *            Used for proxy the interceptor of the builder.
	 */
	public CloseableHttpAsyncClientHook(IRuntimeLinker runtimeLinker) {
		this.runtimeLinker = runtimeLinker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		if ((null != parameters) && (parameters.length == 4)) {
			// the HTTP context
			Object httpContext = parameters[2];

			// the original callback given by the user
			Object originalCallback = parameters[3];

			if (futureCallbackClass == null) {
				futureCallbackClass = new ClassReference(FUTURE_CALLBACK_FQN, object.getClass().getClassLoader());
			}

			if ((futureCallbackClass.get() == null) || !futureCallbackClass.get().isInstance(originalCallback)) {
				return null;
			}

			SpanStoreAdapter spanStoreAdapter = new ApacheHttpContextSpanStoreAdapter(httpContext);

			FutureCallbackProxy proxy = new FutureCallbackProxy(originalCallback, spanStoreAdapter);
			Object newProxy = runtimeLinker.createProxy(FutureCallbackProxy.class, proxy, object.getClass().getClassLoader());

			parameters[3] = newProxy;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}
}
