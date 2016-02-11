package info.novatec.inspectit.indexing.impl;

/**
 * Indexing exception class. Used for signaling problems with indexing elements.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexingException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8640380079673438659L;

	/**
	 * Same as {@link Exception#Exception(String)}.
	 * 
	 * @param message
	 *            the exception message.
	 */
	public IndexingException(String message) {
		super(message);
	}

	/**
	 * Same as {@link Exception#Exception(String, Throwable)}.
	 * 
	 * @param message
	 *            the exception message.
	 * @param throwable
	 *            the nested <code>Throwable</code> instance.
	 */
	public IndexingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
