package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.cmr.service.IRegistrationService;

/**
 * Class which encapsulates the request to the remote object {@link IRegistrationService}. The
 * method to call is {@link IRegistrationService#registerPlatformSensorTypeIdent(long, String)}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class RegisterPlatformSensorType extends AbstractRemoteMethodCall<IRegistrationService, Long> {

	/**
	 * The platform sensor type configuration which is registered at the server.
	 */
	private final PlatformSensorTypeConfig platformSensorTypeConfig;

	/**
	 * The ID of the current platform used for the registering process.
	 */
	private final long platformId;

	/**
	 * The only constructor for this class accepts three attributes.
	 * 
	 * @param registrationService
	 *            The remote object.
	 * @param platformSensorTypeConfig
	 *            The {@link PlatformSensorTypeConfig} which is registered at the server.
	 * @param platformId
	 *            The ID of the platform.
	 */
	public RegisterPlatformSensorType(IRegistrationService registrationService, PlatformSensorTypeConfig platformSensorTypeConfig, long platformId) {
		super(registrationService);
		this.platformSensorTypeConfig = platformSensorTypeConfig;
		this.platformId = platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Long performRemoteCall(IRegistrationService remoteObject) {
		return Long.valueOf(remoteObject.registerPlatformSensorTypeIdent(platformId, platformSensorTypeConfig.getClassName()));
	}

}
