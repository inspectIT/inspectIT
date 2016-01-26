package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;

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
		return getClassSensorAssignmentFilter().matches(methodSensorAssignment, classType);
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
	protected void applyAssignment(AgentConfiguration agentConfiguration, RegisteredSensorConfig registeredSensorConfig) {
		// first deal with sensor id
		MethodSensorTypeConfig methodSensorTypeConfig = getSensorTypeConfigFromConfiguration(agentConfiguration, environment, methodSensorAssignment);
		long sensorId = methodSensorTypeConfig.getId();
		if (registeredSensorConfig.addSensorId(sensorId, methodSensorTypeConfig.getPriority())) {
			// if this is new id for the sensor config then register mapping
			registrationService.addSensorTypeToMethod(sensorId, registeredSensorConfig.getId());
		}

		// add all settings
		registeredSensorConfig.addSettings(methodSensorAssignment.getSettings());
	}

	/**
	 * Finds the proper sensor id from the agent configuration and the environment used for the
	 * {@link MethodSensorAssignment}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfiguration}
	 * @param environment
	 *            {@link Environment}
	 * @param assignment
	 *            {@link MethodSensorAssignment}
	 * @return {@link MethodSensorTypeConfig} for the given assignment.
	 */
	private MethodSensorTypeConfig getSensorTypeConfigFromConfiguration(AgentConfiguration agentConfiguration, Environment environment, MethodSensorAssignment assignment) {
		// TODO !!!!!!!! pass another string to the AbstractSensorTypeConfig this way we can easily
		// remove Environment from appliers totally

		IMethodSensorConfig methodSensorConfig = environment.getMethodSensorTypeConfig(assignment.getSensorConfigClass());
		return agentConfiguration.getMethodSensorTypeConfig(methodSensorConfig.getClassName());
	}
}
