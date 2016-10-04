package rocks.inspectit.server.influx.util;

/**
 * @author Marius Oehler
 *
 */
public class QueryResultWrapperException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -4678048245271802513L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public QueryResultWrapperException(String message) {
		super(message);
	}

}
