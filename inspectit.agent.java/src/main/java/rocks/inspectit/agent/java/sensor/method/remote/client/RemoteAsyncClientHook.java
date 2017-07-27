package rocks.inspectit.agent.java.sensor.method.remote.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * The hook is the default implementation of remote client for asynchronous requests. The hook works
 * with the {@link ClientInterceptor} in order to correctly handle client request firing in the
 * {@link #beforeBody(long, long, Object, Object[], RegisteredSensorConfig)}. Start and stop of the
 * span correlated to the request is not done in this hook.
 * <p>
 * The created spans will not be passed to the give core service, but instead must be intercepted at
 * the point of start / stop.
 *
 * @author Ivan Senic
 *
 */
public class RemoteAsyncClientHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteAsyncClientHook.class);

	/**
	 * Helps us to ensure that we only execute one remote client hook for each client request on all
	 * remote client sensor implementations.
	 * <p>
	 * Static on purpose.
	 */
	private static final StartEndMarker REF_MARKER = new StartEndMarker();

	/**
	 * {@link ClientInterceptor}.
	 */
	private final ClientInterceptor clientInterceptor;

	/**
	 * {@link AsyncClientAdapterProvider}.
	 */
	private final AsyncClientAdapterProvider asyncClientAdapterProvider;

	/**
	 * {@link IAsyncSpanContextListener} to report firing of the async spans.
	 */
	private final IAsyncSpanContextListener asyncSpanContextListener;

	/**
	 * Default constructor.
	 *
	 * @param clientInterceptor
	 *            Our client interceptor.
	 * @param asyncClientAdapterProvider
	 *            {@link AsyncClientAdapterProvider}
	 * @param asyncSpanContextListener
	 *            {@link IAsyncSpanContextListener} to report firing of the async spans.
	 *
	 */
	public RemoteAsyncClientHook(ClientInterceptor clientInterceptor, AsyncClientAdapterProvider asyncClientAdapterProvider, IAsyncSpanContextListener asyncSpanContextListener) {
		this.clientInterceptor = clientInterceptor;
		this.asyncClientAdapterProvider = asyncClientAdapterProvider;
		this.asyncSpanContextListener = asyncSpanContextListener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!REF_MARKER.isMarkerSet()) {
			// get requestAdapter and handle
			AsyncClientRequestAdapter<?> adapter = asyncClientAdapterProvider.getAsyncClientRequestAdapter(object, parameters, rsc);
			if (null != adapter) {
				SpanImpl span = clientInterceptor.handleAsyncRequest(adapter);

				if (null != span) {

					span.setTag(ExtraTags.INSPECTT_METHOD_ID, methodId);
					span.setTag(ExtraTags.INSPECTT_SENSOR_ID, sensorTypeId);

					asyncSpanContextListener.asyncSpanContextCreated(span.context());

					if (LOG.isDebugEnabled()) {
						LOG.debug("Remote async client hook before body span " + span);
					}
				}
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