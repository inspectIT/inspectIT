package rocks.inspectit.server.instrumentation.config.applier;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * Special {@link IInstrumentationApplier} that can remove instrumentation points from any class
 * type passed to {@link #removeInstrumentationPoints(ClassType)}.
 * <p>
 * This applier does not know how to add instrumentation points.
 *
 * @author Ivan Senic
 *
 */
public class RemoveAllInstrumentationApplier implements IInstrumentationApplier {

	/**
	 * Static public instance for usage.
	 */
	private static final RemoveAllInstrumentationApplier INSTANCE = new RemoveAllInstrumentationApplier();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns <code>null</code> because there is no assignment bounded to this type of
	 * instrumentation applier.
	 */
	@Override
	public AbstractClassSensorAssignment<?> getSensorAssignment() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Always returns <code>false</code> when invoked as it can not add points.
	 */
	@Override
	public boolean addInstrumentationPoints(AgentConfig agentConfiguration, ClassType classType) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Matches any class type.
	 */
	@Override
	public boolean removeInstrumentationPoints(ClassType classType) {
		if (classType.hasInstrumentationPoints()) {
			return false;
		}

		for (MethodType methodType : classType.getMethods()) {
			methodType.setMethodInstrumentationConfig(null); // NOPMD
		}

		return true;
	}

	/**
	 * Gets {@link #INSTANCE}.
	 *
	 * @return {@link #INSTANCE}
	 */
	public static RemoveAllInstrumentationApplier getInstance() {
		return INSTANCE;
	}

}
