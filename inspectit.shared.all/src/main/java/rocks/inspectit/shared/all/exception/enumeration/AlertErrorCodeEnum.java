package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 * Error code enumeration for the alerting service.
 *
 * @author Marius Oehler, Alexander Wert
 *
 */
public enum AlertErrorCodeEnum implements IErrorCode {

	/**
	 * The alerting definition does not exists.
	 */
	ALERTING_DEFINITION_DOES_NOT_EXIST("The alerting definition does not exist.", null, null),

	/**
	 * Revision failed.
	 */
	REVISION_CHECK_FAILED("Revision check of the resource failed as the revision number is lower than one existing on the server.", "Alerting definition has a newer version.", "Reload resource and try again."),

	/**
	 * The id is null.
	 */
	MISSING_ID("The id of the alerting definition is null.", "The alerting definition was possibly not created by the configuration interface manager.", null),

	/**
	 * Database offline.
	 */
	DATABASE_OFFLINE("The timeseries database is offline.", "Either the connection settings are wrong or the timeseries database is not running.", null),

	/**
	 * Unknown alert id.
	 */
	UNKNOWN_ALERT_ID("The given alert id is unknown.", "Either the alert id has expired or is invalid (has a typo).", null),

	/**
	 * The alert is not related to a business transaction.
	 */
	NO_BTX_ALERT("The alert is not related to any buisness transaction.", null, null);

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
	AlertErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
