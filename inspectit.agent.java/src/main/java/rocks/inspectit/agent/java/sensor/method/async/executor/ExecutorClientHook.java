package rocks.inspectit.agent.java.sensor.method.async.executor;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanBuilderImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * The executor client hook which injects the current span context into the first method parameter
 * if it is a {@link SpanStore}.
 *
 * @author Marius Oehler
 *
 */
public class ExecutorClientHook implements IMethodHook {

	/**
	 * Helps us to ensure that we only execute one remote client hook for each client request on all
	 * remote client sensor implementations.
	 * <p>
	 * Static on purpose.
	 */
	private static final StartEndMarker REF_MARKER = new StartEndMarker();

	/**
	 * Listener for firing async spans.
	 */
	private IAsyncSpanContextListener asyncSpanContextListener;

	/**
	 * The tracer.
	 */
	private TracerImpl tracer;

	/**
	 * Constructor.
	 *
	 * @param asyncSpanContextListener
	 *            the listener for async spans
	 * @param tracer
	 *            the tracer
	 */
	public ExecutorClientHook(IAsyncSpanContextListener asyncSpanContextListener, TracerImpl tracer) {
		this.asyncSpanContextListener = asyncSpanContextListener;
		this.tracer = tracer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!REF_MARKER.isMarkerSet()) {
			if ((parameters.length > 0) && (parameters[0] instanceof SpanStore)) {
				SpanBuilderImpl builder = tracer.buildSpan(null, References.FOLLOWS_FROM, true);
				builder.withTag(ExtraTags.PROPAGATION_TYPE, PropagationType.PROCESS.toString());
				builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
				builder.withTag(ExtraTags.INSPECTT_METHOD_ID, methodId);
				builder.withTag(ExtraTags.INSPECTT_SENSOR_ID, sensorTypeId);

				SpanImpl span = builder.build();

				SpanStore spanStore = (SpanStore) parameters[0];
				spanStore.storeSpan(span);

				asyncSpanContextListener.asyncSpanContextCreated(span.context());
			}
		}
		REF_MARKER.markCall();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		REF_MARKER.markEndCall();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) { // NOCHK:8-params
		// check if in the right(first) invocation
		if (REF_MARKER.isMarkerSet() && REF_MARKER.matchesFirst()) {
			// call ended, remove the marker.
			REF_MARKER.remove();
			// nothing else to do here
		}
	}
}
