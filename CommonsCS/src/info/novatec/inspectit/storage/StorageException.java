package info.novatec.inspectit.storage;

/**
 * Exception that can occur with storage operations.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8429943259996300898L;

	/**
	 * Default constructor.
	 */
	public StorageException() {
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 * @param message
	 *            Message to set.
	 * @param cause
	 *            Cause to set.
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 * @param message
	 *            Message to set.
	 */
	public StorageException(String message) {
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 * @param cause
	 *            Cause to set.
	 */
	public StorageException(Throwable cause) {
		super(cause);
	}

}