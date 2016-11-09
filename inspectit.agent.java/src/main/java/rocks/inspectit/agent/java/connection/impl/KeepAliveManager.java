package rocks.inspectit.agent.java.connection.impl;

import java.net.ConnectException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class handles the sending of keep-alive signals to the CMR.
 *
 * @author Marius Oehler
 *
 */
@Component
public class KeepAliveManager implements InitializingBean, DisposableBean {

	/**
	 * We will always try to reconnect at least in 5 min periods.
	 */
	private static final int MAX_NEXT_RECONNECT = (int) (TimeUnit.MINUTES.toMillis(5) / IKeepAliveService.KA_PERIOD);

	/**
	 * Runnable which sends the keep-alive signal.
	 */
	private final Runnable keepAliveRunner = new Runnable() {

		@Override
		public void run() {
			sendKeepAlive();
		}

	};

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Connection to send the keep-alive signals.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Core-service executor service.
	 */
	@Autowired
	@Qualifier("coreServiceExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * ScheduledFuture representing pending keep-alive sending task.
	 */
	private ScheduledFuture<?> scheduledTask;

	/**
	 * Amount of consecutive times we were not able to send keep-alive due to the connection not
	 * being connected.
	 */
	private int noConnectionCount = 0;

	/**
	 * Defines {@link #noConnectionCount} when we should try to reconnect next.
	 */
	private int nextReconnectAt = 2;

	/**
	 * Sends keep alive signal if connection to the server exists. Otherwise does try to reconnect
	 * in the exponential waiting manner.
	 */
	public void sendKeepAlive() {
		try {
			if (connection.isConnected()) {
				connection.sendKeepAlive(platformManager.getPlatformId());
				resetReconnectCount();
			} else {
				noConnectionCount++;
				if (shouldReconnect()) {
					log.info("Trying to reconnect to the server.");
					try {
						connection.reconnect();
					} catch (ConnectException e) {
						increaseNextReconnectAt();
						if (log.isDebugEnabled()) {
							log.debug("Reconnection failed, next attempt in " + TimeUnit.MILLISECONDS.toSeconds(IKeepAliveService.KA_PERIOD * (nextReconnectAt - noConnectionCount)) + " seconds.");
						}
					}
				}
			}
		} catch (IdNotAvailableException e) {
			if (log.isDebugEnabled()) {
				log.debug("Keep-alive signal could not be sent. No platform id available.", e);
			}
		} catch (ServerUnavailableException e) {
			if (log.isDebugEnabled()) {
				if (e.isServerTimeout()) {
					log.debug("Keep-alive signal could not be sent. Server timeout.", e);
				} else {
					log.debug("Keep-alive signal could not be sent. Server not available.", e);
				}
			}
		}
	}

	/**
	 * If we should execute reconnect.
	 *
	 * @return If we should execute reconnect.
	 */
	private boolean shouldReconnect() {
		return noConnectionCount >= nextReconnectAt;
	}

	/**
	 * Resets the {@link #noConnectionCount} and {@link #nextReconnectAt}.
	 */
	private void resetReconnectCount() {
		noConnectionCount = 0;
		nextReconnectAt = 2;
	}

	/**
	 * Exponentially increase the next reconnect count until reaching {@link #MAX_NEXT_RECONNECT}.
	 */
	private void increaseNextReconnectAt() {
		nextReconnectAt = Math.min(MAX_NEXT_RECONNECT, nextReconnectAt << 1);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Starts the sending of the keep-alive signals.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (scheduledTask == null) {
			scheduledTask = executorService.scheduleAtFixedRate(keepAliveRunner, IKeepAliveService.KA_INITIAL_DELAY, IKeepAliveService.KA_PERIOD, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Stops the further sending of keep-alive messages.
	 */
	@Override
	public void destroy() throws Exception {
		scheduledTask.cancel(false);
	}

}
