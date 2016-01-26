package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;

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
		return getClassSensorAssignmentFilter().matches(functionalAssignment, classType, false);
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
	protected void applyAssignment(AgentConfig agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig) {
		SpecialInstrumentationPoint functionalInstrumentation = new SpecialInstrumentationPoint(functionalAssignment.getInstrumentationType());
		methodInstrumentationConfig.addFunctionalInstrumentation(functionalInstrumentation);
	}

}
