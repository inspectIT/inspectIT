package rocks.inspectit.agent.java.analyzer.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;

/**
 * Runnable class for sending the applied instrumentation via the {@link IConnection}.
 *
 * @author Ivan Senic
 *
 */
public class InstrumentationAppliedRunnable implements Runnable {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InstrumentationAppliedRunnable.class);

	/**
	 * Connection to use.
	 */
	private final IConnection connection;

	/**
	 * Platform ID to pass.
	 */
	private final long platformId;

	/**
	 * Map containing method id as key and applied sensor IDs.
	 */
	private final Map<Long, long[]> methodToSensorMap;

	/**
	 * Default constructor.
	 *
	 * @param connection
	 *            {@link IConnection} to send mappings to.
	 * @param platformId
	 *            Platform ID to pass.
	 * @param methodToSensorMap
	 *            Map containing method id as key and applied sensor IDs.
	 */
	public InstrumentationAppliedRunnable(IConnection connection, long platformId, Map<Long, long[]> methodToSensorMap) {
		this.connection = connection;
		this.platformId = platformId;
		this.methodToSensorMap = methodToSensorMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			if (connection.isConnected()) {
				connection.instrumentationApplied(platformId, methodToSensorMap);
			}
		} catch (ServerUnavailableException e) {
			if (LOG.isDebugEnabled()) {
				if (e.isServerTimeout()) {
					LOG.debug("Instrumentations applied could not be sent to the CMR. Server timeout.", e);
				} else {
					LOG.debug("Instrumentations applied could not be sent to the CMR. Server not available.", e);
				}
			} else {
				LOG.warn("Instrumentations applied could not be sent to the CMR due to the ServerUnavailableException." + (e.isServerTimeout() ? " (timeout)" : "(error)"));
			}
		}
	}

}