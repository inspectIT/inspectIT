package rocks.inspectit.agent.java.sensor.method.remote.server;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.tracing.core.ServerInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.Span;

/**
 * The hook is the default implementation of remote server. The hook works with the
 * {@link ServerInterceptor} in order to correctly handle server request start in the
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
public class RemoteServerHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteServerHook.class);

	/**
	 * Helps us to ensure that we only execute one remote server hook for each server request on all
	 * remote server sensor implementations.
	 * <p>
	 * Static on purpose.
	 */
	private static final StartEndMarker REF_MARKER = new StartEndMarker();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * {@link ServerInterceptor}.
	 */
	private final ServerInterceptor serverInterceptor;

	/**
	 * {@link ClientAdapterProvider}.
	 */
	private final ServerAdapterProvider serverAdapterProvider;

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
	 * @param serverInterceptor
	 *            Our server interceptor.
	 * @param serverAdapterProvider
	 *            {@link ServerAdapterProvider}
	 * @param platformManager
	 *            The Platform manager
	 * @param timer
	 *            The timer used for accurate measuring.
	 *
	 */
	public RemoteServerHook(ServerInterceptor serverInterceptor, ServerAdapterProvider serverAdapterProvider, IPlatformManager platformManager, Timer timer) {
		this.serverInterceptor = serverInterceptor;
		this.serverAdapterProvider = serverAdapterProvider;
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
			ServerRequestAdapter adapter = serverAdapterProvider.getServerRequestAdapter(object, parameters, rsc);
			Span span = serverInterceptor.handleRequest(adapter);

			if (null != span) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Remote server hook before body span " + span);
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
			ResponseAdapter responseAdapter = serverAdapterProvider.getServerResponseAdapter(object, parameters, result, rsc);
			ServerSpan span = serverInterceptor.handleResponse(responseAdapter);

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
						LOG.debug("Remote server hook after body span " + span);
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