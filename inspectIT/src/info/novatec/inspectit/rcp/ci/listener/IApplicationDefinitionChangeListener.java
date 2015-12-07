package info.novatec.inspectit.rcp.ci.listener;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessContextDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.EventListener;

/**
 * Interface for the listeners on the changes of {@link IApplicationDefinition} instances.
 * 
 * @author Alexander Wert
 *
 */
public interface IApplicationDefinitionChangeListener extends EventListener {

	/**
	 * Notifies that a new {@link IApplicationDefinition} has been created.
	 * 
	 * @param application
	 *            created {@link IApplicationDefinition}.
	 * @param positionIndex
	 *            the position index where the new {@link IApplicationDefinition} has been inserted
	 *            in the list of application definitions of the corresponding
	 *            {@link IBusinessContextDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationCreated(IApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link IApplicationDefinition} has been moved in the list of application
	 * definitions of the corresponding {@link IBusinessContextDefinition}.
	 * 
	 * @param application
	 *            moved {@link IApplicationDefinition}.
	 * @param oldPositionIndex
	 *            position index before moving.
	 * @param newPositionIndex
	 *            position index after moving.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationMoved(IApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link IApplicationDefinition} has been updated.
	 * 
	 * @param application
	 *            updated {@link IApplicationDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationUpdated(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link IApplicationDefinition} has been updated.
	 * 
	 * @param application
	 *            deleted {@link IApplicationDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationDeleted(IApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition);
}
