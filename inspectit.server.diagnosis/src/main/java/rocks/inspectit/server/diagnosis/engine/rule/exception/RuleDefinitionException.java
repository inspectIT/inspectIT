package rocks.inspectit.server.diagnosis.engine.rule.exception;

/**
 * Exception is raised if an invalid
 * {@link rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition} is detected.
 *
 * @author Claudio Waldvogel
 */
public class RuleDefinitionException extends RuntimeException {

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
