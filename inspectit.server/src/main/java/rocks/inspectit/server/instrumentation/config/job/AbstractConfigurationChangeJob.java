package rocks.inspectit.server.instrumentation.config.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ClassCacheSearchNarrower;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * Abstract class for all configuration change jobs. This class knows how to add or remove
 * instrumentation points on the given class cache, environment and agent configuration. Note that
 * {@link #environment}, {@link #classCache} and {@link #agentConfiguration} must be set using
 * setters before running the {@link #run()} method.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractConfigurationChangeJob implements Runnable {

	/**
	 * Log for this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ClassCacheSearchNarrower} for help in searching for class types.
	 */
	@Autowired
	private ClassCacheSearchNarrower classCacheSearchNarrower;

	/**
	 * ConfigurationResolver needed for resolving the {@link IInstrumentationApplier}s.
	 */
	@Autowired
	private ConfigurationResolver configurationResolver;

	/**
	 * {@link AgentCacheEntry} containing all necessary information.
	 */
	private AgentCacheEntry agentCacheEntry;

	/**
	 * Process the removed assignments. All instrumentation points affected by the any of these
	 * assignments are first completely removed. All classes that have any point removed will be
	 * re-analyzed against complete configuration in order to reset the possible points coming not
	 * from removed assignments.
	 *
	 * @param classSensorAssignments
	 *            Collection of removed {@link AbstractClassSensorAssignment}s.
	 * @return if processing removed any instrumentation points
	 */
	protected boolean processRemovedAssignments(Collection<? extends AbstractClassSensorAssignment<?>> classSensorAssignments) {
		Collection<ImmutableClassType> changedClassTypes = new ArrayList<>();

		// process all class sensor assignments for removal
		for (AbstractClassSensorAssignment<?> assignment : classSensorAssignments) {
			// narrow the search
			Collection<? extends ImmutableClassType> classTypes = classCacheSearchNarrower.narrowByClassSensorAssignment(getClassCache(), assignment);

			// get the applier
			IInstrumentationApplier instrumentationApplier = configurationResolver.getInstrumentationApplier(assignment, getEnvironment());
			changedClassTypes.addAll(getClassCache().getInstrumentationService().removeInstrumentationPoints(classTypes, Collections.singleton(instrumentationApplier)));
		}

		// if no class was affected just return
		if (CollectionUtils.isNotEmpty(changedClassTypes)) {
			// if any class was affected re-check those classes against complete configuration
			// because we removed all instrumentation points
			Collection<IInstrumentationApplier> instrumentationAppliers = getConfigurationHolder().getInstrumentationAppliers();
			getClassCache().getInstrumentationService().addInstrumentationPoints(changedClassTypes, getAgentConfiguration(), instrumentationAppliers);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Process the added assignments. New instrumentation points will be added to all the classes in
	 * the class cache that fit to the given assignments.
	 *
	 * @param classSensorAssignments
	 *            Collection of added {@link AbstractClassSensorAssignment}s.
	 * @return if processing inserted any new instrumentation points
	 */
	protected boolean processAddedAssignments(Collection<? extends AbstractClassSensorAssignment<?>> classSensorAssignments) {
		boolean added = false;

		// process all class sensor assignments for adding
		for (AbstractClassSensorAssignment<?> assignment : classSensorAssignments) {
			// narrow the search
			Collection<? extends ImmutableClassType> classTypes = classCacheSearchNarrower.narrowByClassSensorAssignment(getClassCache(), assignment);

			// get the applier
			IInstrumentationApplier instrumentationApplier = configurationResolver.getInstrumentationApplier(assignment, getEnvironment());

			// execute
			Collection<? extends ImmutableClassType> instrumentedClassTypes = getClassCache().getInstrumentationService().addInstrumentationPoints(classTypes, getAgentConfiguration(),
					Collections.singleton(instrumentationApplier));
			added |= CollectionUtils.isNotEmpty(instrumentedClassTypes);
		}

		return added;
	}

	/**
	 * @return Returns agent id based on the {@link AgentCacheEntry}.
	 */
	protected long getAgentId() {
		return agentCacheEntry.getId();
	}

	/**
	 * @return Returns class cache based on the {@link AgentCacheEntry}.
	 */
	protected ClassCache getClassCache() {
		return agentCacheEntry.getClassCache();
	}

	/**
	 * @return Returns configuration holder based on the {@link AgentCacheEntry}.
	 */
	protected ConfigurationHolder getConfigurationHolder() {
		return agentCacheEntry.getConfigurationHolder();
	}

	/**
	 * @return Returns environment based on the {@link ConfigurationHolder}.
	 */
	protected Environment getEnvironment() {
		return getConfigurationHolder().getEnvironment();
	}

	/**
	 * @return Returns agent configuration based on the {@link ConfigurationHolder}.
	 */
	protected AgentConfig getAgentConfiguration() {
		return getConfigurationHolder().getAgentConfiguration();
	}

	/**
	 * Sets {@link #agentCacheEntry}.
	 *
	 * @param agentCacheEntry
	 *            New value for {@link #agentCacheEntry}
	 */
	public void setAgentCacheEntry(AgentCacheEntry agentCacheEntry) {
		this.agentCacheEntry = agentCacheEntry;
	}

}
