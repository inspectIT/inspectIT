package info.novatec.inspectit.version;

import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.VersioningErrorCodeEnum;

/**
 * Exception raised if the version of inspectIT is unknown. This can happen during development where
 * we do not keep a version or if the version string does not comply with our patterns.
 *
 * @author Stefan Siegl
 */
public class InvalidVersionException extends TechnicalException {

	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = 7842488997286420660L;

	/**
	 * Default constructor.
	 *
	 * @param errorCode
	 *            Error code describing the exception.
	 * @param cause
	 *            Technical cause of the exception.
	 */
	public InvalidVersionException(VersioningErrorCodeEnum errorCode, Throwable cause) {
		super(errorCode, cause);
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
	public InvalidVersionException(String actionPerformed, VersioningErrorCodeEnum errorCode, Throwable cause) {
		super(actionPerformed, errorCode, cause);
	}

}
