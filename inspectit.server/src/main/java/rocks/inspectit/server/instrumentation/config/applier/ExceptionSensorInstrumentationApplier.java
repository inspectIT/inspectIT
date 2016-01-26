package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Instrumentation applier for the {@link ExceptionSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class ExceptionSensorInstrumentationApplier extends AbstractSensorInstrumentationApplier {

	/**
	 * {@link ExceptionSensorAssignment} that defines instrumentation configuration.
	 */
	private final ExceptionSensorAssignment exceptionSensorAssignment;

	/**
	 * Default constructor.
	 *
	 * @param exceptionSensorAssignment
	 *            {@link ExceptionSensorAssignment} that defines instrumentation configuration.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public ExceptionSensorInstrumentationApplier(ExceptionSensorAssignment exceptionSensorAssignment, Environment environment, IRegistrationService registrationService) {
		super(environment, registrationService);
		this.exceptionSensorAssignment = exceptionSensorAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractClassSensorAssignment<?> getSensorAssignment() {
		return exceptionSensorAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(ClassType classType) {
		return classType.isException() && getClassSensorAssignmentFilter().matches(exceptionSensorAssignment, classType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean matches(MethodType methodType) {
		// only real constructors, exclude static constructors here directly
		return MethodType.Character.CONSTRUCTOR.equals(methodType.getMethodCharacter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfig agentConfiguration, SensorInstrumentationPoint registeredSensorConfig) {
		// there can be only one exception sensor so I just take the id
		ExceptionSensorTypeConfig exceptionSensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
		long sensorId = exceptionSensorTypeConfig.getId();

		// set to rsc
		registeredSensorConfig.addSensorId(sensorId, exceptionSensorTypeConfig.getPriority());

		// add all settings
		registeredSensorConfig.addSettings(exceptionSensorAssignment.getSettings());
	}

}
