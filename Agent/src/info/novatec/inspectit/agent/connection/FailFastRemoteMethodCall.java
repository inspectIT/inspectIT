package info.novatec.inspectit.agent.connection;

import info.novatec.inspectit.agent.connection.impl.FailFastRetryStrategy;

/**
 * Helper class for the fail fast remote method calls.
 * 
 * @author Ivan Senic
 * 
 * @param <R>
 *            type of remote object
 * @param <T>
 *            result type
 */
public abstract class FailFastRemoteMethodCall<R, T> extends AbstractRemoteMethodCall<R, T> {

	/**
	 * Default constructor.
	 * 
	 * @param remoteObject
	 *            Object to make call on.
	 */
	public FailFastRemoteMethodCall(R remoteObject) {
		super(remoteObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RetryStrategy getRetryStrategy() {
		return new FailFastRetryStrategy();
	}
}
