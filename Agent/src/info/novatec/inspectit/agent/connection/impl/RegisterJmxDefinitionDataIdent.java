package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.JmxSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This class is used to register the static information of a MBean once on the CMR.
 * 
 * @author Alfred Krauss
 * 
 */
public class RegisterJmxDefinitionDataIdent extends AbstractRemoteMethodCall {

	/**
	 * The registration object which is used for the actual registering.
	 */
	private final IRegistrationService registrationService;

	/**
	 * The ID of the current platform used for the registering process.
	 */
	private final long platformId;

	/**
	 * JmxSensorConfig of this data-set.
	 */
	private JmxSensorConfig jmxSensorConfig;

	/**
	 * The only constructor for this class accepts 3 attributes.
	 * 
	 * @param registrationService
	 *            The {@link Remote} object.
	 * @param config
	 *            The {@link JmxSensorConfig} which is registered at the server.
	 * @param platformId
	 *            The platformID which is registered at the server.
	 */
	public RegisterJmxDefinitionDataIdent(IRegistrationService registrationService, JmxSensorConfig config, long platformId) {
		jmxSensorConfig = config;
		this.platformId = platformId;
		this.registrationService = registrationService;
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
		return Long.valueOf(reg.registerJmxSensorDefinitionData(platformId, jmxSensorConfig.getmBeanObjectName(), jmxSensorConfig.getAttributeName(), jmxSensorConfig.getmBeanAttributeDescription(),
				jmxSensorConfig.getmBeanAttributeType(), jmxSensorConfig.getmBeanAttributeIsIs(), jmxSensorConfig.getmBeanAttributeIsReadable(), jmxSensorConfig.getmBeanAttributeIsWritable()));
	}
}