package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.SpecialMethodSensorAssignment;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.SpecialInstrumentationPoint;

/**
 * {@link AbstractInstrumentationApplier} for the special assignments.
 *
 * @author Ivan Senic
 *
 */
public class SpecialInstrumentationApplier extends AbstractInstrumentationApplier {

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
	 */
	public SpecialInstrumentationApplier(SpecialMethodSensorAssignment functionalAssignment, Environment environment) {
		super(environment);
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
		return getClassSensorAssignmentFilter().matches(functionalAssignment, classType);
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
	protected void applyAssignment(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		SpecialInstrumentationPoint functionalInstrumentation = new SpecialInstrumentationPoint(functionalAssignment.getInstrumentationType());
		methodInstrumentationConfig.addFunctionalInstrumentation(functionalInstrumentation);
	}

}
