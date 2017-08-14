package rocks.inspectit.server.instrumentation.config.applier;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.context.AbstractContextCapture;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Special {@link MethodSensorInstrumentationApplier} for the timer sensor.
 *
 * @author Ivan Senic
 *
 */
public class TimerMethodSensorInstrumentationApplier extends InvocationStartMethodSensorInstrumentationApplier {

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
	protected void applyAssignment(AgentConfig agentConfiguration, SensorInstrumentationPoint registeredSensorConfig) {
		// call super first
		super.applyAssignment(agentConfiguration, registeredSensorConfig);

		// deal with context captures
		if (CollectionUtils.isNotEmpty(timerAssignment.getContextCaptures())) {
			for (AbstractContextCapture contextCapture : timerAssignment.getContextCaptures()) {
				registeredSensorConfig.addPropertyAccessor(contextCapture.getPropertyPathStart());
			}
		}
	}
}
