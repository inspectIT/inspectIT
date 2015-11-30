package info.novatec.inspectit.agent.connection;

/**
 * <b>IMPORTANT:</b> The class code is copied/taken from <a
 * href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>. Original
 * author is William Grosso. License info can be found <a
 * href="http://www.oreilly.com/terms/">here</a>.
 * 
 * @author William Grosso
 */
public class ServerUnavailableException extends Exception {

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 0L;

	/**
	 * Denotes if the server timeout occurred. Defaults to <code>false</code>.
	 */
	private final boolean serverTimeout;

	/**
	 * Default constructor.
	 */
	public ServerUnavailableException() {
		this(false);
	}

	/**
	 * Constructor to set timeout state.
	 * 
	 * @param serverTimeout
	 *            if timeout occurred during server call.
	 */
	public ServerUnavailableException(boolean serverTimeout) {
		this.serverTimeout = serverTimeout;
	}

	/**
	 * Gets {@link #serverTimeout}.
	 * 
	 * @return {@link #serverTimeout}
	 */
	public boolean isServerTimeout() {
		return serverTimeout;
	}

}
