package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 * Error code enumeration for the CI component.
 * 
 * @author Ivan Senic
 * 
 */
public enum ConfigurationInterfaceErrorCodeEnum implements IErrorCode {

	/**
	 * Profile not existing on the CMR.
	 */
	PROFILE_DOES_NOT_EXIST("The profile to execute the selected operation on does not exist.", "The profile might be deleted.", null),

	/**
	 * Common profiles can not be changed.
	 */
	COMMON_PROFILE_CAN_NOT_BE_ALTERED("Creating, updating or deleting of the common profiles is not allowed.", null, null),

	/**
	 * Revision failed.
	 */
	REVISION_CHECK_FAILED("Revision check of the resource failed as the revision number is lower than one existing on the server.", "Profile/environment/mappings has a newer version.",
			"Reload resource and try again."),

	/**
	 * Environment not existing on the CMR.
	 */
	ENVIRONMENT_DOES_NOT_EXIST("The environment to execute the selected operation on does not exist.", "The environment might be deleted.", null),

	/**
	 * IO operation failed.
	 */
	INPUT_OUTPUT_OPERATION_FAILED("IO operation failed trying to read or write the storage data bytes.", null, "Check disk status and that the write/read permissions exist."),

	/**
	 * JAXB (de-)marshall failed.
	 */
	JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED("JAXB marshaling or demarshalling to/from disk failed.", "CMR version is not compatible with the data.", "Check the CMR version."),

	/**
	 * Environment not found when registering the agent.
	 */
	ENVIRONMENT_FOR_AGENT_NOT_FOUND("Locating an environment for the agent to use failed.", "No  matching environment found for the specified agent name and IP address(es).",
			"Check the agent mapping settings in the Configuration Interface."),

	/**
	 * More than one environment found.
	 */
	MORE_THAN_ONE_ENVIRONMENT_FOR_AGENT_FOUND("Locating an environment for the agent to use failed.", "More than one environment found for the specified agent name and IP address(es).",
			"Check the agent mapping settings in the Configuration Interface.");

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Configuration Interface";

	/**
	 * Description of the error code.
	 */
	private final String description;

	/**
	 * Possible cause(es) for the error.
	 */
	private final String possibleCause;

	/**
	 * Possible solution(s) for the error.
	 */
	private final String possibleSolution;

	/**
	 * Constructor.
	 * 
	 * @param description
	 *            Description of the error code.
	 * @param possibleCause
	 *            Possible cause(es) for the error.
	 * @param possibleSolution
	 *            Possible solution(s) for the error.
	 */
	private ConfigurationInterfaceErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
		if (null == description) {
			throw new IllegalArgumentException("Description for the error code must not be null");
		}
		this.description = description;
		this.possibleCause = possibleCause;
		this.possibleSolution = possibleSolution;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getComponent() {
		return COMPONENT_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return WordUtils.capitalizeFully(this.toString().replace("_", " ").toLowerCase());
	}

	/**
	 * Gets {@link #description}.
	 * 
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets {@link #possibleCause}.
	 * 
	 * @return {@link #possibleCause}
	 */
	public String getPossibleCause() {
		return possibleCause;
	}

	/**
	 * Gets {@link #possibleSolution}.
	 * 
	 * @return {@link #possibleSolution}
	 */
	public String getPossibleSolution() {
		return possibleSolution;
	}

}
