package info.novatec.inspectit.cmr.instrumentation.config.job;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCache;
import info.novatec.inspectit.cmr.instrumentation.config.AgentCacheEntry;
import info.novatec.inspectit.cmr.instrumentation.config.ClassCacheSearchNarrower;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationHolder;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.spring.logger.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * {@link ClassCacheSearchNarrower}.
	 */
	@Autowired
	private ClassCacheSearchNarrower classCacheSearchNarrower;

	/**
	 * Registration service needed for the {@link IInstrumentationApplier}s.
	 */
	@Autowired
	private IRegistrationService registrationService;

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
		final Collection<ImmutableClassType> changedClassTypes = new ArrayList<>();

		// process all class sensor assignments for removal
		for (final AbstractClassSensorAssignment<?> assignment : classSensorAssignments) {
			// narrow the search
			Collection<? extends ImmutableClassType> classTypes = classCacheSearchNarrower.narrowByClassSensorAssignment(getClassCache(), assignment);

			// get the applier
			IInstrumentationApplier instrumentationApplier = assignment.getInstrumentationApplier(getEnvironment(), registrationService);
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
	 *            Collection of removed {@link AbstractClassSensorAssignment}s.
	 * @return if processing inserted any new instrumentation points
	 */
	protected boolean processAddedAssignments(Collection<? extends AbstractClassSensorAssignment<?>> classSensorAssignments) {
		boolean added = false;

		// process all class sensor assignments for adding
		for (final AbstractClassSensorAssignment<?> assignment : classSensorAssignments) {
			// narrow the search
			Collection<? extends ImmutableClassType> classTypes = classCacheSearchNarrower.narrowByClassSensorAssignment(getClassCache(), assignment);

			// get the applier
			IInstrumentationApplier instrumentationApplier = assignment.getInstrumentationApplier(getEnvironment(), registrationService);

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
	protected AgentConfiguration getAgentConfiguration() {
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
