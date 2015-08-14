package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.JmxSensorTypeConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Class which encapsulates the request to the {@link Remote} object {@link IRegistrationService}.
 * The method to call is {@link IRegistrationService#registerPlatformSensorTypeIdent(long, String)}.
 * 
 * @author Alfred Krauss
 * 
 */
public class RegisterJmxSensorType extends AbstractRemoteMethodCall {

	/**
	 * The registration object which is used for the actual registering.
	 */
	private final IRegistrationService registrationService;

	/**
	 * The platform sensor type configuration which is registered at the server.
	 */
	private final JmxSensorTypeConfig jmxSensorTypeConfig;

	/**
	 * The ID of the current platform used for the registering process.
	 */
	private final long platformId;

	/**
	 * The only constructor for this class accepts two attributes.
	 * 
	 * @param registrationService
	 *            The {@link Remote} object.
	 * @param jmxSensorTypeConfig
	 *            The {@link JmxSensorTypeConfig} which is registered at the server.
	 * @param platformId
	 *            The ID of the platform.
	 */
	public RegisterJmxSensorType(IRegistrationService registrationService, JmxSensorTypeConfig jmxSensorTypeConfig, long platformId) {
		this.registrationService = registrationService;
		this.jmxSensorTypeConfig = jmxSensorTypeConfig;
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
		return Long.valueOf(reg.registerJmxSensorTypeIdent(platformId, jmxSensorTypeConfig.getClassName()));
	}

}
