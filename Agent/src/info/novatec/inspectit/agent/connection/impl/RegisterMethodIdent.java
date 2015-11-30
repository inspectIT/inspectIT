package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.cmr.service.IRegistrationService;

/**
 * Class which encapsulates the request to the remote object
 * {@link IRegistrationService#registerMethodIdent(long, String, String, String, java.util.List, String)}
 * .
 * 
 * @author Patrice Bouillet
 * 
 */
public class RegisterMethodIdent extends AbstractRemoteMethodCall<IRegistrationService, Long> {

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
	 * The only constructor for this class accepts three attributes. The first one is the remote
	 * object, which will be used to send the data. The second one is the sensor configuration which
	 * holds the data used for the registration. The last one is agent id.
	 * 
	 * @param registrationService
	 *            The remote object.
	 * @param sensorConfig
	 *            The sensor configuration.
	 * @param platformId
	 *            The ID of the platform.
	 */
	public RegisterMethodIdent(IRegistrationService registrationService, RegisteredSensorConfig sensorConfig, long platformId) {
		super(registrationService);
		this.rsc = sensorConfig;
		this.platformId = platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Long performRemoteCall(IRegistrationService remoteObject) {
		return Long.valueOf(remoteObject.registerMethodIdent(platformId, rsc.getTargetPackageName(), rsc.getTargetClassName(), rsc.getTargetMethodName(), rsc.getParameterTypes(), rsc.getReturnType(),
				rsc.getModifiers()));
	}
}
