package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.cmr.service.IRegistrationService;

/**
 * Class which encapsulates the request to the remote object
 * {@link IRegistration#addTypeToSensor(String, String, String, int)}.
 * 
 * @author Patrice Bouillet
 */
public class AddSensorTypeToMethod extends AbstractRemoteMethodCall<IRegistrationService, Void> {

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
		super(registrationService);
		this.sensorTypeId = sensorTypeId;
		this.methodId = methodId;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Void performRemoteCall(IRegistrationService remoteObject) {
		remoteObject.addSensorTypeToMethod(sensorTypeId, methodId);
		return null;
	}

}
