package info.novatec.inspectit.cmr.ci;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

/**
 * Class that knows how to resolve paths related to the configuration interface.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ConfigurationInterfacePathResolver {

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
	 * Returns the default CI folder.
	 * 
	 * @return Returns the default CI folder.
	 */
	public Path getDefaultCiPath() {
		return Paths.get(DEFAULT_CI_FOLDER);
	}

	/**
	 * Returns the schema path.
	 * 
	 * @return Returns the schema path.
	 */
	public Path getSchemaPath() {
		return Paths.get(DEFAULT_CI_FOLDER, SCHEMA_FOLDER, SCHEMA_FILE);
	}

	/**
	 * Returns the directory where profiles are saved.
	 * 
	 * @return Profiles directory path.
	 */
	public Path getProfilesPath() {
		return Paths.get(DEFAULT_CI_FOLDER, PROFILES_FOLDER);
	}

	/**
	 * Returns path pointing to the profile file.
	 * 
	 * @param profile
	 *            {@link Profile}
	 * @return Path to the file.
	 */
	public Path getProfileFilePath(Profile profile) {
		String fileName = profile.getId() + "-" + profile.getName().replace(' ', '-') + ".xml";
		return getProfilesPath().resolve(fileName);
	}

	/**
	 * Returns the directory where environments are saved.
	 * 
	 * @return Environments directory path.
	 */
	public Path getEnvironmentPath() {
		return Paths.get(DEFAULT_CI_FOLDER, ENVIRONMENTS_FOLDER);
	}

	/**
	 * Returns path pointing to the environment file.
	 * 
	 * @param environment
	 *            {@link Environment}
	 * @return Path to the file.
	 */
	public Path getEnvironmentFilePath(Environment environment) {
		String fileName = environment.getId() + "-" + environment.getName().replace(' ', '-') + ".xml";
		return getEnvironmentPath().resolve(fileName);
	}

	/**
	 * Returns path pointing to the agent mapping file.
	 * 
	 * @return Path to the file.
	 */
	public Path getAgentMappingFilePath() {
		return Paths.get(DEFAULT_CI_FOLDER, AGENT_MAPPING_FILE);
	}
}
