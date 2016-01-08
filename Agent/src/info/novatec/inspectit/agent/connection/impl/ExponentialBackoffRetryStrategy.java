package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.RetryStrategy;

/**
 * The classic "if it doesn't get fixed in n seconds, wait 2n seconds and try again" strategy. Using
 * a large number of retries in this one results in enormously long delays.
 * <p>
 * You probably don't want to use an ExponentialBackoffRetryStrategy in a thread which needs to be
 * responsive (e.g. in the Swing event handling thread).
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken from
 * <a href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>.
 * Original author is William Grosso. License info can be found
 * <a href="http://www.oreilly.com/terms/">here</a>.
 * 
 * @author William Grosso
 */

public class ExponentialBackoffRetryStrategy extends RetryStrategy {

	/**
	 * The starting wait time.
	 */
	public static final long STARTING_WAIT_TIME = 3000;

	/**
	 * The current time to wait for the next retry.
	 */
	private long currentTimeToWait;

	/**
	 * Default constructor which initializes the class with the default values.
	 */
	public ExponentialBackoffRetryStrategy() {
		this(DEFAULT_NUMBER_OF_RETRIES, STARTING_WAIT_TIME);
	}

	/**
	 * Additional constructor with two parameters. The first one defines the number of retries till
	 * it completely fails. The second specifies the starting wait time.
	 * 
	 * @param numberOfRetries
	 *            The number of retries.
	 * @param startingWaitTime
	 *            The starting wait time.
	 */
	public ExponentialBackoffRetryStrategy(int numberOfRetries, long startingWaitTime) {
		super(numberOfRetries);
		currentTimeToWait = startingWaitTime;
	}

	/**
	 * {@inheritDoc}
	 */
	protected long getTimeToWait() {
		long returnValue = currentTimeToWait;
		currentTimeToWait *= 2;
		return returnValue;
	}
}
