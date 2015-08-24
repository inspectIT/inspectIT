package info.novatec.inspectit.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import info.novatec.inspectit.exception.IErrorCode;

/**
 * Error code enumeration for the versioning component.
 * 
 * @author Stefan Siegl
 * 
 */
public enum VersioningErrorCodeEnum implements IErrorCode {

	/** File cannot be found. */
	VERSION_FILE_IO("The version file version.log cannot be found or read.", "The version file might be deleted.",
			"Ensure that you did not delete the version file. Re-Install the solution. If this problem arises again, please file a bug with inspectIT."),

	/** No version in file. */
	VERSION_NOT_IN_FILE("The version file version.log does not contain a version.", null,
			"Ensure that you did not change the version file by hand. Re-Install the solution. If this problem arises again, please file a bug with inspectIT."),

	/** Invalid version. */
	VERSION_INVALID("The version of inspectIT has an invalid format.", null,
			"Ensure that you did not change the version file by hand. Re-Install the solution. If this problem arises again, please file a bug with inspectIT.");

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Versioning";

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
	private VersioningErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
		return WordUtils.capitalizeFully(this.toString().toLowerCase());
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
