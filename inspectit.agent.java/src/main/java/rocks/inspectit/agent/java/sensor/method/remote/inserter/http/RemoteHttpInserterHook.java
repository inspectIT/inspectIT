package rocks.inspectit.agent.java.sensor.method.remote.inserter.http;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteDefaultInserterHook;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;

/**
 * The hook is the implementation of http inserter. It puts the inspectIT header as additional
 * header/attribute to the remote call.
 *
 * @author Thomas Kluge
 *
 */
public abstract class RemoteHttpInserterHook extends RemoteDefaultInserterHook<RemoteHttpCallData> {

	/**
	 * Constructor.
	 *
	 * @param platformManager
	 *            The Platform manager
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	protected RemoteHttpInserterHook(IPlatformManager platformManager, RemoteIdentificationManager remoteIdentificationManager) {
		super(platformManager, remoteIdentificationManager);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Read the http response code from Webrequest. Implementation depends on the application
	 * server.
	 *
	 * @param object
	 *            The Object.
	 * @param parameters
	 *            The parameters of the method call.
	 * @param result
	 *            The result.
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
	 *            The result.
	 * @return The requested URL.
	 */
	protected abstract String readURL(Object object, Object[] parameters, Object result);

	/**
	 * The remote specific data for a http call are response code and url.
	 */
	@Override
	protected void addRemoteSpecificData(Object object, Object[] parameters, Object result) {
		RemoteHttpCallData data = this.threadRemoteCallData.get();
		data.setResponseCode(this.readResponseCode(object, parameters, result));
		data.setUrl(this.readURL(object, parameters, result));
	}

}
