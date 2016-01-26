package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;

/**
 * Abstract classes for instrumentation that work with our {@link RegisteredSensorConfig}.
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
	 * Applies the assignment of this applier to the {@link RegisteredSensorConfig}.
	 *
	 * @param agentConfiguration
	 *            Agent configuration being used.
	 * @param registeredSensorConfig
	 *            {@link RegisteredSensorConfig}.
	 */
	protected abstract void applyAssignment(AgentConfiguration agentConfiguration, RegisteredSensorConfig registeredSensorConfig);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		RegisteredSensorConfig registeredSensorConfig = getOrCreateRegisteredSensorConfig(agentConfiguration, methodType, methodInstrumentationConfig);
		// first check for the enhanced exception
		checkForEnhancedExceptionSensor(registeredSensorConfig, agentConfiguration);
		// then apply additional based on assignment
		applyAssignment(agentConfiguration, registeredSensorConfig);
	}

	/**
	 * Checks for the enhanced exception sensor in environment and sets the proper value in the
	 * {@link RegisteredSensorConfig}.
	 *
	 * @param registeredSensorConfig
	 *            {@link RegisteredSensorConfig}
	 * @param agentConfiguration
	 *            used configuration
	 */
	private void checkForEnhancedExceptionSensor(RegisteredSensorConfig registeredSensorConfig, AgentConfiguration agentConfiguration) {
		ExceptionSensorTypeConfig exceptionSensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
		registeredSensorConfig.setEnhancedExceptionSensor(null != exceptionSensorTypeConfig && exceptionSensorTypeConfig.isEnhanced());
	}

	/**
	 * Checks if the {@link RegisteredSensorConfig} exists in the {@link MethodType}. If not new one
	 * is created, registered with registration service and saved in the {@link MethodType}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfiguration} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return {@link RegisteredSensorConfig} for the {@link MethodType}.
	 */
	private RegisteredSensorConfig getOrCreateRegisteredSensorConfig(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// check for existing
		RegisteredSensorConfig registeredSensorConfig = methodInstrumentationConfig.getRegisteredSensorConfig();

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

			registeredSensorConfig = new RegisteredSensorConfig(methodInstrumentationConfig);
			registeredSensorConfig.setId(id);

			// set to method instrumentation
			methodInstrumentationConfig.setRegisteredSensorConfig(registeredSensorConfig);
		}

		return registeredSensorConfig;
	}

}
