package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.FunctionalInstrumentationPoint;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

/**
 * {@link AbstractInstrumentationApplier} for the functional assignments.
 *
 * @author Ivan Senic
 *
 */
public class FunctionalInstrumentationApplier extends AbstractInstrumentationApplier {

	/**
	 * {@link FunctionalMethodSensorAssignment} to use.
	 */
	private final FunctionalMethodSensorAssignment functionalAssignment;

	/**
	 * Default constructor.
	 *
	 * @param functionalAssignment
	 *            {@link FunctionalMethodSensorAssignment} to use.
	 * @param environment
	 *            Environment belonging to the assignment.
	 */
	public FunctionalInstrumentationApplier(FunctionalMethodSensorAssignment functionalAssignment, Environment environment) {
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
		FunctionalInstrumentationPoint functionalInstrumentation = new FunctionalInstrumentationPoint(functionalAssignment.getInstrumentationType());
		methodInstrumentationConfig.addFunctionalInstrumentation(functionalInstrumentation);
	}

}
