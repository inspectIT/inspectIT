package rocks.inspectit.agent.java.sensor.method.async.http;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.store.ApacheHttpContextSpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Sensor hook for hooking into the generation of asynchronous HTTP requests in order to be able to
 * start spans for tracing purpose.
 * <p>
 * Thereto, the method <code>generateRequest</code> of the Apache's
 * <code>org.apache.http.impl.nio.client.AbstractClientExchangeHandler</code> class will be
 * instrumented.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class ApacheClientExchangeHandlerHook implements IMethodHook {

	/**
	 * Cache for caching the reflection calls.
	 */
	private static final ReflectionCache REFLECTION_CACHE = new ReflectionCache();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		Object httpContext = REFLECTION_CACHE.getField(object.getClass(), "localContext", object, null);

		if (httpContext != null) {
			SpanStoreAdapter spanStoreAdapter = new ApacheHttpContextSpanStoreAdapter(httpContext);
			SpanStore spanStore = spanStoreAdapter.getSpanStore();

			if (spanStore != null) {
				spanStore.startSpan();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {// NOCHK:8-params
	}
}
