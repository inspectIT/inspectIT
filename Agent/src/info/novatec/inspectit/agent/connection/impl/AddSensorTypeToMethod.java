package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class which encapsulates the request to the {@link Remote} object
 * {@link IRegistration#addTypeToSensor(String, String, String, int)}.
 * 
 * @author Patrice Bouillet
 */
public class AddSensorTypeToMethod extends AbstractRemoteMethodCall {

	/**
	 * The registration object which is used for the actual registering.
	 */
	private final Remote registrationService;

	/**
	 * The id of the sensor type.
	 */
	private final long sensorTypeId;

	/**
	 * The id of the method.
	 */
	private final long methodId;

	/**
	 * The only available constructor for this object.
	 * 
	 * @param registrationService
	 *            The remote object.
	 * @param sensorTypeId
	 *            The id of the sensor type.
	 * @param methodId
	 *            The id of the method.
	 */
	public AddSensorTypeToMethod(IRegistrationService registrationService, long sensorTypeId, long methodId) {
		this.registrationService = registrationService;
		this.sensorTypeId = sensorTypeId;
		this.methodId = methodId;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Remote getRemoteObject() throws ServerUnavailableException {
		return registrationService;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Object performRemoteCall(Remote remoteObject) throws RemoteException {
		IRegistrationService reg = (IRegistrationService) remoteObject;
		reg.addSensorTypeToMethod(sensorTypeId, methodId);
		return null;
	}

}
