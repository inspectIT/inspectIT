package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Applier for the {@link JmxBeanSensorAssignment}.
 *
 * @see #addMonitoringPoint(AgentConfig, JmxAttributeDescriptor)
 * @author Ivan Senic
 */
public class JmxMonitoringApplier extends GenericApplier {

	/**
	 * Registration service.
	 */
	private IRegistrationService registrationService;

	/**
	 * {@link JmxBeanSensorAssignment}.
	 */
	private JmxBeanSensorAssignment jmxSensorAssignment;

	/**
	 * Default constructor.
	 *
	 * @param jmxSensorAssignment
	 *            Assignment that defines monitoring configuration.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public JmxMonitoringApplier(JmxBeanSensorAssignment jmxSensorAssignment, Environment environment, IRegistrationService registrationService) {
		super(environment);
		if (null == registrationService) {
			throw new IllegalArgumentException("Registration service can not be null in instrumentation applier.");
		}
		this.environment = environment;
		this.registrationService = registrationService;
		this.jmxSensorAssignment = jmxSensorAssignment;
	}

	/**
	 * Creates monitoring point for the {@link JmxAttributeDescriptor} if the one matches the
	 * definition provided in the {@link #jmxSensorAssignment}.
	 * <p>
	 * If monitoring point was added the given {@link JmxAttributeDescriptor} will have correctly
	 * set ID that is registered with the {@link #registrationService}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig} holding the agent id.
	 * @param jmxAttributeDescriptor
	 *            {@link JmxAttributeDescriptor}
	 * @return Returns <code>true</code> if the given {@link JmxAttributeDescriptor} should be
	 *         monitored, <code>false</code> otherwise
	 */
	public boolean addMonitoringPoint(AgentConfig agentConfiguration, JmxAttributeDescriptor jmxAttributeDescriptor) {
		if (matches(jmxAttributeDescriptor)) {
			// registration data
			String objectName = jmxAttributeDescriptor.getmBeanObjectName();
			String attributeName = jmxAttributeDescriptor.getAttributeName();
			String description = jmxAttributeDescriptor.getmBeanAttributeDescription();
			String type = jmxAttributeDescriptor.getmBeanAttributeType();
			boolean isIs = jmxAttributeDescriptor.ismBeanAttributeIsIs();
			boolean readable = jmxAttributeDescriptor.ismBeanAttributeIsReadable();
			boolean writable = jmxAttributeDescriptor.ismBeanAttributeIsWritable();

			// register and set ID
			long id = registrationService.registerJmxSensorDefinitionDataIdent(agentConfiguration.getPlatformId(), objectName, attributeName, description, type, isIs, readable, writable);
			jmxAttributeDescriptor.setId(id);

			return true;
		}
		return false;
	}

	/**
	 * If the assignment matches the {@link JmxAttributeDescriptor}.
	 * <p>
	 * By default uses filter provided by {@link #getJmxSensorAssignmentFilter()}.
	 *
	 * @param jmxAttributeDescriptor
	 *            Descriptor to check.
	 * @return True if the assignment matches the descriptor.
	 */
	protected boolean matches(JmxAttributeDescriptor jmxAttributeDescriptor) {
		return getJmxSensorAssignmentFilter().matches(jmxSensorAssignment, jmxAttributeDescriptor);
	}

	/**
	 * Gets {@link #jmxSensorAssignment}.
	 * 
	 * @return {@link #jmxSensorAssignment}
	 */
	public JmxBeanSensorAssignment getJmxSensorAssignment() {
		return this.jmxSensorAssignment;
	}

}
