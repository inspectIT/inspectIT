package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;

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
		return "<init>".equals(methodType.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfiguration agentConfiguration, RegisteredSensorConfig registeredSensorConfig) {
		// there can be only one exception sensor so I just take the id
		ExceptionSensorTypeConfig exceptionSensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
		long sensorId = exceptionSensorTypeConfig.getId();

		if (registeredSensorConfig.addSensorId(sensorId, exceptionSensorTypeConfig.getPriority())) {
			// if this is new id for the sensor config then register mapping
			registrationService.addSensorTypeToMethod(sensorId, registeredSensorConfig.getId());
		}

		// add all settings
		registeredSensorConfig.addSettings(exceptionSensorAssignment.getSettings());
	}

}
