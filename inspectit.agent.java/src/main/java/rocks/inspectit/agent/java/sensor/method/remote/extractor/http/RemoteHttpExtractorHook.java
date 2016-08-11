package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * The hook implements the {@link RemoteHttpExtractorSensor} class. It extracts the inspectIT header
 * from a remote Call.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpExtractorHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteHttpExtractorHook.class);

	/**
	 * The ID manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The extractor.
	 */
	private final RemoteHttpParameterExtractor extractor;

	/**
	 * Helps us to ensure that we only store on remote call per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param extractor
	 *            The Extractor
	 */
	public RemoteHttpExtractorHook(IPlatformManager platformManager, RemoteHttpParameterExtractor extractor) {
		this.extractor = extractor;
		this.platformManager = platformManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		refMarker.markCall();
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// no invocation marked -> skip
		if (!refMarker.isMarkerSet()) {
			return;
		}

		// remove mark from sub call
		refMarker.markEndCall();

	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// check if in the right(first) invocation
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {
			// call ended, remove the marker.
			refMarker.remove();

			// extract InspectItHeader Informations
			Object httpServletRequest = parameters[0];

			RemoteCallData data = extractor.getRemoteCallData(httpServletRequest);

			// just save data if insptectItHeader is available, it makes no sense without the header
			if (data != null) {
				try {

					long platformId = platformManager.getPlatformId();

					data.setPlatformIdent(platformId);
					data.setMethodIdent(methodId);
					data.setSensorTypeIdent(sensorTypeId);
					data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

					// returning gathered information
					coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.nanoTime()), data);
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the remote call data because of an unavailable id. " + e.getMessage());
					}
				}
			}

		}
	}

}