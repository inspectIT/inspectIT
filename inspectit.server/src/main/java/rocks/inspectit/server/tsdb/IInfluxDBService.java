package rocks.inspectit.server.tsdb;

import org.influxdb.dto.Point;

/**
 * Service Interface for writing data to a influx database.
 *
 * @author Alexander Wert
 *
 */
public interface IInfluxDBService {
	/**
	 * Inserts the given {@link Point} into the database.
	 *
	 * @param dataPoint
	 *            {@link Point} to insert
	 */
	void insert(Point dataPoint);

	/**
	 * Indicates whether the influxDB service is connected to a running influxDB instance.
	 *
	 * @return true, if connected, otherwise false
	 */
	boolean isOnline();
}
