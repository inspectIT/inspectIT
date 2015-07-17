package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class which encapsulates the request to the {@link Remote} object
 * {@link IRegistrationService#registerMethodIdent(long, String, String, String, java.util.List, String)}
 * .
 * 
 * @author Patrice Bouillet
 * 
 */
public class RegisterMethodIdent extends AbstractRemoteMethodCall {

	/**
	 * The registration object which is used for the actual registering.
	 */
	private final Remote registrationService;

	/**
	 * The sensor configuration which holds all the information about the sensor used for the
	 * registering process.
	 */
	private final RegisteredSensorConfig rsc;

	/**
	 * The ID of the current platform used for the registering process.
	 */
	private final long platformId;

	/**
	 * The only constructor for this class accepts two attributes. The first one is the
	 * {@link Remote} object, which will be used to send the data. The second one is the sensor
	 * configuration which holds the data used for the registration.
	 * 
	 * @param registrationService
	 *            The {@link Remote} object.
	 * @param sensorConfig
	 *            The sensor configuration.
	 * @param platformId
	 *            The ID of the platform.
	 */
	public RegisterMethodIdent(IRegistrationService registrationService, RegisteredSensorConfig sensorConfig, long platformId) {
		this.registrationService = registrationService;
		this.rsc = sensorConfig;
		this.platformId = platformId;
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

		return Long.valueOf(reg.registerMethodIdent(platformId, rsc.getTargetPackageName(), rsc.getTargetClassName(), rsc.getTargetMethodName(), rsc.getParameterTypes(), rsc.getReturnType(),
				rsc.getModifiers()));
	}
}
