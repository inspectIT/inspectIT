package rocks.inspectit.ui.rcp.ci.listener;

import java.util.EventListener;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for the listeners on the changes of {@link ApplicationDefinition} instances.
 *
 * @author Alexander Wert
 *
 */
public interface IApplicationDefinitionChangeListener extends EventListener {

	/**
	 * Notifies that a new {@link ApplicationDefinition} has been created.
	 *
	 * @param application
	 *            created {@link ApplicationDefinition}.
	 * @param positionIndex
	 *            the position index where the new {@link ApplicationDefinition} has been inserted
	 *            in the list of application definitions of the corresponding
	 *            {@link IBusinessContextDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationCreated(ApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link ApplicationDefinition} has been moved in the list of application
	 * definitions of the corresponding {@link IBusinessContextDefinition}.
	 *
	 * @param application
	 *            moved {@link ApplicationDefinition}.
	 * @param oldPositionIndex
	 *            position index before moving.
	 * @param newPositionIndex
	 *            position index after moving.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationMoved(ApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link ApplicationDefinition} has been updated.
	 *
	 * @param application
	 *            updated {@link ApplicationDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationUpdated(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link ApplicationDefinition} has been updated.
	 *
	 * @param application
	 *            deleted {@link ApplicationDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void applicationDeleted(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition);
}
