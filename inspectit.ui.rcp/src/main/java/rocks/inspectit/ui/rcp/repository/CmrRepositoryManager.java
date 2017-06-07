package rocks.inspectit.ui.rcp.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.ui.rcp.job.UpdateRepositoryJob;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.util.ListenerList;

/**
 * The repository manager only for {@link CmrRepositoryDefinition}s.
 *
 * @author Ivan Senic
 *
 */
public class CmrRepositoryManager {

	/**
	 * /** Update online repository status job repetition time in milliseconds.
	 */
	public static final long UPDATE_JOB_REPETITION = 60000;

	/**
	 * The list containing the available {@link RepositoryDefinition} objects.
	 */
	private List<CmrRepositoryDefinition> cmrRepositoryDefinitions = new ArrayList<>();

	/**
	 * The list of listeners to be notified.
	 */
	private ListenerList<CmrRepositoryChangeListener> cmrRepositoryChangeListeners = new ListenerList<>();

	/**
	 * Map of jobs.
	 */
	private Map<CmrRepositoryDefinition, UpdateRepositoryJob> repositoryUpdateJobMap = new ConcurrentHashMap<>();

	/**
	 * Default constructor.
	 * <p>
	 * Loads the repository definitions from the preference store.
	 */
	public CmrRepositoryManager() {
		List<CmrRepositoryDefinition> savedCmrs = PreferencesUtils.getCmrRepositoryDefinitions();
		if (CollectionUtils.isNotEmpty(savedCmrs)) {
			cmrRepositoryDefinitions.addAll(savedCmrs);
			for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryDefinitions) {
				for (CmrRepositoryChangeListener repositoryChangeListener : cmrRepositoryChangeListeners) {
					cmrRepositoryDefinition.addCmrRepositoryChangeListener(repositoryChangeListener);
				}
				UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob(cmrRepositoryDefinition, true);
				updateRepositoryJob.schedule();
				repositoryUpdateJobMap.put(cmrRepositoryDefinition, updateRepositoryJob);
			}
		}
	}

	/**
	 * Adds a repository definition handled by this manager.
	 *
	 * @param cmrRepositoryDefinition
	 *            The definition to add.
	 */
	public void addCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (!cmrRepositoryDefinitions.contains(cmrRepositoryDefinition)) {
			for (CmrRepositoryChangeListener repositoryChangeListener : cmrRepositoryChangeListeners) {
				cmrRepositoryDefinition.addCmrRepositoryChangeListener(repositoryChangeListener);
			}
			cmrRepositoryDefinitions.add(cmrRepositoryDefinition);

			savePreference();

			for (CmrRepositoryChangeListener repositoryChangeListener : cmrRepositoryChangeListeners) {
				repositoryChangeListener.repositoryAdded(cmrRepositoryDefinition);
			}

			UpdateRepositoryJob updateRepositoryJob = new UpdateRepositoryJob(cmrRepositoryDefinition, true);
			updateRepositoryJob.schedule();
			repositoryUpdateJobMap.put(cmrRepositoryDefinition, updateRepositoryJob);
		}
	}

	/**
	 * Removes a repository definition and notifies all registered listeners.
	 *
	 * @param cmrRepositoryDefinition
	 *            The definition to remove.
	 */
	public void removeCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		for (CmrRepositoryChangeListener repositoryChangeListener : cmrRepositoryChangeListeners) {
			cmrRepositoryDefinition.removeCmrRepositoryChangeListener(repositoryChangeListener);
		}
		cmrRepositoryDefinitions.remove(cmrRepositoryDefinition);

		savePreference();

		for (CmrRepositoryChangeListener repositoryChangeListener : cmrRepositoryChangeListeners) {
			repositoryChangeListener.repositoryRemoved(cmrRepositoryDefinition);
		}

		UpdateRepositoryJob updateRepositoryJob = repositoryUpdateJobMap.remove(cmrRepositoryDefinition);
		if (null != updateRepositoryJob) {
			updateRepositoryJob.cancel();
		}
	}

	/**
	 * Forces the CMR Online update check. If the {@link CmrRepositoryDefinition} to check is not on
	 * the current list of repositories, this method will create a small job to check online status,
	 * but this job won't be rescheduled.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return Returns the job that will be performing the update. Caller can use this job to react
	 *         on the job being done.
	 */
	public UpdateRepositoryJob forceCmrRepositoryOnlineStatusUpdate(final CmrRepositoryDefinition cmrRepositoryDefinition) {
		UpdateRepositoryJob updateRepositoryJob = repositoryUpdateJobMap.get(cmrRepositoryDefinition);
		if (null != updateRepositoryJob) {
			if (updateRepositoryJob.cancel()) {
				updateRepositoryJob.schedule();
			}
		}
		return updateRepositoryJob;
	}

	/**
	 * Returns all registered repository definitions handled by this manager. The list is
	 * unmodifiable.
	 *
	 * @return The list of repository definitions.
	 */
	public List<CmrRepositoryDefinition> getCmrRepositoryDefinitions() {
		return Collections.unmodifiableList(cmrRepositoryDefinitions);
	}

	/**
	 * Adds a listener which notifies on certain events.
	 *
	 * @param repositoryChangeListener
	 *            The listener to add.
	 */
	public void addCmrRepositoryChangeListener(CmrRepositoryChangeListener repositoryChangeListener) {
		cmrRepositoryChangeListeners.add(repositoryChangeListener);

		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryDefinitions) {
			cmrRepositoryDefinition.addCmrRepositoryChangeListener(repositoryChangeListener);
		}
	}

	/**
	 * Removes the listener.
	 *
	 * @param repositoryChangeListener
	 *            The listener to remove.
	 */
	public void removeCmrRepositoryChangeListener(CmrRepositoryChangeListener repositoryChangeListener) {
		cmrRepositoryChangeListeners.remove(repositoryChangeListener);

		for (CmrRepositoryDefinition cmrRepositoryDefinition : cmrRepositoryDefinitions) {
			cmrRepositoryDefinition.removeCmrRepositoryChangeListener(repositoryChangeListener);
		}
	}

	/**
	 * Cancels all the update repository jobs. The method will return only when all jobs are
	 * canceled.
	 */
	public void cancelAllUpdateRepositoriesJobs() {
		for (UpdateRepositoryJob updateRepositoryJob : repositoryUpdateJobMap.values()) {
			while (!updateRepositoryJob.cancel()) {
				try {
					updateRepositoryJob.join();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	/**
	 * Updates the {@link CmrRepositoryDefinition} entry in the preferences.
	 *
	 * @param cmrRepositoryDefinition
	 *            Repository to update.
	 */
	public void updateCmrRepositoryDefinitionData(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.savePreference();
		for (CmrRepositoryChangeListener listener : cmrRepositoryChangeListeners) {
			listener.repositoryDataUpdated(cmrRepositoryDefinition);
		}
	}

	/**
	 * Informs all listener that the provided agent on the repository has been deleted.
	 *
	 * @param cmrRepositoryDefinition
	 *            the repository definition.
	 * @param agent
	 *            Agent that was deleted.
	 */
	public void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent) {
		for (CmrRepositoryChangeListener listener : cmrRepositoryChangeListeners) {
			listener.repositoryAgentDeleted(cmrRepositoryDefinition, agent);
		}
	}

	/**
	 * Save the preferences to the backend store.
	 */
	private void savePreference() {
		List<CmrRepositoryDefinition> toSave = new ArrayList<>();
		for (CmrRepositoryDefinition repositoryDefinition : cmrRepositoryDefinitions) {
			toSave.add(repositoryDefinition);
		}
		PreferencesUtils.saveCmrRepositoryDefinitions(toSave, false);
	}
}
