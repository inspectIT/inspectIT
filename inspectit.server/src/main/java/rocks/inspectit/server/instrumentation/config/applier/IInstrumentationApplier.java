package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

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
	 *            {@link AgentConfig} used.
	 * @param classType
	 *            {@link ClassType} to process.
	 * @return <code>true</code> if at least one instrumentation point was added, false otherwise.
	 */
	boolean addInstrumentationPoints(AgentConfig agentConfiguration, ClassType classType);

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
