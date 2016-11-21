package rocks.inspectit.server.influx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Continuous checker component to monitor the status of an influxDB and get notified if its state
 * (available/not available) changes.
 *
 * @author Marius Oehler
 *
 */
@Component
public class InfluxAvailabilityChecker implements Runnable {

	/**
	 * The listener interface to get notified.
	 */
	public interface InfluxAvailabilityListener {
		/**
		 * Is called if the influxDB gets unavailable.
		 */
		void onDisconnection();

		/**
		 * Is called if the influxDB gets available again.
		 */
		void onReconnection();
	}

	/**
	 * The current state of the monitored influxDB.
	 */
	private enum State {
		/**
		 * The status has not been checked.
		 */
		NOT_CHECKED,

		/**
		 * The database is available.
		 */
		AVAILABLE,

		/**
		 * The database is not available.
		 */
		NOT_AVAILABLE;
	}

	/**
	 * The delay in seconds between consecutive checks.
	 */
	private static final Long[] EXECUTION_DELAYS = { 5L, 15L, 30L, 60L, 600L };

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	/**
	 * The database which states should be checked.
	 */
	private InfluxDB influx;

	/**
	 * The listener to notify about changes of the status.
	 */
	@Autowired
	private InfluxAvailabilityListener availabilityListener;

	/**
	 * The future of the next or currently running check.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * The current state of the {@link #influx}.
	 */
	private State currentState = State.NOT_CHECKED;

	/**
	 * The index of the next used execution delay.
	 */
	private int executionDelayIndex = 0;

	/**
	 * Whether this checker is active and continuously executes a check.
	 */
	private boolean active = false;

	/**
	 * Sets {@link #influx}.
	 *
	 * @param influx
	 *            New value for {@link #influx}
	 */
	public void setInflux(InfluxDB influx) {
		this.influx = influx;
	}

	/**
	 * Activates the continuously check of the {@link #influx}.
	 */
	public void activate() {
		if ((scheduledFuture == null) || scheduledFuture.isDone()) {
			active = true;
			scheduledFuture = scheduledExecutorService.schedule(this, 0L, TimeUnit.SECONDS);
		}
	}

	/**
	 * Deactivates the continuously check of the {@link #influx}.
	 */
	public void deactivate() {
		if ((scheduledFuture != null) && !scheduledFuture.isDone()) {
			active = false;
			scheduledFuture.cancel(false);
			currentState = State.NOT_CHECKED;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			State newState = isAvailable();

			if (availabilityListener != null) {
				if ((currentState == State.AVAILABLE) && (newState == State.NOT_AVAILABLE)) {
					// switched from available to not available
					availabilityListener.onDisconnection();
				} else if ((currentState == State.NOT_AVAILABLE) && (newState == State.AVAILABLE)) {
					// switched from not available to available
					availabilityListener.onReconnection();
				}
			}

			// increasing delay when database is not available
			if (currentState == State.NOT_AVAILABLE) {
				if (executionDelayIndex < (EXECUTION_DELAYS.length - 1)) {
					executionDelayIndex++;
				}
			} else {
				executionDelayIndex = 0;
			}

			currentState = newState;
		} catch (RuntimeException e) {
			// this catch ensures that this runnable is not crashing and is not related to a "not
			// available state" of the database
			if (log.isWarnEnabled()) {
				log.warn("An unexpected exception has been thrown during availability check.", e);
			}
		}

		if (active) {
			long delay = EXECUTION_DELAYS[executionDelayIndex];
			scheduledFuture = scheduledExecutorService.schedule(this, delay, TimeUnit.SECONDS);
		}
	}

	/**
	 * Checks if the remote influxDB instance is available.
	 *
	 * @return Returns {@link State#AVAILABLE} if the influxDB is available else
	 *         {@link State#NOT_AVAILABLE}.
	 */
	private State isAvailable() {
		if (influx == null) {
			return State.NOT_AVAILABLE;
		}

		try {
			influx.ping();
			return State.AVAILABLE;
		} catch (Exception e) {
			if (log.isTraceEnabled()) {
				log.trace("Ping to the influxDB failed.", e);
			}
			return State.NOT_AVAILABLE;
		}
	}
}
