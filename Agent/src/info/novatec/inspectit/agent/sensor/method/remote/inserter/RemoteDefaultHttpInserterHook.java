package info.novatec.inspectit.agent.sensor.method.remote.inserter;

import java.sql.Timestamp;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.RemoteCallData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook is the default implementation of http inserter. It puts the inspectIT header as
 * additional header/attribute to the remote call.
 * 
 * @author Thomas Kluge
 * 
 */
public abstract class RemoteDefaultHttpInserterHook implements IMethodHook {

	/**
	 * The logger of the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(RemoteDefaultHttpInserterHook.class);

	/**
	 * The ID manager.
	 */
	protected final IIdManager idManager;

	/**
	 * Thread local {@link RemoteCallData} object.
	 */
	protected final ThreadLocal<RemoteCallData> threadRemoteCallData = new ThreadLocal<RemoteCallData>();

	/**
	 * The remote identification manager.
	 */
	protected final RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * Constructor.
	 * 
	 * @param idManager
	 *            The id manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	protected RemoteDefaultHttpInserterHook(IIdManager idManager, RemoteIdentificationManager remoteIdentificationManager) {
		this.idManager = idManager;
		this.remoteIdentificationManager = remoteIdentificationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		insertInspectItHeader(methodId, sensorTypeId, object, parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {

		// nothing to do here
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
			long platformId = idManager.getPlatformId();

			inspectItHeader = platformId + ";" + identification;

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
	 * Read the http response code from Webrequest. Implementation depends on the application
	 * server.
	 * 
	 * @param object
	 *            The Object.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result 
	 * @return The http response code.
	 */
	protected abstract int readResponseCode(Object object, Object[] parameters, Object result);

	/**
	 * Read the URL Object from Webrequest. Implementation depends on the application server.
	 * 
	 * @param object
	 *            The Object.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result 
	 * @return The requested URL.
	 */
	protected abstract String readURL(Object object, Object[] parameters, Object result);

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {

		RemoteCallData data = this.threadRemoteCallData.get();

		if (data != null) {

			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);

				data.setPlatformIdent(platformId);
				data.setMethodIdent(registeredMethodId);
				data.setSensorTypeIdent(registeredSensorTypeId);
				data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

				// set RemoteCallData specific fields
				data.setCalling(true);
				data.setResponseCode(this.readResponseCode(object, parameters, result));
				data.setUrl(this.readURL(object, parameters, result));

				// returning gathered information
				coreService.addMethodSensorData(registeredSensorTypeId, registeredMethodId, null, data);

			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		}
	}
}
