package rocks.inspectit.server.influx.dao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.externalservice.IExternalService;
import rocks.inspectit.server.influx.InfluxAvailabilityChecker;
import rocks.inspectit.server.influx.InfluxAvailabilityChecker.InfluxAvailabilityListener;
import rocks.inspectit.server.influx.util.InfluxClientFactory;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;
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
public class InfluxDBDao implements InfluxAvailabilityListener, IExternalService {

	/**
	 * After this duration, the batch have to be flushed.
	 */
	static final int BATCH_FLUSH_TIMER = 10;

	/**
	 * Size of the {@link Point} buffer.
	 */
	static final int BATCH_BUFFER_SIZE = 2000;

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Indicates whether the service is connected to a influxDB instance.
	 */
	private volatile boolean connected = false;

	/**
	 * Activation state of this service.
	 */
	@Value("${influxdb.active}")
	boolean active;

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
	private InfluxDB influxDB;

	/**
	 * {@link ExecutorService} instance.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	/**
	 * Factory used to create {@link InfluxDB} clients.
	 */
	@Autowired
	private InfluxClientFactory influxClientFactory;

	/**
	 * The task which connects the InfluxDB.
	 */
	private final ConnectingTask connectingTask = new ConnectingTask();

	/**
	 * {@link Future} representing the state of {@link #connectingTask}.
	 */
	private Future<?> connectingFuture;

	/**
	 * Component to monitor the availability of the influxDB.
	 */
	@Autowired
	private InfluxAvailabilityChecker availabilityChecker;

	/**
	 * Inserts the given {@link Point} into the database.
	 *
	 * @param dataPoint
	 *            {@link Point} to insert
	 */
	public void insert(Point dataPoint) {
		if ((dataPoint == null) || !isConnected()) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Write data to InfluxDB: {}", dataPoint.toString());
		}

		influxDB.write(database, retentionPolicy, dataPoint);
	}

	/**
	 * Executes the given query on the database.
	 *
	 * @param query
	 *            the query to execute
	 * @return the result of this query
	 */
	public QueryResult query(String query) {
		if ((query == null) || !isConnected()) {
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("Execute query on InfluxDB: {}", query);
		}

		return influxDB.query(new Query(query, database));
	}

	/**
	 * Indicates whether the influxDB service is connected to a running influxDB instance.
	 *
	 * @return true, if connected, otherwise false
	 */
	public boolean isConnected() {
		return getServiceStatus() == ExternalServiceStatus.CONNECTED;
	}

	/**
	 * Connects to the InfluxDB if the feature has been enabled.
	 */
	@PostConstruct
	@PropertyUpdate(properties = { "influxdb.host", "influxdb.port", "influxdb.user", "influxdb.passwd", "influxdb.database", "influxdb.active" })
	public void propertiesUpdated() {
		reset();

		if (active) {
			connectingFuture = scheduledExecutorService.submit(connectingTask);
		}
	}

	/**
	 * Resets the service.
	 */
	private void reset() {
		disableBatching();

		if ((connectingFuture != null) && !connectingFuture.isDone()) {
			connectingFuture.cancel(true);
		}

		availabilityChecker.deactivate();

		connected = false;
	}

	/**
	 * Connects to the influxDB instance as specified by the configuration attributes.
	 */
	private void connect() {
		try {
			influxDB = influxClientFactory.createClient();
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("InfluxDB client could not be created. Please check you configuration settings.", e);
			}
			return;
		}

		if (influxDB == null) {
			if (log.isErrorEnabled()) {
				log.error("InfluxDB client is null. Please check your configuration settings and try again.");
			}
		} else {
			enableBatching();

			connected = isAvailable();

			if (connected) {
				if (log.isInfoEnabled()) {
					log.info("|-InfluxDB Service active and connected...");
				}

				createDatabaseIfNotExistent();
			} else {
				if (log.isWarnEnabled()) {
					log.warn("|-InfluxDB Service was not able to connect! Check connection settings!");
				}
			}

			activateAvailabilityChecker();
		}
	}

	/**
	 * Checks if the remote influxDB instance is available.
	 *
	 * @return Returns true if the influxDB is available.
	 */
	private boolean isAvailable() {
		try {
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
		List<String> dbNames = influxDB.describeDatabases();
		if (!dbNames.contains(database)) {
			influxDB.createDatabase(database);
		}
	}

	/**
	 * Starts periodic availability checks.
	 */
	private void activateAvailabilityChecker() {
		availabilityChecker.setInflux(influxDB);
		availabilityChecker.activate();
	}

	/**
	 * Enables batching of the current {@link #influxDB} client.
	 */
	private void enableBatching() {
		if ((null != influxDB) && !influxDB.isBatchEnabled()) {
			influxDB.enableBatch(BATCH_BUFFER_SIZE, BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
		}
	}

	/**
	 * Disables batching of the current {@link #influxDB} client.
	 */
	private void disableBatching() {
		if ((null != influxDB) && influxDB.isBatchEnabled()) {
			influxDB.disableBatch();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisconnection() {
		if (log.isWarnEnabled()) {
			log.warn("|-InfluxDB Service not available anymore!");
		}

		// Batching will be disabled to prevent exceptions during sending of the buffered data
		disableBatching();

		connected = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReconnection() {
		if (log.isInfoEnabled()) {
			log.info("|-InfluxDB Service recovered!");
		}

		enableBatching();
		createDatabaseIfNotExistent();

		connected = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExternalServiceStatus getServiceStatus() {
		if (!active) {
			return ExternalServiceStatus.DISABLED;
		}

		if (connected) {
			return ExternalServiceStatus.CONNECTED;
		} else {
			return ExternalServiceStatus.DISCONNECTED;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExternalServiceType getServiceType() {
		return ExternalServiceType.INFLUXDB;
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
			connect();
		}
	}
}
