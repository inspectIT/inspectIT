package rocks.inspectit.agent.java.core;

import rocks.inspectit.agent.java.core.impl.PlatformManager;

/**
 * This exception is thrown whenever something happens unexpectedly while accessing or registering
 * an ID (method, sensor type, agent, ...).
 * 
 * @author Patrice Bouillet
 * @see IPlatformManager
 * @see PlatformManager
 */
public class IdNotAvailableException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -436483658552247186L;

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized,
	 * and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message
	 *            The detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public IdNotAvailableException(String message) {
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
	public IdNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
