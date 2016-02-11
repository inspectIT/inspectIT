package info.novatec.inspectit.exception;

/**
 * Technical exception is special type of exception that occurred due to the technical problem
 * (IOException, SerializationException, etc) but we know details like what action was performed or
 * what might be the problem causes and solutions.
 * 
 * @author Ivan Senic
 * 
 */
public class TechnicalException extends BusinessException {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -3729639474538426711L;

	/**
	 * No-arg constructor for serialization.
	 */
	public TechnicalException() {
		super();
	}

	/**
	 * Default constructor.
	 * 
	 * @param errorCode
	 *            Error code describing the exception.
	 * @param cause
	 *            Technical cause of the exception.
	 */
	public TechnicalException(IErrorCode errorCode, Throwable cause) {
		super(errorCode);
		initCause(cause);
	}

	/**
	 * Constructor that allows definition of the action.
	 * 
	 * @param actionPerformed
	 *            Action being performed when exception occurred.
	 * @param errorCode
	 *            Error code describing the exception.
	 * @param cause
	 *            Technical cause of the exception.
	 */
	public TechnicalException(String actionPerformed, IErrorCode errorCode, Throwable cause) {
		super(actionPerformed, errorCode);
		initCause(cause);
	}

}
