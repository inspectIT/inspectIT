package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;

/**
 * Instrumentation applier can add or remove instrumentation points to the method types.
 *
 * @author Ivan Senic
 *
 */
public interface IInstrumentationApplier {

	/**
	 * Returns assignment that is correlated with this applier. Can be <code>null</code> if applier
	 * is not having a specific sensor assignment to bounded to it.
	 *
	 * @return Returns assignment that is correlated with this applier.
	 */
	AbstractClassSensorAssignment<?> getSensorAssignment();

	/**
	 * Adds all the instrumentation points to the {@link ClassType} if the applier's assignment is
	 * matching class and any method in the given {@link ClassType}.
	 *
	 * @param agentConfiguration
	 *            {@link AgentConfiguration} used.
	 * @param classType
	 *            {@link ClassType} to process.
	 * @return <code>true</code> if at least one instrumentation point was added, false otherwise.
	 */
	boolean addInstrumentationPoints(AgentConfiguration agentConfiguration, ClassType classType);

	/**
	 * Removes all instrumentation points that might be created as result of the applier's
	 * assignment from a {@link ClassType}.
	 *
	 * @param classType
	 *            {@link ClassType} to process.
	 * @return <code>true</code> if at least one instrumentation point was removed, false otherwise.
	 */
	boolean removeInstrumentationPoints(ClassType classType);
}
