package rocks.inspectit.agent.java.connection;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryonet.rmi.TimeoutException;

import rocks.inspectit.agent.java.connection.impl.AdditiveWaitRetryStrategy;

/**
 * <b>IMPORTANT:</b> The class code is copied/taken from <a
 * href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>. Original
 * author is William Grosso. License info can be found <a
 * href="http://www.oreilly.com/terms/">here</a>.
 *
 * @author William Grosso
 * @author Patrice Bouillet
 * @author Ivan Senic
 *
 * @param <R>
 *            type of the remote object
 * @param <T>
 *            type of the result returned by the call.
 */
public abstract class AbstractRemoteMethodCall<R, T> {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRemoteMethodCall.class);

	/**
	 * Remote object to make the call on.
	 */
	private final R remoteObject;

	/**
	 * Constructor that accepts remote object.
	 *
	 * @param remoteObject
	 *            Remote object
	 */
	public AbstractRemoteMethodCall(R remoteObject) {
		if (null == remoteObject) {
			throw new IllegalArgumentException("Remote object can not be null");
		}

		this.remoteObject = remoteObject;
	}

	/**
	 * Performs the actual call to the server.
	 *
	 * @return The object returned from the server (if there is one).
	 * @throws ServerUnavailableException
	 *             Throws a ServerUnavailable exception if the server isn't available anymore due to
	 *             network problems or something else.
	 * @throws ExecutionException
	 *             If checked exception was thrown as result of the remote call.
	 */
	public final T makeCall() throws ServerUnavailableException, ExecutionException {
		RetryStrategy strategy = getRetryStrategy();
		while (strategy.shouldRetry()) {
			R remoteObject = getRemoteObject();
			if (null == remoteObject) {
				throw new ServerUnavailableException();
			}
			try {
				return performRemoteCall(remoteObject);
			} catch (TimeoutException timeoutException) {
				// on timeout just inform that we hit the timeout
				throw new ServerUnavailableException(true); // NOPMD
			} catch (RuntimeException remoteException) {
				// on any other runtime exception, true to repeat as kryonet will report all errors
				// via runtime exceptions

				// first log this exception as it is important to understand
				LOG.warn("Communication with " + remoteObject + " failed, applying retry strategy.", remoteException);

				try {
					strategy.remoteExceptionOccured();
				} catch (RetryException retryException) {
					handleRetryException(remoteObject);
				}
			} catch (Exception e) {
				// on checked exception pack into execution exception and throw
				throw new ExecutionException(e);
			}
		}
		return null;
	}

	/*
	 * The next 4 methods define the core behavior. Of these, two must be implemented by the
	 * subclass (and so are left abstract). The remaining three can be altered to provide customized
	 * retry handling.
	 */

	/**
	 * getRemoteObject is a template method which by defaults returns the stub.
	 * <p>
	 * Sub-classes can override if needed.
	 *
	 * @return The Remote Stub
	 * @throws ServerUnavailableException
	 *             Throws a ServerUnavailable exception if the server isn't available anymore due to
	 *             network problems or something else.
	 */
	protected R getRemoteObject() throws ServerUnavailableException {
		return remoteObject;
	}

	/**
	 * performRemoteCall is a template method which actually makes the remote method invocation.
	 *
	 * @param remoteObject
	 *            The actual remote object.
	 * @return The {@link Object} received from the server.
	 * @throws Exception
	 *             if checked exception occurred on the remote call.
	 */
	protected abstract T performRemoteCall(R remoteObject) throws Exception;

	/**
	 * Returns the selected retry strategy.
	 *
	 * @return The retry strategy.
	 */
	protected RetryStrategy getRetryStrategy() {
		return new AdditiveWaitRetryStrategy();
	}

	/**
	 * This method is executed if some calls to the server weren't successful.
	 *
	 * @param remoteObject
	 *            The remote object.
	 * @throws ServerUnavailableException
	 *             The exception {@link ServerUnavailableException} is always thrown when this
	 *             method is entered.
	 */
	protected final void handleRetryException(final R remoteObject) throws ServerUnavailableException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Repeated attempts to communicate with " + remoteObject + " failed.");
		}
		throw new ServerUnavailableException();
	}

}
