package rocks.inspectit.server.ci;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.util.ResourcesPathResolver;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;

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
	 * File name where default configuration is stored.
	 */
	private static final String BUSINESS_CONTEXT_CONFIG_FILE = "businessContext.xml";

	/**
	 * Sub-folder for saving alert thresholds.
	 */
	private static final String ALERTING_DEFINITIONS_FOLDER = "alerting";

	/**
	 * Migration folder name.
	 */
	private static final String MIGRATION_FOLDER = "migration";

	/**
	 * Used with {@link ResourcesPathResolver} to get the file of the ci dir.
	 */
	private File ciDirFile;

	/**
	 * Initializes {@link #configDirFile}.
	 */
	@PostConstruct
	protected void init() {
		try {
			ciDirFile = ResourcesPathResolver.getResourceFile(DEFAULT_CI_FOLDER);
		} catch (IOException exception) {
			throw new BeanInitializationException("Property manager can not locate configuration directory.", exception);
		}
	}

	/**
	 * Returns the default CI folder.
	 *
	 * @return Returns the default CI folder.
	 */
	public Path getDefaultCiPath() {
		return ciDirFile.toPath();
	}

	/**
	 * Returns the schema path.
	 *
	 * @return Returns the schema path.
	 */
	public Path getSchemaPath() {
		return getDefaultCiPath().resolve(SCHEMA_FOLDER).resolve(SCHEMA_FILE);
	}

	/**
	 * Returns the schema path.
	 *
	 * @return Returns the schema path.
	 */
	public Path getMigrationPath() {
		return getDefaultCiPath().resolve(SCHEMA_FOLDER).resolve(MIGRATION_FOLDER);
	}

	/**
	 * Returns the directory where profiles are saved.
	 *
	 * @return Profiles directory path.
	 */
	public Path getProfilesPath() {
		return getDefaultCiPath().resolve(PROFILES_FOLDER);
	}

	/**
	 * Returns path pointing to the profile file.
	 *
	 * @param profile
	 *            {@link Profile}
	 * @return Path to the file.
	 */
	public Path getProfileFilePath(Profile profile) {
		String secureProfileName = removeIllegalFilenameCharacters(profile.getName());
		String fileName = profile.getId() + "-" + secureProfileName + ".xml";
		return getProfilesPath().resolve(fileName);
	}

	/**
	 * Returns the directory where environments are saved.
	 *
	 * @return Environments directory path.
	 */
	public Path getEnvironmentPath() {
		return getDefaultCiPath().resolve(ENVIRONMENTS_FOLDER);
	}

	/**
	 * Returns path pointing to the environment file.
	 *
	 * @param environment
	 *            {@link Environment}
	 * @return Path to the file.
	 */
	public Path getEnvironmentFilePath(Environment environment) {
		String secureEnvironmentName = removeIllegalFilenameCharacters(environment.getName());
		String fileName = environment.getId() + "-" + secureEnvironmentName + ".xml";
		return getEnvironmentPath().resolve(fileName);
	}

	/**
	 * Returns path pointing to the agent mapping file.
	 *
	 * @return Path to the file.
	 */
	public Path getAgentMappingFilePath() {
		return getDefaultCiPath().resolve(AGENT_MAPPING_FILE);
	}

	/**
	 * Returns path pointing to the business context file.
	 *
	 * @return Path to the file.
	 */
	public Path getBusinessContextFilePath() {
		return getDefaultCiPath().resolve(BUSINESS_CONTEXT_CONFIG_FILE);
	}

	/**
	 * Returns the directory where alert thresholds are saved.
	 *
	 * @return Path to the folder of the alert thresholds.
	 */
	public Path getAlertingDefinitionsPath() {
		return getDefaultCiPath().resolve(ALERTING_DEFINITIONS_FOLDER);
	}

	/**
	 * Returns path pointing to the alerting definition file.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition}
	 * @return Path to the file.
	 */
	public Path getAlertingDefinitionFilePath(AlertingDefinition alertingDefinition) {
		String secureDefinitionName = removeIllegalFilenameCharacters(alertingDefinition.getName());
		String fileName = alertingDefinition.getId() + "-" + secureDefinitionName + ".xml";
		return getAlertingDefinitionsPath().resolve(fileName);
	}

	/**
	 * Removes replaces all characters which match not the following <code>a-zA-Z0-9.-</code> by
	 * <code>_</code> and ensures a valid filename.
	 *
	 * @param input
	 *            {@link String} possibly containing illegal filename chars.
	 * @return {@link String} without illegal filename characters
	 */
	private String removeIllegalFilenameCharacters(String input) {
		return input.replaceAll("[^a-zA-Z0-9.-]", "_");
	}
}
