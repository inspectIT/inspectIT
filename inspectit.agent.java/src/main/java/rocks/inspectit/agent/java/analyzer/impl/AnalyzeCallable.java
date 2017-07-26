package rocks.inspectit.agent.java.analyzer.impl;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;

/**
 * {@link Callable} that invokes
 * {@link IConnection#analyze(long, String, rocks.inspectit.shared.all.instrumentation.classcache.Type)}
 * method.
 *
 * @author Ivan Senic
 *
 */
public class AnalyzeCallable implements Callable<InstrumentationDefinition> {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnalyzeCallable.class);

	/**
	 * Connection to use.
	 */
	private final IConnection connection;

	/**
	 * Platform ID to pass.
	 */
	private final long platformId;

	/**
	 * Hash to pass.
	 */
	private final String hash;

	/**
	 * {@link Type} to pass.
	 */
	private final Type type;

	/**
	 * Default constructor.
	 *
	 * @param connection
	 *            Connection to use.
	 * @param platformId
	 *            Platform ID to pass.
	 * @param hash
	 *            Hash to pass.
	 * @param type
	 *            {@link Type} to pass.
	 * @see IConnection#analyze(long, String,
	 *      rocks.inspectit.shared.all.instrumentation.classcache.Type)
	 */
	public AnalyzeCallable(IConnection connection, long platformId, String hash, Type type) {
		this.connection = connection;
		this.platformId = platformId;
		this.hash = hash;
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstrumentationDefinition call() throws Exception {
		try {
			if (connection.isConnected()) {
				return connection.analyze(platformId, hash, type);
			} else {
				throw new ServerUnavailableException(false);
			}
		} catch (ServerUnavailableException e) {
			if (LOG.isDebugEnabled()) {
				if (e.isServerTimeout()) {
					LOG.debug("Type could not be sent to the CMR. Server timeout.", e);
				} else {
					LOG.debug("Type could not be sent to the CMR. Server not available.", e);
				}
			} else {
				LOG.warn("Type could not be sent to the CMR due to the ServerUnavailableException." + (e.isServerTimeout() ? " (timeout)" : "(error)"));
			}
			throw e;
		} catch (BusinessException businessException) {
			LOG.warn(businessException.getMessage(), businessException);
			return null;
		}
	}

}
