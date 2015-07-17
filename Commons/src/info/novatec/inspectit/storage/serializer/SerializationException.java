package info.novatec.inspectit.storage.serializer;

/**
 * Serialization exception class.
 * 
 * @author Ivan Senic
 * 
 */
public class SerializationException extends Exception {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -233619748894539822L;

	/**
	 * Default construtor.
	 */
	public SerializationException() {
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            Exception message.
	 * @see Exception#Exception(String)
	 */
	public SerializationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param throwable
	 *            Throwable.
	 * @see Exception#Exception(Throwable)
	 */
	public SerializationException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            Exception message.
	 * @param throwable
	 *            Throwable.
	 * @see Exception#Exception(String, Throwable)
	 */
	public SerializationException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
