package info.novatec.inspectit.cmr.ci;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.cmr.jaxb.JAXBTransformator;
import info.novatec.inspectit.spring.logger.Log;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * Manages all configuration interface operations.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ConfigurationInterfaceManager {

	/**
	 * Folder where all CI related stuff is saved.
	 */
	private static final String DEFAULT_CI_FOLDER = "ci";

	/**
	 * Sub-folder for schema.
	 */
	private static final String SCHEMA_FOLDER = "schema";

	/**
	 * File name for the schema.
	 */
	private static final String SCHEMA_FILE = "ciSchema.xsd";

	/**
	 * Sub-folder for saving profiles.
	 */
	private static final String PROFILES_FOLDER = "profiles";

	/**
	 * Sub-folder for saving environments.
	 */
	private static final String ENVIRONMENTS_FOLDER = "environments";

	/**
	 * File to save the agent mappings.
	 */
	private static final String AGENT_MAPPING_FILE = "agent-mappings.xml";

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link JAXBTransformator}.
	 */
	private JAXBTransformator transformator = new JAXBTransformator();

	/**
	 * Existing profiles in the system mapped by the id.
	 */
	private ConcurrentHashMap<String, Profile> existingProfiles;

	/**
	 * Existing environments in the system mapped by the id.
	 */
	private ConcurrentHashMap<String, Environment> existingEnvironments;

	/**
	 * Currently used agent mapping.
	 */
	private AtomicReference<AgentMappings> agentMappingsReference = new AtomicReference<>();

	/**
	 * Returns all existing profiles.
	 * 
	 * @return Returns all existing profiles.
	 */
	public Collection<Profile> getAllProfiles() {
		return new ArrayList<>(existingProfiles.values());
	}

	/**
	 * Returns the profile with the given id.
	 * 
	 * @param id
	 *            Id of profile.
	 * @return {@link Profile}
	 * @throws Exception
	 *             If profile with given id does not exist.
	 */
	public Profile getProfile(String id) throws Exception {
		Profile profile = existingProfiles.get(id);
		if (null == profile) {
			throw new Exception("Profile with the id=" + id + " does not exist on the CMR.");
		}
		return profile;
	}

	/**
	 * Creates new profile.
	 * 
	 * @param profile
	 *            Profile template.
	 * @return Returns created profile with correctly set id.
	 * @throws Exception
	 *             If saving fails. //TODO Change to business exception when exception change is
	 *             integrated.
	 */
	public Profile createProfile(Profile profile) throws Exception {
		profile.setId(getRandomUUIDString());
		profile.setCreatedDate(new Date());
		existingProfiles.put(profile.getId(), profile);
		saveProfile(profile);
		return profile;
	}

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
	 *             If saving fails. //TODO Change to business exception when exception change is
	 *             integrated.
	 */
	public Profile updateProfile(Profile profile) throws Exception {
		if (profile.isCommonProfile()) {
			throw new Exception("Common profiles can not be updated.");
		}

		return updateProfileInternal(profile);
	}

	/**
	 * Deletes the existing profile.
	 * 
	 * @param profile
	 *            Profile to delete.
	 * @throws Exception
	 *             If profile is default profile.. //TODO Business exception
	 */
	public void deleteProfile(Profile profile) throws Exception {
		if (profile.isCommonProfile()) {
			throw new Exception("Common profiles can not be deleted.");
		}

		String id = profile.getId();
		Profile local = existingProfiles.remove(id);
		if (null != local) {
			Files.deleteIfExists(getProfileFilePath(local));

			for (Environment environment : existingEnvironments.values()) {
				if (checkProfiles(environment)) {
					try {
						updateEnvironment(environment, false);
					} catch (Exception e) {
						log.error("Update of the environment on the profile deletion failed.", e);
					}
				}
			}
		}
	}

	/**
	 * Returns all existing environment.
	 * 
	 * @return Returns all existing environment.
	 */
	public Collection<Environment> getAllEnvironments() {
		return new ArrayList<>(existingEnvironments.values());
	}

	/**
	 * Returns the environment with the given id.
	 * 
	 * @param id
	 *            Id of environment.
	 * @return {@link Environment}
	 * @throws Exception
	 *             If environment with given id does not exist.
	 */
	public Environment getEnvironment(String id) throws Exception {
		Environment environment = existingEnvironments.get(id);
		if (null == environment) {
			throw new Exception("Environment with the id=" + id + " does not exist on the CMR.");
		}
		return environment;
	}

	/**
	 * Creates new environment.
	 * 
	 * @param environment
	 *            Environment template.
	 * @return Returns created environment with correctly set id.
	 * @throws Exception
	 *             If saving fails. //TODO Change to business exception when exception change is
	 *             integrated.
	 */
	public Environment createEnvironment(Environment environment) throws Exception {
		environment.setId(getRandomUUIDString());
		existingEnvironments.put(environment.getId(), environment);

		// add the default include profiles
		Set<String> profileIds = new HashSet<>();
		for (Profile profile : existingProfiles.values()) {
			if (profile.isDefaultProfile()) {
				profileIds.add(profile.getId());
			}
		}
		environment.setProfileIds(profileIds);

		saveEnvironment(environment);
		return environment;
	}

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
	 * @param checkProfiles
	 *            if environment should be checked for non existing profiles
	 * @return updated environment instance
	 * @throws Exception
	 *             If saving fails.
	 */
	public Environment updateEnvironment(Environment environment, boolean checkProfiles) throws Exception {
		if (checkProfiles) {
			checkProfiles(environment);
		}

		String id = environment.getId();
		environment.setRevision(environment.getRevision() + 1);
		Environment local = existingEnvironments.replace(id, environment);
		if (null == local) {
			existingEnvironments.remove(id);
			throw new Exception("Update of the environment '" + environment.getName() + "' failed because it does not exist on the CMR.");
		} else if (local != environment && local.getRevision() + 1 != environment.getRevision()) { // NOPMD
			// == check here if same object is used
			existingEnvironments.replace(id, local);
			Exception e = new Exception("Update of the environment '" + environment.getName() + "' failed because revision check failed (current sequence=" + local.getRevision()
					+ ", update sequence=" + environment.getRevision() + "). Most likely other user already updated the environment in question, please try again.");
			environment.setRevision(environment.getRevision() - 1);
			throw e;
		}
		saveEnvironment(environment);

		// if the name changes we should also delete local from disk
		if (!Objects.equals(environment.getName(), local.getName())) {
			Files.deleteIfExists(getEnvironmentFilePath(local));
		}

		return environment;
	}

	/**
	 * Deletes the existing environment.
	 * 
	 * @param environment
	 *            Environment to delete.
	 * @throws Exception
	 *             If deletion fails.
	 */
	public void deleteEnvironment(Environment environment) throws Exception {
		String id = environment.getId();
		Environment local = existingEnvironments.remove(id);
		if (null != local) {
			Files.deleteIfExists(getEnvironmentFilePath(local));

			AgentMappings agentMappings = agentMappingsReference.get();
			if (checkEnvironments(agentMappings)) {
				try {
					setAgentMappings(agentMappings, false);
				} catch (Exception e) {
					log.error("Update of the agent mappings on the environment deletion failed.", e);
				}
			}
		}
	}

	/**
	 * Returns the currently used agent mappings.
	 * 
	 * @return Returns the currently used agent mappings.
	 */
	public AgentMappings getAgentMappings() {
		return agentMappingsReference.get();
	}

	/**
	 * Sets the agent mappings to be used.
	 * 
	 * @param agentMappings
	 *            {@link AgentMappings}
	 * @param checkEnvironments
	 *            if mapping should be checked for non existing environments
	 * @return updated {@link AgentMappings} instance
	 * @throws Exception
	 *             If save fails. //TODO Change with business exception
	 */
	public AgentMappings setAgentMappings(AgentMappings agentMappings, boolean checkEnvironments) throws Exception {
		// check environment
		if (checkEnvironments) {
			checkEnvironments(agentMappings);
		}

		// ensure there is not overwrite

		AgentMappings current;
		do {
			current = agentMappingsReference.get();
			if (current.getRevision() != agentMappings.getRevision()) {
				throw new Exception("Update of the agent mappings failed because revision check failed (current sequence=" + current.getRevision() + ", update sequence=" + agentMappings.getRevision()
						+ "). Most likely other user already updated the profile in question, please try again.");
			}
		} while (!agentMappingsReference.compareAndSet(current, agentMappings));

		agentMappings.setRevision(agentMappings.getRevision() + 1);
		saveAgentMapping(agentMappings);
		return agentMappings;
	}

	/**
	 * Internal process of updating the profile.
	 * 
	 * @param profile
	 *            Profile being updated.
	 * @return Updated instance.
	 * @throws Exception
	 *             If update is not possible.
	 */
	private Profile updateProfileInternal(Profile profile) throws Exception {
		String id = profile.getId();
		profile.setRevision(profile.getRevision() + 1);
		Profile local = existingProfiles.replace(id, profile);
		if (null == local) {
			existingProfiles.remove(id);
			throw new Exception("Update of the profile '" + profile.getName() + "' failed because it does not exist on the CMR.");
		} else if (local != profile && local.getRevision() + 1 != profile.getRevision()) { // NOPMD
			// == check here if same object is used
			existingProfiles.replace(id, local);
			Exception e = new Exception("Update of the profile '" + profile.getName() + "' failed because revision check failed (current sequence=" + local.getRevision() + ", update sequence="
					+ profile.getRevision() + "). Most likely other user already updated the profile in question, please try again.");
			profile.setRevision(profile.getRevision() - 1);
			throw e;
		}
		profile.setUpdatedDate(new Date());
		saveProfile(profile);

		// if the name changes we should also delete local from disk
		if (!Objects.equals(profile.getName(), local.getName())) {
			Files.deleteIfExists(getProfileFilePath(local));
		}

		return profile;
	}

	/**
	 * Cleans the non-existing profiles from the {@link Environment}.
	 * 
	 * @param environment
	 *            {@link Environment}.
	 * @return if environment was changed during the check process
	 */
	private boolean checkProfiles(Environment environment) {
		boolean changed = false;
		if (CollectionUtils.isNotEmpty(environment.getProfileIds())) {
			for (Iterator<String> it = environment.getProfileIds().iterator(); it.hasNext();) {
				String profileId = it.next();
				if (!existingProfiles.containsKey(profileId)) {
					it.remove();
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Cleans the non-existing environments from the {@link AgentMappings}.
	 * 
	 * @param agentMappings
	 *            {@link AgentMappings}.
	 * @return if mappings where changed during the check process
	 */
	private boolean checkEnvironments(AgentMappings agentMappings) {
		boolean changed = false;
		if (CollectionUtils.isNotEmpty(agentMappings.getMappings())) {
			for (Iterator<AgentMapping> it = agentMappings.getMappings().iterator(); it.hasNext();) {
				AgentMapping agentMapping = it.next();
				if (!existingEnvironments.containsKey(agentMapping.getEnvironmentId())) {
					it.remove();
					changed = true;
				}
			}
		}
		return changed;
	}

	/**
	 * Saves profile and persists it to the list.
	 * 
	 * @param profile
	 *            Profile to be saved.
	 * @throws Exception
	 *             If saving fails.
	 */
	private void saveProfile(Profile profile) throws Exception {
		if (profile.isCommonProfile()) {
			throw new Exception("Common profiles can not be saved.");
		}
		transformator.marshall(getProfileFilePath(profile), profile, getProfilesPath().relativize(getSchemaPath()).toString());
	}

	/**
	 * Saves {@link Environment} to the disk.
	 * 
	 * @param environment
	 *            {@link Environment} to save.
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs. If saving fails.
	 */
	private void saveEnvironment(Environment environment) throws JAXBException, IOException {
		transformator.marshall(getEnvironmentFilePath(environment), environment, getEnvironmentPath().relativize(getSchemaPath()).toString());
	}

	/**
	 * Saves agent mapping.
	 * 
	 * @param agentMappings
	 *            To save
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws JAXBException
	 *             If {@link JAXBException} occurs. If saving fails.
	 */
	private void saveAgentMapping(AgentMappings agentMappings) throws JAXBException, IOException {
		transformator.marshall(getAgentMappingFilePath(), agentMappings, Paths.get(DEFAULT_CI_FOLDER).relativize(getSchemaPath()).toString());
	}

	/**
	 * Returns the schema path.
	 * 
	 * @return Returns the schema path.
	 */
	protected Path getSchemaPath() {
		return Paths.get(DEFAULT_CI_FOLDER, SCHEMA_FOLDER, SCHEMA_FILE);
	}

	/**
	 * Returns the directory where profiles are saved.
	 * 
	 * @return Profiles directory path.
	 */
	protected Path getProfilesPath() {
		return Paths.get(DEFAULT_CI_FOLDER, PROFILES_FOLDER);
	}

	/**
	 * Returns path pointing to the profile file.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @return Path to the file.
	 */
	protected Path getProfileFilePath(Profile profile) {
		String fileName = profile.getId() + "-" + profile.getName().replace(' ', '-') + ".xml";
		return getProfilesPath().resolve(fileName);
	}

	/**
	 * Returns the directory where environments are saved.
	 * 
	 * @return Environments directory path.
	 */
	protected Path getEnvironmentPath() {
		return Paths.get(DEFAULT_CI_FOLDER, ENVIRONMENTS_FOLDER);
	}

	/**
	 * Returns path pointing to the environment file.
	 * 
	 * @param environment
	 *            {@link Environment}
	 * @return Path to the file.
	 */
	protected Path getEnvironmentFilePath(Environment environment) {
		String fileName = environment.getId() + "-" + environment.getName().replace(' ', '-') + ".xml";
		return getEnvironmentPath().resolve(fileName);
	}

	/**
	 * Returns path pointing to the agent mapping file.
	 * 
	 * @return Path to the file.
	 */
	protected Path getAgentMappingFilePath() {
		return Paths.get(DEFAULT_CI_FOLDER, AGENT_MAPPING_FILE);
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		// WARNING loading must be done in this order
		loadExistingProfiles();
		loadExistingEnvironments();
		loadAgentMappings();
	}

	/**
	 * Loads all existing profiles.
	 */
	private void loadExistingProfiles() {
		log.info("|-Loading the existing Configuration interface profiles..");
		existingProfiles = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = getProfilesPath();
		final Path schemaPath = getSchemaPath();

		if (Files.notExists(path)) {
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							Profile profile = transformator.unmarshall(file, schemaPath, Profile.class);
							existingProfiles.put(profile.getId(), profile);
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing Configuration interface profile file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring Configuration interface profiles directory. Directory path: " + path.toString() + ".", e);
		}
	}

	/**
	 * Loads all existing profiles.
	 */
	private void loadExistingEnvironments() {
		log.info("|-Loading the existing Configuration interface environments..");
		existingEnvironments = new ConcurrentHashMap<>(16, 0.75f, 2);

		Path path = getEnvironmentPath();
		final Path schemaPath = getSchemaPath();

		if (Files.notExists(path)) {
			return;
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isXmlFile(file)) {
						try {
							Environment environment = transformator.unmarshall(file, schemaPath, Environment.class);
							existingEnvironments.put(environment.getId(), environment);

							// if checking of the profile made a change, save it
							if (checkProfiles(environment)) {
								try {
									saveEnvironment(environment);
								} catch (IOException | JAXBException e) {
									log.error("Error updating existing Configuration interface environment file. File path: " + file.toString() + ".", e);
								}
							}
						} catch (JAXBException | SAXException e) {
							log.error("Error reading existing Configuration interface environment file. File path: " + file.toString() + ".", e);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Error exploring Configuration interface environments directory. Directory path: " + path.toString() + ".", e);
		}

		// create at least one default environment if such does not exist
		if (MapUtils.isEmpty(existingEnvironments)) {
			Environment environment = new Environment();
			environment.setName("Default Environment");
			environment.setDescription("Environment that contains the default inspectIT monitoring settings and all default profiles.");
			try {
				createEnvironment(environment);
			} catch (Exception e) {
				log.error("Error creating default Configuration interface environment on the CMR.", e);
			}
		}
	}

	/**
	 * Loads currently used agent mapping file.
	 */
	private void loadAgentMappings() {
		log.info("|-Loading the existing Configuration interface agent mappings..");

		AgentMappings agentMappings;
		Path path = getAgentMappingFilePath();
		if (Files.notExists(path)) {
			agentMappings = new AgentMappings(Collections.<AgentMapping> emptyList());
		} else {
			try {
				agentMappings = transformator.unmarshall(path, getSchemaPath(), AgentMappings.class);
			} catch (JAXBException | IOException | SAXException e) {
				agentMappings = new AgentMappings(Collections.<AgentMapping> emptyList());
				log.error("Error loading Configuration interface agent mappings file. File path: " + path.toString() + ".", e);
			}
		}

		// set atomic reference
		agentMappingsReference.set(agentMappings);

		// check that mapped Environment exists
		if (checkEnvironments(agentMappings)) {
			try {
				saveAgentMapping(agentMappings);
			} catch (JAXBException | IOException e) {
				log.error("Error save Configuration interface agent mappings file. File path: " + path.toString() + ".", e);
			}
		}
	}

	/**
	 * If path is a file that ends with the <i>.xml</i> extension.
	 * 
	 * @param path
	 *            Path to the file.
	 * @return If path is a file that ends with the <i>.xml</i> extension.
	 */
	private boolean isXmlFile(Path path) {
		return !Files.isDirectory(path) && path.toString().endsWith(".xml");
	}

	/**
	 * Returns the unique String that will be used for IDs.
	 * 
	 * @return Returns unique string based on the {@link UUID}.
	 */
	private String getRandomUUIDString() {
		return String.valueOf(UUID.randomUUID().getLeastSignificantBits());
	}
}
