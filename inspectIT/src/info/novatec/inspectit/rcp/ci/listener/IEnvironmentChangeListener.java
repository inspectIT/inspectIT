package info.novatec.inspectit.rcp.ci.listener;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.EventListener;

/**
 * Interface for the listeners on the {@link Environment} changes.
 * 
 * @author Ivan Senic
 * 
 */
public interface IEnvironmentChangeListener extends EventListener {

	/**
	 * Environment added.
	 * 
	 * @param environment
	 *            {@link Environment}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 */
	void environmentAdded(Environment environment, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Environment edited.
	 * 
	 * @param environment
	 *            {@link Environment}
	 */
	void environmentEdited(Environment environment);

	/**
	 * Environment deleted.
	 * 
	 * @param environment
	 *            {@link Environment}
	 */
	void environmentDeleted(Environment environment);

}