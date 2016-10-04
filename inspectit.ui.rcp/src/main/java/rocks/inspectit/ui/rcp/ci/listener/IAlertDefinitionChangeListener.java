package rocks.inspectit.ui.rcp.ci.listener;

import java.util.EventListener;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for the listeners on the changes of {@link AlertingDefinition} instances.
 *
 * @author Alexander Wert
 *
 */
public interface IAlertDefinitionChangeListener extends EventListener {
	/**
	 * Notifies that a new {@link AlertingDefinition} has been created.
	 *
	 * @param alertDefinition
	 *            created {@link AlertingDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void alertDefinitionCreated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link AlertingDefinition} has been updated.
	 *
	 * @param alertDefinition
	 *            updated {@link AlertingDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void alertDefinitionUpdated(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Notifies that an {@link AlertingDefinition} has been updated.
	 *
	 * @param alertDefinition
	 *            deleted {@link AlertingDefinition}.
	 * @param repositoryDefinition
	 *            corresponding {@link CmrRepositoryDefinition}.
	 */
	void alertDefinitionDeleted(AlertingDefinition alertDefinition, CmrRepositoryDefinition repositoryDefinition);
}
