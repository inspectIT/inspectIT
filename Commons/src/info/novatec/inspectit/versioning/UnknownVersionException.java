package info.novatec.inspectit.versioning;

/**
 * Exception raised if the version of inspectIT is unknown. This can happen during development where
 * we do not keep a version or if the version string does not comply with our patterns.
 * 
 * @author Stefan Siegl
 */
public class UnknownVersionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 * 
	 * @param message
	 *            the message to display.
	 */
	public UnknownVersionException(String message) {
		super(message);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message
	 *            the message to display.
	 * @param cause
	 *            the chained cause.
	 */
	public UnknownVersionException(String message, Throwable cause) {
		super(message, cause);
	}

}
