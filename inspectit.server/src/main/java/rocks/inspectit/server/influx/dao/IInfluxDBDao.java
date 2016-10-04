package rocks.inspectit.server.influx.dao;

import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;

/**
 * DAO interface for writing data to a influx database.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
public interface IInfluxDBDao {

	/**
	 * Executes the given query on the database.
	 *
	 * @param query
	 *            the query to execute
	 * @return the result of this query
	 */
	QueryResult query(String query);

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
