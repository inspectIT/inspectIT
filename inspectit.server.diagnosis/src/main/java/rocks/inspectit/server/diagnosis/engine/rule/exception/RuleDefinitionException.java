package rocks.inspectit.server.diagnosis.engine.rule.exception;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;

/**
 * Exception is raised if an invalid
 * {@link rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition} is detected.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class RuleDefinitionException extends DiagnosisEngineException {

	/**
	 * The serial version ID.
	 */
	private static final long serialVersionUID = 96081583948602202L;

	/**
	 * Default constructor.
	 *
	 * @param message
	 *            A detailed error description
	 */
	public RuleDefinitionException(String message) {
		super(message);
	}

	/**
	 * Constructor to define a root cause.
	 *
	 * @param message
	 *            A detailed error description
	 * @param cause
	 *            The root cause
	 */
	public RuleDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}
}
