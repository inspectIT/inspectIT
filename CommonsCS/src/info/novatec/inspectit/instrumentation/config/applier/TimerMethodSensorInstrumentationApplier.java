package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.context.AbstractContextCapture;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;

import org.apache.commons.collections.CollectionUtils;

/**
 * Special {@link MethodSensorInstrumentationApplier} for the timer sensor.
 *
 * @author Ivan Senic
 *
 */
public class TimerMethodSensorInstrumentationApplier extends MethodSensorInstrumentationApplier {

	/**
	 * {@link TimerMethodSensorAssignment} that defines instrumentation configuration.
	 */
	private final TimerMethodSensorAssignment timerAssignment;

	/**
	 * Default constructor.
	 *
	 * @param timerAssignment
	 *            {@link TimerMethodSensorAssignment} that defines instrumentation configuration.
	 * @param environment
	 *            Environment belonging to the assignment.
	 * @param registrationService
	 *            Registration service needed for registration of the IDs.
	 */
	public TimerMethodSensorInstrumentationApplier(TimerMethodSensorAssignment timerAssignment, Environment environment, IRegistrationService registrationService) {
		super(timerAssignment, environment, registrationService);
		this.timerAssignment = timerAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyAssignment(AgentConfiguration agentConfiguration, RegisteredSensorConfig registeredSensorConfig) {
		// call super first
		super.applyAssignment(agentConfiguration, registeredSensorConfig);

		// check for invocation starts
		if (timerAssignment.isStartsInvocation()) {
			// find the id of invocation sensor and only mark if one is found
			IMethodSensorConfig invocationSensorConfig = environment.getMethodSensorTypeConfig(InvocationSequenceSensorConfig.class);
			if (null != invocationSensorConfig) {
				MethodSensorTypeConfig invocationSensorTypeConfig = agentConfiguration.getMethodSensorTypeConfig(invocationSensorConfig.getClassName());
				registeredSensorConfig.addSensorId(invocationSensorTypeConfig.getId(), invocationSensorTypeConfig.getPriority());
				registeredSensorConfig.setStartsInvocation(true);
			}
		}

		// deal with context captures
		if (CollectionUtils.isNotEmpty(timerAssignment.getContextCaptures())) {
			for (AbstractContextCapture contextCapture : timerAssignment.getContextCaptures()) {
				registeredSensorConfig.addPropertyAccessor(contextCapture.getPropertyPathStart());
			}
		}
	}
}
