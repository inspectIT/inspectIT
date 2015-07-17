package info.novatec.inspectit.rcp.preferences;

/**
 * Exception that is thrown with regard to preference saving and loading.
 * 
 * @author Ivan Senic
 * 
 */
public class PreferenceException extends Exception {

	/**
	 * Generated GUI.
	 */
	private static final long serialVersionUID = 50343882577720433L;

	/**
	 * Default constructor.
	 */
	public PreferenceException() {
		super();
	}

	/**
	 * @param message
	 *            Message.
	 */
	public PreferenceException(String message) {
		super(message);
	}

	/**
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public PreferenceException(String message, Throwable cause) {
		super(message, cause);
	}
}
