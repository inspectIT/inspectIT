package rocks.inspectit.agent.java.connection.impl;

import java.rmi.Remote;

import rocks.inspectit.agent.java.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.agent.java.connection.AbstractRemoteMethodCall;
import rocks.inspectit.shared.all.cmr.service.IRegistrationService;

/**
 * Class which encapsulates the request to the {@link Remote} object {@link IRegistrationService}.
 * The method to call is {@link IRegistrationService#registerPlatformSensorTypeIdent(long, String)}.
 * 
 * @author Alfred Krauss
 * 
 */
public class RegisterJmxSensorType extends AbstractRemoteMethodCall<IRegistrationService, Long> {

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
		super(registrationService);
		this.jmxSensorTypeConfig = jmxSensorTypeConfig;
		this.platformId = platformId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long performRemoteCall(IRegistrationService remoteObject) throws Exception {
		return Long.valueOf(remoteObject.registerJmxSensorTypeIdent(platformId, jmxSensorTypeConfig.getClassName()));
	}

}
