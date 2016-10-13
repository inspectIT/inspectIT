package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * {@link AbstractSensorInstrumentationApplier} for the special assignments.
 *
 * @author Ivan Senic
 *
 */
public class SpecialInstrumentationApplier extends AbstractSensorInstrumentationApplier {

	/**
	 * {@link SpecialMethodSensorAssignment} to use.
	 */
	private final SpecialMethodSensorAssignment functionalAssignment;

	/**
	 * Default constructor.
	 *
	 * @param functionalAssignment
	 *            {@link SpecialMethodSensorAssignment} to use.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public SpecialInstrumentationApplier(SpecialMethodSensorAssignment functionalAssignment, Environment environment, IRegistrationService registrationService) {
		super(environment, registrationService);
		this.functionalAssignment = functionalAssignment;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractClassSensorAssignment<?> getSensorAssignment() {
		return functionalAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(ClassType classType) {
		return getClassSensorAssignmentFilter().matches(functionalAssignment, classType, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(MethodType methodType) {
		return getMethodSensorAssignmentFilter().matches(functionalAssignment, methodType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		SpecialInstrumentationPoint specialInstrumentationPoint = getOrCreateSpecialInstrumentationPoint(agentConfiguration, methodType, methodInstrumentationConfig);
		MethodSensorTypeConfig methodSensorTypeConfig = getSensorTypeConfigFromConfiguration(agentConfiguration);

		long sensorId = methodSensorTypeConfig.getId();
		specialInstrumentationPoint.setSensorId(sensorId);
	}

	/**
	 * Finds the proper sensor id from the agent configuration used for the
	 * {@link SpecialMethodSensorAssignment}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig}
	 * @return {@link MethodSensorTypeConfig} for the given assignment.
	 */
	private MethodSensorTypeConfig getSensorTypeConfigFromConfiguration(AgentConfig agentConfiguration) {
		IMethodSensorConfig methodSensorConfig = functionalAssignment.getSpecialMethodSensorConfig();
		return agentConfiguration.getSpecialMethodSensorTypeConfig(methodSensorConfig.getClassName());
	}
}
