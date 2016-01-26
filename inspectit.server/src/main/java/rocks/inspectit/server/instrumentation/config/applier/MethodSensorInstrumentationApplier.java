package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Applier for the {@link MethodSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class MethodSensorInstrumentationApplier extends AbstractSensorInstrumentationApplier {

	/**
	 * Method sensor assignment to work with.
	 */
	private final MethodSensorAssignment methodSensorAssignment;

	/**
	 * Default constructor.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment} that defines instrumentation configuration.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public MethodSensorInstrumentationApplier(MethodSensorAssignment methodSensorAssignment, Environment environment, IRegistrationService registrationService) {
		super(environment, registrationService);
		this.methodSensorAssignment = methodSensorAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractClassSensorAssignment<?> getSensorAssignment() {
		return methodSensorAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(ClassType classType) {
		return getClassSensorAssignmentFilter().matches(methodSensorAssignment, classType, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(MethodType methodType) {
		return getMethodSensorAssignmentFilter().matches(methodSensorAssignment, methodType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfig agentConfiguration, SensorInstrumentationPoint registeredSensorConfig) {
		// first deal with sensor id
		MethodSensorTypeConfig methodSensorTypeConfig = getSensorTypeConfigFromConfiguration(agentConfiguration, environment, methodSensorAssignment);
		long sensorId = methodSensorTypeConfig.getId();

		// set to rsc
		registeredSensorConfig.addSensorId(sensorId, methodSensorTypeConfig.getPriority());

		// add all settings
		registeredSensorConfig.addSettings(methodSensorAssignment.getSettings());
	}

	/**
	 * Finds the proper sensor id from the agent configuration and the environment used for the
	 * {@link MethodSensorAssignment}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig}
	 * @param environment
	 *            {@link Environment}
	 * @param assignment
	 *            {@link MethodSensorAssignment}
	 * @return {@link MethodSensorTypeConfig} for the given assignment.
	 */
	private MethodSensorTypeConfig getSensorTypeConfigFromConfiguration(AgentConfig agentConfiguration, Environment environment, MethodSensorAssignment assignment) {
		IMethodSensorConfig methodSensorConfig = environment.getMethodSensorTypeConfig(assignment.getSensorConfigClass());
		return agentConfiguration.getMethodSensorTypeConfig(methodSensorConfig.getClassName());
	}
}
