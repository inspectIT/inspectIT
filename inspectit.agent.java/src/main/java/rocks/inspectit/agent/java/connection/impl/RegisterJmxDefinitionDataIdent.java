package rocks.inspectit.agent.java.connection.impl;

import java.rmi.Remote;

import rocks.inspectit.agent.java.config.impl.JmxSensorConfig;
import rocks.inspectit.agent.java.connection.AbstractRemoteMethodCall;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.service.IRegistrationService;

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
