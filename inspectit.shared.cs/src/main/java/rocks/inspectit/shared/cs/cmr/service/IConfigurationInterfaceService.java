package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceImportData;

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
	List<Profile> getAllProfiles();

	/**
	 * Returns the profile with the given id.
	 *
	 * @param id
	 *            Id of profile.
	 * @return {@link Profile}
	 * @throws BusinessException
	 *             If profile with given id does not exist.
	 */
	Profile getProfile(String id) throws BusinessException;

	/**
	 * Creates new profile.
	 *
	 * @param profile
	 *            Profile template.
	 * @return Returns created profile with correctly set id.
	 * @throws BusinessException
	 *             If saving fails.
	 */
	Profile createProfile(Profile profile) throws BusinessException;

	/**
	 * Imports the profile. Note that if profile with the same id already exists it will be
	 * overwritten.
	 *
	 * @param profile
	 *            Profile.
	 * @return Returns created/updated profile depending if the overwrite was executed.
	 * @throws BusinessException
	 *             If import fails.
	 */
	Profile importProfile(Profile profile) throws BusinessException;

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
	 * @throws BusinessException
	 *             If update of common profile is attempted. If profile does not exist or revision
	 *             check fails. If saving fails.
	 */
	Profile updateProfile(Profile profile) throws BusinessException;

	/**
	 * Deletes the existing profile.
	 *
	 * @param profile
	 *            Profile to delete.
	 * @throws BusinessException
	 *             If profile with given id does not exist.
	 */
	void deleteProfile(Profile profile) throws BusinessException;

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
	 * @throws BusinessException
	 *             If environment with given id does not exist.
	 */
	Environment getEnvironment(String id) throws BusinessException;

	/**
	 * Creates new environment.
	 *
	 * @param environment
	 *            Environment template.
	 * @return Returns created environment with correctly set id.
	 * @throws BusinessException
	 *             If saving fails.
	 */
	Environment createEnvironment(Environment environment) throws BusinessException;

	/**
	 * Imports the environment. Note that if environment with the same id already exists it will be
	 * overwritten.
	 *
	 * @param environment
	 *            Environment.
	 * @return Returns created/updated environment depending if the overwrite was executed.
	 * @throws BusinessException
	 *             If import fails.
	 */
	Environment importEnvironment(Environment environment) throws BusinessException;

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
	 * @throws BusinessException
	 *             If environment does not exist or revision check fails. If saving fails.
	 */
	Environment updateEnvironment(Environment environment) throws BusinessException;

	/**
	 * Deletes the existing environment.
	 *
	 * @param environment
	 *            Environment to delete.
	 * @throws BusinessException
	 *             If deletion fails.
	 */
	void deleteEnvironment(Environment environment) throws BusinessException;

	/**
	 * Returns the currently used agent mappings.
	 *
	 * @return Returns the currently used agent mappings.
	 */
	AgentMappings getAgentMappings();

	/**
	 * Saves the agent mappings to be used.
	 *
	 * @param agentMappings
	 *            {@link AgentMappings} to save
	 * @return updated {@link AgentMappings} instance
	 * @throws BusinessException
	 *             If the revision fails. If save fails.
	 */
	AgentMappings saveAgentMappings(AgentMappings agentMappings) throws BusinessException;

	/**
	 * Returns the bytes for the given import data consisted out of given environments and profiles.
	 * These bytes can be saved directly to export file.
	 *
	 * @param environments
	 *            Environments to export.
	 * @param profiles
	 *            Profiles to export.
	 * @return Byte array.
	 * @throws BusinessException
	 *             If operation fails.
	 */
	byte[] getExportData(Collection<Environment> environments, Collection<Profile> profiles) throws BusinessException;

	/**
	 * Returns the {@link ConfigurationInterfaceImportData} from the given import data bytes.
	 *
	 * @param importData
	 *            bytes that were exported.
	 * @return {@link ConfigurationInterfaceImportData}.
	 * @throws BusinessException
	 *             If operation fails.
	 */
	ConfigurationInterfaceImportData getImportData(byte[] importData) throws BusinessException;

}
