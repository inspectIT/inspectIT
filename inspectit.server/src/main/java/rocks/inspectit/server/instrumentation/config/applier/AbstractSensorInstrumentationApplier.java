package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

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
	protected abstract void applyAssignment(AgentConfig agentConfiguration, SensorInstrumentationPoint registeredSensorConfig);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		SensorInstrumentationPoint registeredSensorConfig = getOrCreateRegisteredSensorConfig(agentConfiguration, methodType, methodInstrumentationConfig);
		// then apply additional based on assignment
		applyAssignment(agentConfiguration, registeredSensorConfig);
	}

	/**
	 * Checks if the {@link SensorInstrumentationPoint} exists in the {@link MethodType}. If not new one
	 * is created, registered with registration service and saved in the {@link MethodType}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return {@link SensorInstrumentationPoint} for the {@link MethodType}.
	 */
	private SensorInstrumentationPoint getOrCreateRegisteredSensorConfig(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// check for existing
		SensorInstrumentationPoint registeredSensorConfig = methodInstrumentationConfig.getSensorInstrumentationPoint();

		// if not create new one
		if (null == registeredSensorConfig) {
			// if not create new and register

			// extract package and class name
			String fqn = methodInstrumentationConfig.getTargetClassFqn();
			int index = fqn.lastIndexOf('.');
			String packageName = index >= 0 ? fqn.substring(0, index) : "";
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
