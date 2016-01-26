package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 * Error code enumeration for the storage component.
 * 
 * @author Ivan Senic
 * 
 */
public enum StorageErrorCodeEnum implements IErrorCode {

	/**
	 * Name not provided.
	 */
	STORAGE_NAME_IS_NOT_PROVIDED("Creating a storage requires to provide a name for new storage.", null, null),

	/**
	 * Storage not existing on the CMR.
	 */
	STORAGE_DOES_NOT_EXIST("The storage to execute the selected operation on does not exist.", "The storage might be deleted.", null),

	/**
	 * Storage not closed on the CMR.
	 */
	STORAGE_IS_NOT_CLOSED("The storage to execute the selected operation on is not yet closed.", null, "Close the storage first and try again."),

	/**
	 * Storage is not opened.
	 */
	STORAGE_IS_NOT_OPENED("The storage to execute the selected operation on is not yet opened.", null, "Open the storage first and try again."),

	/**
	 * Storage can not be reopened.
	 */
	STORAGE_CAN_NOT_BE_REOPENED("Attempt has been made to open already closed storage.", null, null),

	/**
	 * Storage can not be closed.
	 */
	STORAGE_CAN_NOT_BE_CLOSED("The selected storage can not be closed at the moment.", "Storage is currently used for recording or recording has been scheduled.", null),

	/**
	 * Storage already closed.
	 */
	STORAGE_ALREADY_CLOSED("The storage to execute the selected operation on is closed.", null, null),

	/**
	 * Storage already downloaded.
	 */
	STORAGE_ALREADY_DOWNLOADED("The selected storage is already fully downloaded.", null, null),

	/**
	 * Storage can be be uploaded.
	 */
	STORAGE_IS_NOT_DOWNLOADED("The operation to execute failed because the selected storage is not fully downloaded.", null, "Download the storage first and try again."),

	/**
	 * Recording can not be started.
	 */
	CAN_NOT_START_RECORDING("The recording can not be started at the moment.", "Recording is already active.", "Stop the recording if the one is currently active."),

	/**
	 * Storage can not write.
	 */
	WRITE_FAILED("Writing data to the storage failed.", "Storage is currently used for recording.", null),

	/**
	 * Unpacking failed due to the low disk space.
	 */
	LOW_DISK_SPACE("The operation to execute failed due to the low amount of the space on the hard drive.", null, "Provide additional space on the hard drive."),

	/**
	 * Storage file does not exist.
	 */
	FILE_DOES_NOT_EXIST("The file or directory does not exist on the given path.", null, null),

	/**
	 * Can not delete storage label type.
	 */
	LABEL_TYPE_CAN_NOT_BE_DELETED("Label types can not be deleted if any label of the selected type still exists.", null, "Delete first all labels of the selected type and try again."),

	/**
	 * Serialization failed.
	 */
	SERIALIZATION_FAILED("(De-)Serialization of storage data bytes failed.", "CMR version for the storage not compatible.", "Perform the action with the compatible version of the CMR."),

	/**
	 * IO operation failed.
	 */
	INPUT_OUTPUT_OPERATION_FAILED("IO operation failed trying to read or write the storage data bytes.", null, "Check disk status and that the write/read permissions exist.");

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Storage";

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
	private StorageErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
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
