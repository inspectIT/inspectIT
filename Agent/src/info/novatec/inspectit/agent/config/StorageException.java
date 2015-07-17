package info.novatec.inspectit.agent.config;

/**
 * This exception is thrown whenever something unexpected happens while trying to access/store/load
 * something from the {@link IConfigurationStorage} implementation.
 * 
 * @author Patrice Bouillet
 * 
 */
public class StorageException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1644533648562152813L;

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message
	 *            The detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public StorageException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically
	 * incorporated in this exception's detail message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            The cause (which is saved for later retrieval by the {@link #getCause()} method).
	 *            (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent
	 *            or unknown.)
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

}
