package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.MethodType.Character;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.SensorInstrumentationPoint;

/**
 * Abstract classes for instrumentation that work with our {@link SensorInstrumentationPoint}.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractSensorInstrumentationApplier extends AbstractInstrumentationApplier {

	/**
	 * Registration service needed for registration of the IDs.
	 */
	protected IRegistrationService registrationService;

	/**
	 * Default constructor.
	 *
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 *
	 */
	public AbstractSensorInstrumentationApplier(Environment environment, IRegistrationService registrationService) {
		super(environment);

		if (null == registrationService) {
			throw new IllegalArgumentException("Registration service can not be null in instrumentation applier.");
		}
		this.registrationService = registrationService;
	}

	/**
	 * Applies the assignment of this applier to the {@link SensorInstrumentationPoint}.
	 *
	 * @param agentConfiguration
	 *            Agent configuration being used.
	 * @param registeredSensorConfig
	 *            {@link SensorInstrumentationPoint}.
	 */
	protected abstract void applyAssignment(AgentConfiguration agentConfiguration, SensorInstrumentationPoint registeredSensorConfig);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		SensorInstrumentationPoint registeredSensorConfig = getOrCreateRegisteredSensorConfig(agentConfiguration, methodType, methodInstrumentationConfig);
		// first check for the enhanced exception
		checkForEnhancedExceptionSensor(registeredSensorConfig, agentConfiguration);
		// then apply additional based on assignment
		applyAssignment(agentConfiguration, registeredSensorConfig);
	}

	/**
	 * Checks for the enhanced exception sensor in environment and sets the proper value in the
	 * {@link SensorInstrumentationPoint}.
	 *
	 * @param registeredSensorConfig
	 *            {@link SensorInstrumentationPoint}
	 * @param agentConfiguration
	 *            used configuration
	 */
	private void checkForEnhancedExceptionSensor(SensorInstrumentationPoint registeredSensorConfig, AgentConfiguration agentConfiguration) {
		ExceptionSensorTypeConfig exceptionSensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
		registeredSensorConfig.setEnhancedExceptionSensor(null != exceptionSensorTypeConfig && exceptionSensorTypeConfig.isEnhanced());
	}

	/**
	 * Checks if the {@link SensorInstrumentationPoint} exists in the {@link MethodType}. If not new one
	 * is created, registered with registration service and saved in the {@link MethodType}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfiguration} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return {@link SensorInstrumentationPoint} for the {@link MethodType}.
	 */
	private SensorInstrumentationPoint getOrCreateRegisteredSensorConfig(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// check for existing
		SensorInstrumentationPoint registeredSensorConfig = methodInstrumentationConfig.getSensorInstrumentationPoint();

		// if not create new one
		if (null == registeredSensorConfig) {
			// if not create new and register

			// extract package and class name
			String fqn = methodInstrumentationConfig.getTargetClassFqn();
			int index = fqn.lastIndexOf('.');
			String packageName = fqn.substring(0, index);
			String className = fqn.substring(index + 1);

			long id = registrationService.registerMethodIdent(agentConfiguration.getPlatformId(), packageName, className, methodType.getName(), methodType.getParameters(), methodType.getReturnType(),
					methodType.getModifiers());

			registeredSensorConfig = new SensorInstrumentationPoint();
			registeredSensorConfig.setId(id);
			if (Character.CONSTRUCTOR.equals(methodType.getMethodCharacter())) {
				registeredSensorConfig.setConstructor(true);
			}

			// set to method instrumentation
			methodInstrumentationConfig.setSensorInstrumentationPoint(registeredSensorConfig);
		}

		return registeredSensorConfig;
	}

}
