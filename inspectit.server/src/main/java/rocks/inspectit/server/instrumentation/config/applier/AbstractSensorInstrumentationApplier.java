package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
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
	 * Checks if the {@link SensorInstrumentationPoint} exists in the
	 * {@link MethodInstrumentationConfig}. If not new one is created, registered with registration
	 * service and saved in the {@link MethodInstrumentationConfig}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return {@link SensorInstrumentationPoint} for the {@link MethodInstrumentationConfig}.
	 */
	protected SensorInstrumentationPoint getOrCreateSensorInstrumentationPoint(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// check for existing
		SensorInstrumentationPoint sensorInstrumentationPoint = methodInstrumentationConfig.getSensorInstrumentationPoint();

		// if not create new one
		if (null == sensorInstrumentationPoint) {
			// if not create new and register
			long id = registerMethod(agentConfiguration, methodType, methodInstrumentationConfig);
			sensorInstrumentationPoint = new SensorInstrumentationPoint();
			sensorInstrumentationPoint.setId(id);
			if (Character.CONSTRUCTOR.equals(methodType.getMethodCharacter())) {
				sensorInstrumentationPoint.setConstructor(true);
			}

			// set to method instrumentation
			methodInstrumentationConfig.setSensorInstrumentationPoint(sensorInstrumentationPoint);
		}

		return sensorInstrumentationPoint;
	}

	/**
	 * Checks if the {@link SensorInstrumentationPoint} exists in the
	 * {@link MethodInstrumentationConfig}. If not new one is created, registered with registration
	 * service and saved in the {@link MethodInstrumentationConfig}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return {@link SensorInstrumentationPoint} for the {@link MethodInstrumentationConfig}.
	 */
	protected SpecialInstrumentationPoint getOrCreateSpecialInstrumentationPoint(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// check for existing
		SpecialInstrumentationPoint specialInstrumentationPoint = methodInstrumentationConfig.getSpecialInstrumentationPoint();
		// if not create new one
		if (null == specialInstrumentationPoint) {
			// if not create new and register
			long id = registerMethod(agentConfiguration, methodType, methodInstrumentationConfig);
			specialInstrumentationPoint = new SpecialInstrumentationPoint();
			specialInstrumentationPoint.setId(id);

			// set to method instrumentation
			methodInstrumentationConfig.setSpecialInstrumentationPoint(specialInstrumentationPoint);
		}

		return specialInstrumentationPoint;
	}

	/**
	 * Registers method to the registration service.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfig} to read platform id.
	 * @param methodType
	 *            {@link MethodType} in question.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 * @return Id of the method.
	 */
	private long registerMethod(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		// extract package and class name
		String fqn = methodInstrumentationConfig.getTargetClassFqn();
		int index = fqn.lastIndexOf('.');
		String packageName = index >= 0 ? fqn.substring(0, index) : "";
		String className = fqn.substring(index + 1);

		return registrationService.registerMethodIdent(agentConfiguration.getPlatformId(), packageName, className, methodType.getName(), methodType.getParameters(), methodType.getReturnType(),
				methodType.getModifiers());
	}

}
