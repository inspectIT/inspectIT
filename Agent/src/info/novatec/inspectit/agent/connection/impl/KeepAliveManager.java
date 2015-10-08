package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.cmr.service.IKeepAliveService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the sending of keep-alive signals to the CMR.
 * 
 * @author Marius Oehler
 *
 */
public class KeepAliveManager implements Runnable {

	/**
	 * Thread pool to provide a scheduled thread to send keep alive messages.
	 */
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	/**
	 * Connection to send the keep-alive signals.
	 */
	private IConnection connection;

	/**
	 * ScheduledFuture representing pending keep-alive sending task.
	 */
	private ScheduledFuture<?> scheduledTask;

	/**
	 * Constructor.
	 * 
	 * @param connection
	 *            The connection that is used to send the keep-alive signals.
	 */
	public KeepAliveManager(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * Starts the sending of the keep-alive signals.
	 */
	public void start() {
		if (scheduledTask == null) {
			scheduledTask = scheduledThreadPool.scheduleAtFixedRate(this, IKeepAliveService.KA_INITIAL_DELAY, IKeepAliveService.KA_PERIOD, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		connection.sendKeepAlive();
	}

	/**
	 * Stops the further sending of keep-alive messages.
	 */
	public void stop() {
		scheduledTask.cancel(false);
	}
}
