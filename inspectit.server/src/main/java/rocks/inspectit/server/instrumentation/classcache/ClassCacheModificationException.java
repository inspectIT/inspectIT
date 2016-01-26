package rocks.inspectit.server.instrumentation.classcache;

/**
 * Exception thrown before the modification of the class cache is performed.
 * 
 * @author Stefan Siegl
 * 
 */
public class ClassCacheModificationException extends Exception {

	/**
	 * serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param reason
	 *            the textual reason.
	 */
	public ClassCacheModificationException(String reason) {
		super(reason);
	}
}
