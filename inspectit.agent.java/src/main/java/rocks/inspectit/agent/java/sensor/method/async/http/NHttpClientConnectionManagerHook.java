package rocks.inspectit.agent.java.sensor.method.async.http;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.store.ApacheHttpContextSpanStoreAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * Hook that intercepts the addInterceptorFirst method of the {@link HttpAsyncClientBuilder}.
 *
 * @author Isabel Vico Peinado
 * @author Marius Oehler
 *
 */
public class NHttpClientConnectionManagerHook implements IMethodHook {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if ((parameters != null) && (parameters.length == 3)) {
			Object httpContext = parameters[2];

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
