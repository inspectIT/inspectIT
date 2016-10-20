package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 *
 * Error code enumeration for the business context component.
 *
 * @author Alexander Wert
 *
 */
public enum BusinessContextErrorCodeEnum implements IErrorCode {

	/**
	 * Duplicate item.
	 */
	DUPLICATE_ITEM("Adding item failed.", "An item with the same identifier already exists.", null),

	/**
	 * Unknown application.
	 */
	UNKNOWN_APPLICATION("The application to execute the selected operation on does not exist.", "The application might be deleted.", null),

	/**
	 * Unknown business transaction.
	 */
	UNKNOWN_BUSINESS_TRANSACTION("The business transaction to execute the selected operation on does not exist.", "The business transaction might be deleted.", null),

	/**
	 * Invalid move operation.
	 */
	INVALID_MOVE_OPRATION("The move operation cannot be performed.", "The target index of the move operation is invalid.", "Check whether the target of the move operation is within the valid range."),

	/**
	 * IO operation failed.
	 */
	INPUT_OUTPUT_OPERATION_FAILED("IO operation failed trying to read or write the business context data bytes.", null, "Check disk status and that the write/read permissions exist."),

	/**
	 * JAXB (de-)marshall failed.
	 */
	JAXB_MARSHALLING_OR_DEMARSHALLING_FAILED("JAXB marshaling or demarshalling to/from disk failed.", "CMR version is not compatible with the data.", "Check the CMR version.");
	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Business Context";

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
	private BusinessContextErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
