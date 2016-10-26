package rocks.inspectit.server.influx.dao.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.dao.IInfluxDBDao;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.ExecutorServiceUtils;

/**
 * This DAO encapsulates the HTTP connection to a influx database.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
@Component
public class InfluxDBDao implements IInfluxDBDao {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * After this duration, the batch have to be flushed.
	 */
	private static final int BATCH_FLUSH_TIMER = 10;

	/**
	 * Size of the {@link Point} buffer.
	 */
	private static final int BATCH_BUFFER_SIZE = 2000;

	/**
	 * Indicates whether the service is connected to a influxDB instance.
	 */
	volatile boolean isConnected = false;

	/**
	 * Activation state of this service.
	 */
	@Value("${influxdb.active}")
	private boolean active;

	/**
	 * Host where InfluxDB is running.
	 */
	@Value("${influxdb.host}")
	private String host;

	/**
	 * Port of the running InfluxDB.
	 */
	@Value("${influxdb.port}")
	private int port;

	/**
	 * InfluxDB user.
	 */
	@Value("${influxdb.user}")
	private String user;

	/**
	 * Password of the InfluxDB user.
	 */
	@Value("${influxdb.passwd}")
	private String password;

	/**
	 * Database to use.
	 */
	@Value("${influxdb.database}")
	String database;

	/**
	 * The retention policy to use.
	 */
	@Value("${influxdb.retentionPolicy}")
	String retentionPolicy;

	/**
	 * Configured {@link InfluxDB} instance.
	 */
	InfluxDB influxDB;

	/**
	 * {@link ScheduledFuture} instance representing the task that checks periodically for
	 * availability of the influxDB.
	 */
	ScheduledFuture<?> availabilityCheckTask;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	/**
	 * The task which connects the InfluxDB.
	 */
	final ConnectingTask connectingTask = new ConnectingTask();

	/**
	 * {@link Future} representing the state of {@link #connectingTask}.
	 */
	Future<?> connectingFuture;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert(Point dataPoint) {
		if ((dataPoint == null) || (influxDB == null)) {
			return;
		}

		if (isConnected) {
			if (log.isDebugEnabled()) {
				log.debug("Write data to InfluxDB: {}", dataPoint.toString());
			}

			influxDB.write(database, retentionPolicy, dataPoint);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryResult query(String query) {
		if (!isConnected || (query == null) || (influxDB == null)) {
			return null;
		}

		return influxDB.query(new Query(query, database));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOnline() {
		return isConnected && active;
	}

	/**
	 * Connects to the InfluxDB if the feature has been enabled.
	 */
	@PostConstruct
	@PropertyUpdate(properties = { "influxdb.host", "influxdb.port", "influxdb.user", "influxdb.passwd", "influxdb.database", "influxdb.active" })
	public void propertiesUpdated() {
		reset();

		if ((connectingFuture != null) && !connectingFuture.isDone()) {
			connectingFuture.cancel(true);
		}

		connectingFuture = scheduledExecutorService.submit(connectingTask);
	}

	/**
	 * Resets the service.
	 */
	private void reset() {
		isConnected = false;
		if ((null != influxDB) && influxDB.isBatchEnabled()) {
			influxDB.disableBatch();
		}
		if ((null != availabilityCheckTask) && !availabilityCheckTask.isDone()) {
			availabilityCheckTask.cancel(false);
		}
	}

	/**
	 * Starts periodic availability checks.
	 */
	private void startAvailabilityChecks() {
		long schedulerDelay = BATCH_FLUSH_TIMER / 2;
		availabilityCheckTask = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				boolean connected = ping();

				if (!isConnected && connected) { // changed from disconnected to connected
					if (!influxDB.isBatchEnabled()) {
						influxDB.enableBatch(BATCH_BUFFER_SIZE, BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
					}
					if (log.isInfoEnabled()) {
						log.info("|-InfluxDB Service recovered!");
					}

				} else if (isConnected && !connected) { // changed from connected to disconnected
					if (influxDB.isBatchEnabled()) {
						influxDB.disableBatch();
					}
					if (log.isWarnEnabled()) {
						log.warn("|-InfluxDB Service not available anymore!");
					}

				}
				isConnected = connected;
			}
		}, schedulerDelay, schedulerDelay, TimeUnit.SECONDS);
	}

	/**
	 * Connects to the influxDB instance as specified by the configuration attributes.
	 *
	 * @return Returns true if connection has been established.
	 */
	private boolean connect() {
		influxDB = InfluxDBFactory.connect("http://" + host + ":" + port, user, password);
		influxDB.enableBatch(BATCH_BUFFER_SIZE, BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
		boolean connected = ping();
		if (connected && log.isInfoEnabled()) {
			log.info("|-InfluxDB Service active and connected...");
		} else if (!connected && log.isWarnEnabled()) {
			log.warn("|-InfluxDB Service was not able to connect! Check connection settings!");
		}
		return connected;
	}

	/**
	 * Checks if the remote influxDB instance is still available.
	 *
	 * @return Returns true if the influxDB is available.
	 */
	private boolean ping() {
		try {
			if (null == influxDB) {
				return false;
			}
			influxDB.ping();
			return true;
		} catch (Exception e) {
			if (log.isTraceEnabled()) {
				log.trace("Ping to the influxDB failed.", e);
			}
			return false;
		}
	}

	/**
	 * Creates the configured database in influx if it does not exist yet.
	 */
	private void createDatabaseIfNotExistent() {
		if (isOnline()) {
			List<String> dbNames = influxDB.describeDatabases();
			if (!dbNames.contains(database)) {
				influxDB.createDatabase(database);
			}
		}
	}

	/**
	 * Shutting down the used executor service.
	 */
	@PreDestroy
	protected void shutDownExecutorService() {
		ExecutorServiceUtils.shutdownExecutor(scheduledExecutorService, 5L, TimeUnit.SECONDS);
	}

	/**
	 * Executes the connection process to the InfluxDB.
	 *
	 * @author Marius Oehler
	 *
	 */
	private class ConnectingTask implements Runnable {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (active) {
				isConnected = connect();
				if (isConnected) {
					createDatabaseIfNotExistent();
				}
				if ((null == availabilityCheckTask) || availabilityCheckTask.isDone()) {
					startAvailabilityChecks();
				}
			}
		}

	}
}
