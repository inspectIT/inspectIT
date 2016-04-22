package rocks.inspectit.ui.rcp.repository;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Extended {@link RepositoryChangeListener} only for events on the {@link CmrRepositoryDefinition}
 * s.
 *
 * @author Ivan Senic
 *
 */
public interface CmrRepositoryChangeListener {

	/**
	 * If the online status of the repository has been changed.
	 *
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param oldStatus
	 *            Old status.
	 * @param newStatus
	 *            New status.
	 */
	void repositoryOnlineStatusUpdated(CmrRepositoryDefinition repositoryDefinition, OnlineStatus oldStatus, OnlineStatus newStatus);

	/**
	 * If a repository has been added.
	 *
	 * @param cmrRepositoryDefinition
	 *            the repository definition.
	 */
	void repositoryAdded(CmrRepositoryDefinition cmrRepositoryDefinition);

	/**
	 * If a repository has been removed.
	 *
	 * @param cmrRepositoryDefinition
	 *            the repository definition.
	 */
	void repositoryRemoved(CmrRepositoryDefinition cmrRepositoryDefinition);

	/**
	 * Informs the listener that the repository data like name or description have been updated.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} that was updated.
	 */
	void repositoryDataUpdated(CmrRepositoryDefinition cmrRepositoryDefinition);

	/**
	 * Informs the listener that the provided agent on the repository has been deleted.
	 *
	 * @param cmrRepositoryDefinition
	 *            the repository definition.
	 * @param agent
	 *            Agent that was deleted.
	 */
	void repositoryAgentDeleted(CmrRepositoryDefinition cmrRepositoryDefinition, PlatformIdent agent);
}
