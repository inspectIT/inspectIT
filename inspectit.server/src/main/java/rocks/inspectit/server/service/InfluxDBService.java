package rocks.inspectit.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IInfluxDBService;

/**
 * Service to query details from the CMR's InfluxDB.
 *
 * @author Marius Oehler
 *
 */
@Service
public class InfluxDBService implements IInfluxDBService {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link IInfluxDBDao} to read from.
	 */
	@Autowired
	InfluxDBDao influxDbDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMeasurements() {
		QueryResult queryResult = influxDbDao.query("SHOW MEASUREMENTS;");
		return extractStringList(queryResult, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTags(String measurement) {
		String query = "SHOW TAG KEYS FROM \"" + measurement + "\";";
		QueryResult queryResult = influxDbDao.query(query);
		return extractStringList(queryResult, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getTagValues(String measurement, String tagKey) {
		String query = "SHOW TAG VALUES FROM \"" + measurement + "\" WITH KEY = \"" + tagKey + "\";";
		QueryResult queryResult = influxDbDao.query(query);
		return extractStringList(queryResult, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getFields(String measurement) {
		String query = "SHOW FIELD KEYS FROM \"" + measurement + "\";";
		try {
			QueryResult queryResult = influxDbDao.query(query);
			return extractStringList(queryResult, 0);
		} catch (Exception e) {
			log.info("Exception while execution query: " + query);
			return null;
		}
	}

	/**
	 * Extracts the queried values of the given {@link QueryResult} and creates a list comprising
	 * them.
	 *
	 * @param queryResult
	 *            the {@link QueryResult} containing the data
	 * @param columnIndex
	 *            the index of the column to return
	 * @return list containing the values
	 */
	private List<String> extractStringList(QueryResult queryResult, int columnIndex) {
		if (queryResult == null) {
			return Collections.emptyList();
		}
		if (queryResult.getResults().isEmpty()) {
			return Collections.emptyList();
		}

		Result result = queryResult.getResults().get(0);
		if (result.getSeries() == null) {
			return Collections.emptyList();
		}
		if (result.getSeries().isEmpty()) {
			return Collections.emptyList();
		}

		Series series = result.getSeries().get(0);
		if (series.getValues() == null) {
			return Collections.emptyList();
		}

		List<String> list = new ArrayList<>();

		for (List<Object> valueList : series.getValues()) {
			list.add((String) valueList.get(columnIndex));
		}

		return list;
	}
}
