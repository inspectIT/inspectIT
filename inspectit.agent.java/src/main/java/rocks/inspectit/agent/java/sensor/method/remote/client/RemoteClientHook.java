package rocks.inspectit.agent.java.sensor.method.remote.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.transformer.SpanTransformer;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * The hook is the default implementation of remote client. The hook works with the
 * {@link ClientInterceptor} in order to correctly handle client request start in the
 * {@link #beforeBody(long, long, Object, Object[], RegisteredSensorConfig)} and request end in the
 * {@link #secondAfterBody(ICoreService, long, long, Object, Object[], Object, boolean, RegisteredSensorConfig)}.
 * <p>
 * This hook measures also measures execution time.
 * <p>
 * The created spans will be passed to the give core service of the second after body method.
 *
 * @author Thomas Kluge
 * @author Ivan Senic
 *
 */
public class RemoteClientHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteClientHook.class);

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
	 * {@link ClientAdapterProvider}.
	 */
	private final ClientAdapterProvider clientAdapterProvider;

	/**
	 * The Platform manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The stack containing the span created by the {@link #clientInterceptor}.
	 */
	private final ThreadLocal<SpanImpl> spanStack = new ThreadLocal<SpanImpl>();

	/**
	 * Default constructor.
	 *
	 * @param clientInterceptor
	 *            Our client interceptor.
	 * @param clientAdapterProvider
	 *            {@link ClientAdapterProvider}
	 * @param platformManager
	 *            The Platform manager
	 *
	 */
	public RemoteClientHook(ClientInterceptor clientInterceptor, ClientAdapterProvider clientAdapterProvider, IPlatformManager platformManager) {
		this.clientInterceptor = clientInterceptor;
		this.clientAdapterProvider = clientAdapterProvider;
		this.platformManager = platformManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!REF_MARKER.isMarkerSet()) {
			// get requestAdapter and handle
			ClientRequestAdapter<?> adapter = clientAdapterProvider.getClientRequestAdapter(object, parameters, rsc);
			if (null != adapter) {
				SpanImpl span = clientInterceptor.handleRequest(adapter);

				if (null != span) {
					spanStack.set(span);

					if (LOG.isDebugEnabled()) {
						LOG.debug("Remote client hook before body span " + span);
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

			// extract span from thread local
			SpanImpl span = spanStack.get();

			if (null != span) {
				// get requestAdapter
				ResponseAdapter adapter = clientAdapterProvider.getClientResponseAdapter(object, parameters, result, exception, rsc);
				// only handle if request adapter is provided
				if (null != adapter) {
					clientInterceptor.handleResponse(span, adapter);
					spanStack.remove();

					if (LOG.isDebugEnabled()) {
						LOG.debug("Remote client hook after body span " + span);
					}

					AbstractSpan transformedSpan = SpanTransformer.transformSpan(span);
					transformedSpan.setPlatformIdent(platformManager.getPlatformId());
					transformedSpan.setMethodIdent(methodId);
					transformedSpan.setSensorTypeIdent(sensorTypeId);

					// add to core service (use span id as prefix)
					coreService.addDefaultData(transformedSpan);
				}
			}
		}
	}

}