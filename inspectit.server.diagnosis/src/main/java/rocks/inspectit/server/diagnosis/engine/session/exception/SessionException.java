package rocks.inspectit.server.diagnosis.engine.session.exception;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;

/**
 * Common exception for all failures which can occur while executing a <code>Session</code>.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class SessionException extends DiagnosisEngineException {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -7588955901539457468L;

	/**
	 * Default Constructor.
	 *
	 * @param message
	 *            The error message
	 */
	public SessionException(String message) {
		super(message);
	}

	/**
	 * Constructor with cause.
	 *
	 * @param message
	 *            The error message
	 * @param cause
	 *            The cause of the error
	 */
	public SessionException(String message, Throwable cause) {
		super(message, cause);
	}
}
