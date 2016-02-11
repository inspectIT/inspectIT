package info.novatec.inspectit.agent.config;

import info.novatec.inspectit.agent.config.impl.PropertyAccessor;

/**
 * This exception is thrown whenever something unexpected happens while accessing a property.
 * 
 * @author Patrice Bouillet
 * @see PropertyAccessor
 */
public class PropertyAccessException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5939579158523517975L;

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message
	 *            The detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public PropertyAccessException(String message) {
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
	public PropertyAccessException(String message, Throwable cause) {
		super(message, cause);
	}

}
