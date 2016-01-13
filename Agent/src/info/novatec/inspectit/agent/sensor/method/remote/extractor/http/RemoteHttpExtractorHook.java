package info.novatec.inspectit.agent.sensor.method.remote.extractor.http;

import java.sql.Timestamp;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.http.StartEndMarker;
import info.novatec.inspectit.communication.data.RemoteCallData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implements the {@link RemoteHttpExtractorSensor} class. It extracts the inspectIT
 * header from a remote Call.
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
	private final IIdManager idManager;

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
	 * @param idManager
	 *            The id manager
	 * @param extractor
	 *            The Extractor
	 */
	public RemoteHttpExtractorHook(IIdManager idManager, RemoteHttpParameterExtractor extractor) {
		this.extractor = extractor;
		this.idManager = idManager;
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
			Class<?> servletRequestClass = httpServletRequest.getClass();

			RemoteCallData data = extractor.getRemoteCallData(servletRequestClass, httpServletRequest);

			// just save data if insptectItHeader is available, it makes no sense without the header
			if (data != null) {
				try {

					long platformId = idManager.getPlatformId();
					long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
					long registeredMethodId = idManager.getRegisteredMethodId(methodId);

					data.setPlatformIdent(platformId);
					data.setMethodIdent(registeredMethodId);
					data.setSensorTypeIdent(registeredSensorTypeId);
					data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

					// returning gathered information
					coreService.addMethodSensorData(registeredSensorTypeId, registeredMethodId, null, data);
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the remote call data because of an unavailable id. " + e.getMessage());
					}
				}
			}

		}
	}

}
