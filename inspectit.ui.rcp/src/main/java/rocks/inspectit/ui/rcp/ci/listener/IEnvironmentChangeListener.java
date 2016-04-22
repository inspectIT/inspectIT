package rocks.inspectit.ui.rcp.ci.listener;

import java.util.EventListener;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for the listeners on the {@link Environment} changes.
 *
 * @author Ivan Senic
 *
 */
public interface IEnvironmentChangeListener extends EventListener {

	/**
	 * Environment created.
	 *
	 * @param environment
	 *            {@link Environment}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Environment updated.
	 *
	 * @param environment
	 *            {@link Environment}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Environment deleted.
	 *
	 * @param environment
	 *            {@link Environment}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition);

}