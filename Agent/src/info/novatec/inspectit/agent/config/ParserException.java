package info.novatec.inspectit.agent.config;

/**
 * This exception is thrown whenever {@link IConfigurationReader} implementation tries to parse a
 * given source and finds some errors in it.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ParserException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -7005044097962205063L;

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message
	 *            The detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public ParserException(String message) {
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
	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}

}
