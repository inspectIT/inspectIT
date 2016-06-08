package rocks.inspectit.agent.java.sensor.method.remote.inserter;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.http.StartEndMarker;
import rocks.inspectit.agent.java.sensor.method.remote.RemoteConstants;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * The hook is the default implementation of inserter. It puts the inspectIT header as additional
 * header/attribute to the remote call.
 *
 * @author Thomas Kluge
 *
 * @param <T>
 *            Subclass of {@link RemoteCallData}
 */
public abstract class RemoteDefaultInserterHook<T extends RemoteCallData> implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteDefaultInserterHook.class);

	/**
	 * Empty method signature.
	 */
	protected static final Class<?>[] METHOD_PARAMETER_EMPTY = new Class<?>[] {};

	/**
	 * Jetty HttpConnection specific method signature to get a header.
	 */
	protected static final Class<?>[] METHOD_PARAMETER_ONE_STRING_FIELD = new Class<?>[] { String.class };

	/**
	 * Jetty HttpConnection specific method signature to add InspechtITHeader.
	 */
	protected static final Class<?>[] METHOD_PARAMETER_TWO_STRING_FIELD = new Class<?>[] { String.class, String.class };

	/**
	 * The Platform manager.
	 */
	protected final IPlatformManager platformManager;

	/**
	 * Thread local {@link RemoteCallData} object.
	 */
	protected final ThreadLocal<T> threadRemoteCallData = new ThreadLocal<T>();

	/**
	 * Helps us to ensure that we only store on http metric per request.
	 */
	private final StartEndMarker refMarker = new StartEndMarker();

	/**
	 * The remote identification manager.
	 */
	protected final RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	protected RemoteDefaultInserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager) {
		this.platformManager = platformManager;
		this.remoteIdentificationManager = remoteIdentificationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		if (needToInsertInspectItHeader(object, parameters)) {
			insertInspectItHeader(methodId, sensorTypeId, object, parameters);
		}
		refMarker.markCall();
	}

	/**
	 * Checks if an inspectIT header is already in place.
	 *
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 * @return true if we need to insert an inspectIT header.
	 */
	protected abstract boolean needToInsertInspectItHeader(Object object, Object[] parameters);

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// remove mark from sub call
		refMarker.markEndCall();
	}

	/**
	 * Builds the InspectIT Header for http requests. The format of the header is
	 * "platformId;registeredSensorTypeId;registeredMethodId;timestamp"
	 *
	 * @param identification
	 *            The identification used as unique ID.
	 * @return The inspectItHeader as String.
	 */
	protected String getInspectItHeader(long identification) {

		String inspectItHeader = null;
		try {
			long platformId = platformManager.getPlatformId();

			inspectItHeader = StringUtils.join(new String[] { String.valueOf(platformId), String.valueOf(identification) }, RemoteConstants.CHAR_SPLIT_INSPECTIT_HEADER);

		} catch (IdNotAvailableException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
			}
		}

		return inspectItHeader;
	}

	/**
	 *
	 * Plattform dependent implemtation to insert the InspectItHeader into the http request.
	 *
	 * @param methodId
	 *            The unique method id.
	 * @param sensorTypeId
	 *            The unique sensor type id.
	 * @param object
	 *            The class itself which contains the hook.
	 * @param parameters
	 *            The parameters of the method call.
	 */
	protected abstract void insertInspectItHeader(long methodId, long sensorTypeId, Object object, Object[] parameters);

	/**
	 * Adds hook specific data to the {@link RemoteCallData}.
	 *
	 * @param object
	 *            The Object.
	 *
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result
	 *            The result.
	 */
	protected abstract void addRemoteSpecificData(Object object, Object[] parameters, Object result);

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {

		// check if in the right(first) invocation
		if (refMarker.isMarkerSet() && refMarker.matchesFirst()) {
			// call ended, remove the marker.
			refMarker.remove();
			T data = this.threadRemoteCallData.get();

			if (data != null) {

				try {
					long platformId = platformManager.getPlatformId();

					data.setPlatformIdent(platformId);
					data.setMethodIdent(methodId);
					data.setSensorTypeIdent(sensorTypeId);
					data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

					// set RemoteCallData specific fields
					data.setCalling(true);
					addRemoteSpecificData(object, parameters, result);

					// returning gathered information
					coreService.addMethodSensorData(sensorTypeId, methodId, String.valueOf(System.nanoTime()), data);

				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
					}
				}
			}
		}
	}
}
