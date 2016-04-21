package rocks.inspectit.server.diagnosis.engine;

/**
 * Umbrella exception for failures in the engine.
 *
 * @author Claudio Waldvogel
 */
public class DiagnosisEngineException extends Exception {

	/**
	 * The serial version ID.
	 */
	private static final long serialVersionUID = -5280019890160625754L;

	/**
	 * Default constructor to create a new exception.
	 *
	 * @param message
	 *            The error message.
	 */
	public DiagnosisEngineException(String message) {
		super(message);
	}

	/**
	 * Constructor to create a new exception with embedded root cause.
	 *
	 * @param message
	 *            The error message.
	 * @param cause
	 *            The root cause
	 */
	public DiagnosisEngineException(String message, Throwable cause) {
		super(message, cause);
	}
}
