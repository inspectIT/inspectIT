package rocks.inspectit.agent.java.sensor.method.remote.client;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.Span;

/**
 * The hook is the default implementation of remote client. The hook works with the
 * {@link ClientInterceptor} in order to correctly handle client request start in the
 * {@link #beforeBody(long, long, Object, Object[], RegisteredSensorConfig)} and request end in the
 * {@link #secondAfterBody(ICoreService, long, long, Object, Object[], Object, RegisteredSensorConfig)}.
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
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

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
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * Default constructor.
	 *
	 * @param clientInterceptor
	 *            Our client interceptor.
	 * @param clientAdapterProvider
	 *            {@link ClientAdapterProvider}
	 * @param platformManager
	 *            The Platform manager
	 * @param timer
	 *            The timer used for accurate measuring.
	 *
	 */
	public RemoteClientHook(ClientInterceptor clientInterceptor, ClientAdapterProvider clientAdapterProvider, IPlatformManager platformManager, Timer timer) {
		this.clientInterceptor = clientInterceptor;
		this.clientAdapterProvider = clientAdapterProvider;
		this.platformManager = platformManager;
		this.timer = timer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (!REF_MARKER.isMarkerSet()) {
			// get requestAdapter and handle
			ClientRequestAdapter adapter = clientAdapterProvider.getClientRequestAdapter(object, parameters, rsc);
			Span span = clientInterceptor.handleRequest(adapter);

			if (null != span) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Remote client hook before body span " + span);
				}
			}

			timeStack.push(new Double(timer.getCurrentTime()));
		}
		REF_MARKER.markCall();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		REF_MARKER.markEndCall();

		if (REF_MARKER.isMarkerSet() && REF_MARKER.matchesFirst()) {
			timeStack.push(new Double(timer.getCurrentTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// check if in the right(first) invocation
		if (REF_MARKER.isMarkerSet() && REF_MARKER.matchesFirst()) {
			// call ended, remove the marker.
			REF_MARKER.remove();

			// extract times from stacks
			double endTime = timeStack.pop().doubleValue();
			double startTime = timeStack.pop().doubleValue();

			// get requestAdapter and handle
			ResponseAdapter adapter = clientAdapterProvider.getClientResponseAdapter(object, parameters, result, rsc);
			ClientSpan span = clientInterceptor.handleResponse(adapter);

			if (null != span) {
				try {
					// set all needed stuff
					double duration = endTime - startTime;
					Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));
					span.setTimeStamp(timestamp);
					span.setDuration(duration);
					span.setPlatformIdent(platformManager.getPlatformId());
					span.setMethodIdent(methodId);
					span.setSensorTypeIdent(sensorTypeId);

					// add to core service (use span id as prefix)
					coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(span.getSpanIdent().getId()), span);

					if (LOG.isDebugEnabled()) {
						LOG.debug("Remote client hook after body span " + span);
					}
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
					}
				}
			}
		}
	}

}