package info.novatec.inspectit.cmr.service.exception;

/**
 * A exception that can occur during the communication to the CMR when its services are invoked.
 * 
 * @author Ivan Senic
 * 
 */
public class ServiceException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 5533129305062329555L;

	/**
	 * Constructs a new exception with <code>null</code> as its detail message. The cause is not
	 * initialized, and may subsequently be initialized by a call to {@link #initCause}.
	 */
	public ServiceException() {
	}

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public ServiceException(String message) {
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
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method).
	 *            (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent
	 *            or unknown.)
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
