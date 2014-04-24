package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;

import java.util.Collection;


/**
 * Service for the CI.
 * 
 * @author Ivan Senic
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IConfigurationInterfaceService {

	/**
	 * Returns all existing profiles.
	 * 
	 * @return Returns all existing profiles.
	 */
	Collection<Profile> getAllProfiles();

	/**
	 * Returns the profile with the given id.
	 * 
	 * @param id
	 *            Id of profile.
	 * @return {@link Profile}
	 * @throws Exception
	 *             If profile with given id does not exist.
	 */
	Profile getProfile(String id) throws Exception;

	/**
	 * Creates new profile.
	 * 
	 * @param profile
	 *            Profile template.
	 * @return Returns created profile with correctly set id.
	 * @throws Exception
	 *             If saving fails.
	 */
	Profile createProfile(Profile profile) throws Exception;

	/**
	 * Updates the given profile and saves it to the disk. Update will fail with an Exception if:
	 * <ul>
	 * <li>Attempt is made to update default profile.
	 * <li>Profile does not exists on the CMR.
	 * <li>Profile revision sequence does not match the current sequence.
	 * </ul>
	 * 
	 * @param profile
	 *            Profile to update.
	 * @return updated profile instance
	 * @throws Exception
	 *             If saving fails.
	 */
	Profile updateProfile(Profile profile) throws Exception;

	/**
	 * Deletes the existing profile.
	 * 
	 * @param profile
	 *            Profile to delete.
	 * @throws Exception
	 *             If profile with given id does not exist.
	 */
	void deleteProfile(Profile profile) throws Exception;

	/**
	 * Returns all existing environment.
	 * 
	 * @return Returns all existing environment.
	 */
	Collection<Environment> getAllEnvironments();

	/**
	 * Returns the environment with the given id.
	 * 
	 * @param id
	 *            Id of environment.
	 * @return {@link Environment}
	 * @throws Exception
	 *             If environment with given id does not exist.
	 */
	Environment getEnvironment(String id) throws Exception;

	/**
	 * Creates new environment.
	 * 
	 * @param environment
	 *            Environment template.
	 * @return Returns created environment with correctly set id.
	 * @throws Exception
	 *             If saving fails.
	 */
	Environment createEnvironment(Environment environment) throws Exception;
	
	/**
	 * Updates the given environment and saves it to the disk. Update will fail with an Exception
	 * if:
	 * <ul>
	 * <li>Environment does not exists on the CMR.
	 * <li>Environment revision sequence does not match the current sequence.
	 * </ul>
	 * 
	 * @param environment
	 *            Environment to update.
	 * @return updated environment instance
	 * @throws Exception
	 *             If saving fails.
	 */
	Environment updateEnvironment(Environment environment) throws Exception;

	/**
	 * Deletes the existing environment.
	 * 
	 * @param environment
	 *            Environment to delete.
	 * @throws Exception
	 *             If deletion fails.
	 */
	void deleteEnvironment(Environment environment) throws Exception;

	/**
	 * Returns the currently used agent mappings.
	 * 
	 * @return Returns the currently used agent mappings.
	 */
	AgentMappings getAgentMappings();

	/**
	 * Sets the agent mappings to be used.
	 * 
	 * @param agentMappings
	 *            Collection of {@link AgentMapping}s
	 * @return updated {@link AgentMappings} instance
	 * @throws Exception
	 *             If save fails.
	 */
	AgentMappings setAgentMappings(AgentMappings agentMappings) throws Exception;

}
