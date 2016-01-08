package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.RetryStrategy;

/**
 * The most commonly used retry strategy; it extends the waiting period by a constant amount with
 * each retry.
 * 
 * Note that the default version of this (e.g. the one with a zero argument constructor) will make 3
 * calls and wind up waiting approximately 11 seconds (zero wait for the first call, 3 seconds for
 * the second call, and 8 seconds for the third call). These wait times are pretty small, and are
 * usually dwarfed by socket timeouts when network difficulties occur anyway.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken from
 * <a href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>.
 * Original author is William Grosso. License info can be found
 * <a href="http://www.oreilly.com/terms/">here</a>.
 * 
 * @author William Grosso
 */
public class AdditiveWaitRetryStrategy extends RetryStrategy {

	/**
	 * The starting wait time.
	 */
	public static final long STARTING_WAIT_TIME = 3000;

	/**
	 * Every time there is an exception, we add to the waiting time the specified one.
	 */
	public static final long WAIT_TIME_INCREMENT = 5000;

	/**
	 * The current time to wait.
	 */
	private long currentTimeToWait;

	/**
	 * The additional time to wait every time.
	 */
	private long waitTimeIncrement;

	/**
	 * The default constructor which initializes the class with the predefined values.
	 */
	public AdditiveWaitRetryStrategy() {
		this(DEFAULT_NUMBER_OF_RETRIES, STARTING_WAIT_TIME, WAIT_TIME_INCREMENT);
	}

	/**
	 * This constructor takes three arguments, the first is the actual number of retries till we
	 * completely fail. The second one is the first wait time when an error occurs, and the third
	 * one is the additional wait time every time when an error occurs.
	 * 
	 * @param numberOfRetries
	 *            The number of retries till it completely fails.
	 * @param startingWaitTime
	 *            The starting wait time.
	 * @param waitTimeIncrement
	 *            The additional wait time every time the sending etc. fails.
	 */
	public AdditiveWaitRetryStrategy(int numberOfRetries, long startingWaitTime, long waitTimeIncrement) {
		super(numberOfRetries);

		this.currentTimeToWait = startingWaitTime;
		this.waitTimeIncrement = waitTimeIncrement;
	}

	/**
	 * {@inheritDoc}
	 */
	protected long getTimeToWait() {
		long returnValue = currentTimeToWait;

		currentTimeToWait += waitTimeIncrement;

		return returnValue;
	}
}
