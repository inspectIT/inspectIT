package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.JmxSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.IRegistrationService;

import java.rmi.Remote;

/**
 * This class is used to register the static information of a MBean once on the CMR.
 * 
 * @author Alfred Krauss
 * 
 */
public class RegisterJmxDefinitionDataIdent extends AbstractRemoteMethodCall<IRegistrationService, Long> {

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
		super(registrationService);
		this.jmxSensorConfig = config;
		this.platformId = platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long performRemoteCall(IRegistrationService remoteObject) throws Exception {
		JmxDefinitionDataIdent jmxDefinitionDataIdent = new JmxDefinitionDataIdent();
		jmxDefinitionDataIdent.setmBeanObjectName(jmxSensorConfig.getmBeanObjectName());
		jmxDefinitionDataIdent.setmBeanAttributeName(jmxSensorConfig.getAttributeName());
		jmxDefinitionDataIdent.setmBeanAttributeDescription(jmxSensorConfig.getmBeanAttributeDescription());
		jmxDefinitionDataIdent.setmBeanAttributeType(jmxSensorConfig.getmBeanAttributeType());
		jmxDefinitionDataIdent.setmBeanAttributeIsIs(jmxSensorConfig.getmBeanAttributeIsIs());
		jmxDefinitionDataIdent.setmBeanAttributeIsReadable(jmxSensorConfig.getmBeanAttributeIsReadable());
		jmxDefinitionDataIdent.setmBeanAttributeIsWritable(jmxSensorConfig.getmBeanAttributeIsWritable());

		return Long.valueOf(remoteObject.registerJmxSensorDefinitionDataIdent(platformId, jmxSensorConfig.getmBeanObjectName(), jmxSensorConfig.getAttributeName(),
				jmxSensorConfig.getmBeanAttributeDescription(), jmxSensorConfig.getmBeanAttributeType(), jmxSensorConfig.getmBeanAttributeIsIs(), jmxSensorConfig.getmBeanAttributeIsReadable(),
				jmxSensorConfig.getmBeanAttributeIsWritable()));
	}
}
