package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.RetryStrategy;

/**
 * Strategy that fails fast. Has 1 retry attempts and has no waiting.
 * 
 * @author Ivan Senic
 * 
 */
public class FailFastRetryStrategy extends RetryStrategy {

	/**
	 * Default constructor. As we want fail fast, this will set <code>1</code> attempts.
	 * 
	 * @see RetryStrategy#RetryStrategy(int)
	 */
	public FailFastRetryStrategy() {
		super(1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getTimeToWait() {
		return 0;
	}

}
