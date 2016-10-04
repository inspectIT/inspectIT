/**
 *
 */
package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 * Error code enumeration for the alerting definition component.
 *
 * @author Marius Oehler
 *
 */
public enum AlertingDefinitionErrorCodeEnum implements IErrorCode {

	/**
	 * Threshold definition tag is null.
	 */
	TAG_KEY_IS_NULL("The given tag key must not be null.", null, null),

	/**
	 * Threshold definition tag is empty.
	 */
	TAG_KEY_IS_EMPTY("The given tag key must not be empty.", null, null),

	/**
	 * Threshold definition tag is null.
	 */
	TAG_VALUE_IS_NULL("The given tag value must not be null.", null, null),

	/**
	 * Threshold definition tag is empty.
	 */
	TAG_VALUE_IS_EMPTY("The given tag value must not be empty.", null, null),

	/**
	 * The used tag key already exists.
	 */
	TAG_KEY_ALREADY_EXISTS("The given tag key already exists in the alerting definition.", null, null),

	/**
	 * The tag key does not exists.
	 */
	TAG_KEY_DOES_NOT_EXISTS("The given tag key does not exist in the alerting definition.", null, null),

	/**
	 * Given email address is null.
	 */
	EMAIL_IS_NULL("The given email adress must not be null.", null, null),

	/**
	 * Given email address is empty.
	 */
	EMAIL_IS_EMPTY("The given email adress must not be empty.", null, null),

	/**
	 * The alerting definition does not exists.
	 */
	ALERTING_DEFINITION_DOES_NOT_EXIST("The alerting definition does not exist.", null, null),

	/**
	 * Revision failed.
	 */
	REVISION_CHECK_FAILED("Revision check of the resource failed as the revision number is lower than one existing on the server.", "Alerting definition has a newer version.", "Reload resource and try again."),

	/**
	 * The email address is not valid.
	 */
	EMAIL_IS_NOT_VALID("The e-mail address is not in a valid pattern.", null, null),

	/**
	 * Replacing notification e-mails with null.
	 */
	REPLACING_WITH_NULL("Replacing the current object with 'null'.", null, null),

	/**
	 * The e-mail already exists.
	 */
	EMAIL_ALREADY_EXISTS("The e-mail address already exists.", null, null),

	/**
	 * The id is null.
	 */
	MISSING_ID("The id of the alerting definition is null.", "The alerting definition was possibly not created by the configuration interface manager.", null);

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Alerting Definition";

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
	AlertingDefinitionErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
	@Override
	public String getComponent() {
		return COMPONENT_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return WordUtils.capitalizeFully(this.toString().replace("_", " ").toLowerCase());
	}

	/**
	 * Gets {@link #description}.
	 *
	 * @return {@link #description}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Gets {@link #possibleCause}.
	 *
	 * @return {@link #possibleCause}
	 */
	@Override
	public String getPossibleCause() {
		return possibleCause;
	}

	/**
	 * Gets {@link #possibleSolution}.
	 *
	 * @return {@link #possibleSolution}
	 */
	@Override
	public String getPossibleSolution() {
		return possibleSolution;
	}

}
