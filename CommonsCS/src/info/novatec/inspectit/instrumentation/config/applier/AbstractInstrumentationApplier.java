package info.novatec.inspectit.instrumentation.config.applier;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.config.filter.AssignmentFilterProvider;
import info.novatec.inspectit.instrumentation.config.filter.ClassSensorAssignmentFilter;
import info.novatec.inspectit.instrumentation.config.filter.MethodSensorAssignmentFilter;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

/**
 * Base class for all instrumentation appliers.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractInstrumentationApplier implements IInstrumentationApplier {

	/**
	 * Assignment filter provider for providing class and method matching filters.
	 */
	protected AssignmentFilterProvider assignmentFilterProvider = new AssignmentFilterProvider();

	/**
	 * Environment belonging to the assignment.
	 */
	protected Environment environment;

	/**
	 * Default constructor.
	 *
	 * @param environment
	 *            Environment belonging to the assignment.
	 */
	public AbstractInstrumentationApplier(Environment environment) {
		if (null == environment) {
			throw new IllegalArgumentException("Environment can not be null in instrumentation applier.");
		}
		this.environment = environment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addInstrumentationPoints(AgentConfiguration agentConfiguration, ClassType classType) {
		boolean added = false;
		if (matches(classType)) {
			for (MethodType methodType : classType.getMethods()) {
				if (matches(methodType)) {
					MethodInstrumentationConfig methodInstrumentationConfig = getOrCreateMethodInstrumentationConfig(methodType);
					applyAssignment(agentConfiguration, methodType, methodInstrumentationConfig);
					added = true;
				}
			}
		}
		return added;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeInstrumentationPoints(ClassType classType) {
		if (!classType.hasInstrumentationPoints()) {
			return false;
		}

		boolean removed = false;
		if (matches(classType)) {
			for (MethodType methodType : classType.getMethods()) {
				if (matches(methodType)) {
					methodType.setMethodInstrumentationConfig(null);
					removed = true;
				}
			}
		}
		return removed;
	}

	/**
	 * If the {@link ClassType} matches the assignment in the applier.
	 *
	 * @param classType
	 *            ClassType to check.
	 * @return <code>true</code> if matches, <code>false</code> otherwise
	 */
	protected abstract boolean matches(ClassType classType);

	/**
	 * If the {@link MethodType} matches the assignment in the applier.
	 *
	 * @param methodType
	 *            MethoType to check.
	 * @return <code>true</code> if matches, <code>false</code> otherwise
	 */
	protected abstract boolean matches(MethodType methodType);

	/**
	 * Applies the assignment of this applier to the {@link MethodInstrumentationConfig}.
	 *
	 * @param agentConfiguration
	 *            Agent configuration being used.
	 * @param methodType
	 *            MethodType being instrumented.
	 * @param methodInstrumentationConfig
	 *            {@link MethodInstrumentationConfig}.
	 */
	protected abstract void applyAssignment(AgentConfiguration agentConfiguration, MethodType methodType, MethodInstrumentationConfig methodInstrumentationConfig);

	/**
	 * Gets {@link AssignmentFilterProvider#getClassSensorAssignmentFilter()}.
	 *
	 * @return {@link AssignmentFilterProvider#getClassSensorAssignmentFilter()}
	 */
	protected ClassSensorAssignmentFilter getClassSensorAssignmentFilter() {
		return assignmentFilterProvider.getClassSensorAssignmentFilter();
	}

	/**
	 * Gets {@link AssignmentFilterProvider#getMethodsSensorAssignmentFilter()}.
	 *
	 * @return {@link AssignmentFilterProvider#getMethodSensorAssignmentFilter()}
	 */
	protected MethodSensorAssignmentFilter getMethodSensorAssignmentFilter() {
		return assignmentFilterProvider.getMethodSensorAssignmentFilter();
	}

	/**
	 * Creates method instrumentation configuration for the given {@link MethodType} or returns
	 * existing one.
	 *
	 * @param methodType
	 *            {@link MethodType} to get {@link MethodInstrumentationConfig} for.
	 * @return Existing or new {@link MethodInstrumentationConfig}.
	 */
	private MethodInstrumentationConfig getOrCreateMethodInstrumentationConfig(MethodType methodType) {
		// check for existing
		MethodInstrumentationConfig methodInstrumentationConfig = methodType.getMethodInstrumentationConfig();
		// if not create new one
		if (null == methodInstrumentationConfig) {
			methodInstrumentationConfig = new MethodInstrumentationConfig(methodType);
			methodType.setMethodInstrumentationConfig(methodInstrumentationConfig);
		}

		return methodInstrumentationConfig;
	}

}
