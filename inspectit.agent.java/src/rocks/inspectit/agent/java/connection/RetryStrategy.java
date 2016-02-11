package rocks.inspectit.agent.java.connection;

/**
 * <b>IMPORTANT:</b> The class code is copied/taken from <a
 * href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>. Original
 * author is William Grosso. License info can be found <a
 * href="http://www.oreilly.com/terms/">here</a>.
 * 
 * @author William Grosso
 */
public abstract class RetryStrategy {

	/**
	 * The default number of retries.
	 */
	public static final int DEFAULT_NUMBER_OF_RETRIES = 3;

	/**
	 * How many tries are left till we go on.
	 */
	private int numberOfTriesLeft;

	/**
	 * Initializes the class with the default number of retries.
	 */
	public RetryStrategy() {
		this(DEFAULT_NUMBER_OF_RETRIES);
	}

	/**
	 * Initializes the class with the given number of retries.
	 * 
	 * @param numberOfRetries
	 *            The number of retries to use.
	 */
	public RetryStrategy(final int numberOfRetries) {
		numberOfTriesLeft = numberOfRetries;
	}

	/**
	 * Shall we retry when an error occurs?
	 * 
	 * @return If we will retry the sending.
	 */
	public final boolean shouldRetry() {
		return 0 < numberOfTriesLeft;
	}

	/**
	 * Called when a remote exception occured at the server. Two options are available here, the
	 * first is to raise an exception, and the second is to wait till we are going for a retry.
	 * 
	 * @throws RetryException
	 *             Thrown if we won't try to send data anymore.
	 */
	public final void remoteExceptionOccured() throws RetryException {
		numberOfTriesLeft--;

		if (!shouldRetry()) {
			throw new RetryException();
		}

		waitUntilNextTry();
	}

	/**
	 * Has to be overwritten by subclasses to specify the time till we'll try the next
	 * connecting/sending.
	 * 
	 * @return Returns a value in milliseconds of how long we'll wait.
	 */
	protected abstract long getTimeToWait();

	/**
	 * Will suspend the actual thread and waits for the time we get through {@link #getTimeToWait()}
	 * .
	 */
	private void waitUntilNextTry() {
		long timeToWait = getTimeToWait();

		try {
			Thread.sleep(timeToWait);
		} catch (InterruptedException ignored) { // NOCHK
			// nothing to do here
		}
	}
}
