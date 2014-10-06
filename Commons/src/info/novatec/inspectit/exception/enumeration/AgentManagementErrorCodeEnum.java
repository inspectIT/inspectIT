package info.novatec.inspectit.exception.enumeration;

import info.novatec.inspectit.exception.IErrorCode;

import org.apache.commons.lang.WordUtils;

/**
 * Error code enumeration for the storage component.
 * 
 * @author Ivan Senic
 * 
 */
public enum AgentManagementErrorCodeEnum implements IErrorCode {

	/**
	 * Agent does not exists.
	 */
	AGENT_DOES_NOT_EXIST("The agent to execute selected operation on does not exist.", "The agent might be deleted.", null),

	/**
	 * Can not delete agent.
	 */
	AGENT_CAN_NOT_BE_DELETED("Selected agent can not be deleted at the moment.", "The agent is currently connected.", "Disconnect the agent and try again."),

	/**
	 * More than one agent registered.
	 */
	MORE_THAN_ONE_AGENT_REGISTERED("More than one agent is registered with the same properties.", null, "Send your database to inspectIT team.");

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Agent Management";

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
	private AgentManagementErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
