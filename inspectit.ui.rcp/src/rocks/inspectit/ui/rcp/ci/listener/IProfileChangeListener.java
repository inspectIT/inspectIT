package info.novatec.inspectit.rcp.ci.listener;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.EventListener;

/**
 * Interface for the listeners on the {@link Profile} changes.
 * 
 * @author Ivan Senic
 * 
 */
public interface IProfileChangeListener extends EventListener {

	/**
	 * Profile is created.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Profile is updated.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 * @param onlyProperties
	 *            If only properties like name, description, etc are edited. <code>true</code> if no
	 *            change on the profile assignments are done.
	 */
	void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties);

	/**
	 * Profile is deleted.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param repositoryDefinition
	 *            {@link CmrRepositoryDefinition} on which action occurred
	 */
	void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition);

}