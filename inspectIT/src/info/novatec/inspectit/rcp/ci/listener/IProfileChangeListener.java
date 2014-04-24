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
	 * Profile is added.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param repositoryDefinition TODO
	 */
	void profileAdded(Profile profile, CmrRepositoryDefinition repositoryDefinition);

	/**
	 * Profile is added.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @param onlyProperties
	 *            If only properties like name, description, etc are edited. <code>true</code> if no
	 *            change on the profile assignments are done.
	 */
	void profileEdited(Profile profile, boolean onlyProperties);

	/**
	 * Profile is added.
	 * 
	 * @param profile
	 *            {@link Profile}
	 */
	void profileDeleted(Profile profile);

}