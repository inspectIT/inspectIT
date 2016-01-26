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
	 * Map containing method id as key and applied sensor IDs.
	 */
	private final Map<Long, long[]> methodToSensorMap;


	/**
	 * Default constructor.
	 *
	 * @param connection
	 *            {@link IConnection} to send mappings to.
	 * @param methodToSensorMap
	 *            Map containing method id as key and applied sensor IDs.
	 */
	public InstrumentationAppliedRunnable(IConnection connection, Map<Long, long[]> methodToSensorMap) {
		this.connection = connection;
		this.methodToSensorMap = methodToSensorMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		try {
			if (connection.isConnected()) {
				connection.instrumentationApplied(methodToSensorMap);
			}
		} catch (ServerUnavailableException e) {
			if (LOG.isDebugEnabled()) {
				if (e.isServerTimeout()) {
					LOG.debug("Instrumentations applied could not be sent to the CMR. Server timeout.", e);
				} else {
					LOG.debug("Instrumentations applied could not be sent to the CMR. Server not available.", e);
				}
			} else {
				LOG.info("Instrumentations applied could not be sent to the CMR due to the ServerUnavailableException.");
			}
		}
	}

}