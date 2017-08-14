package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.InvocationStartMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Special {@link MethodSensorInstrumentationApplier} for the invocation start assignment.
 *
 * @author Ivan Senic
 *
 */
public class InvocationStartMethodSensorInstrumentationApplier extends MethodSensorInstrumentationApplier {

	/**
	 * {@link InvocationStartMethodSensorAssignment} that defines instrumentation configuration.
	 */
	private final InvocationStartMethodSensorAssignment assignment;

	/**
	 * Default constructor.
	 *
	 * @param assignment
	 *            {@link InvocationStartMethodSensorAssignment} that defines instrumentation
	 *            configuration.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public InvocationStartMethodSensorInstrumentationApplier(InvocationStartMethodSensorAssignment assignment, Environment environment, IRegistrationService registrationService) {
		super(assignment, environment, registrationService);
		this.assignment = assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfig agentConfiguration, SensorInstrumentationPoint registeredSensorConfig) {
		// call super first
		super.applyAssignment(agentConfiguration, registeredSensorConfig);

		// check for invocation starts
		if (assignment.isStartsInvocation()) {
			// find the id of invocation sensor and only mark if one is found
			IMethodSensorConfig invocationSensorConfig = environment.getMethodSensorTypeConfig(InvocationSequenceSensorConfig.class);
			if (null != invocationSensorConfig) {
				MethodSensorTypeConfig invocationSensorTypeConfig = agentConfiguration.getMethodSensorTypeConfig(invocationSensorConfig.getClassName());
				registeredSensorConfig.addSensorId(invocationSensorTypeConfig.getId(), invocationSensorTypeConfig.getPriority());
				registeredSensorConfig.setStartsInvocation(true);
			}
		}
	}
}
