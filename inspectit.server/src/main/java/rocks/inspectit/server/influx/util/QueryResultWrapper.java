package rocks.inspectit.server.influx.util;

import org.apache.commons.lang.math.NumberUtils;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

/**
 * Wrapper for easy access to the data of a {@link QueryResult}.
 *
 * @author Marius Oehler
 *
 */
public class QueryResultWrapper {

	/**
	 * The {@link QueryResult} instanced wrapped by this wrapper.
	 */
	private final QueryResult queryResult;

	/**
	 * The data series contained in the {@link #queryResult}.
	 */
	private Series currentSeries;

	/**
	 * Constructor.
	 *
	 * @param queryResult
	 *            the {@link QueryResult} to wrap
	 */
	public QueryResultWrapper(QueryResult queryResult) {
		this.queryResult = queryResult;

		init();
	}

	/**
	 * Initially checking if the {@link #queryResult} contains a {@link Series}.
	 */
	private void init() {
		if (queryResult == null) {
			return;
		}

		if (queryResult.getResults() == null) {
			return;
		}

		if (queryResult.getResults().isEmpty()) {
			return;
		}

		Result result = queryResult.getResults().get(0);
		if (result.getSeries() == null) {
			return;
		}
		if (result.getSeries().isEmpty()) {
			return;
		}

		Series series = result.getSeries().get(0);
		if (series.getValues() == null) {
			return;
		}

		currentSeries = series;
	}

	/**
	 * Returns whether the {@link #queryResult} is empty.
	 *
	 * @return Returns <code>true</code> if no data is available
	 */
	public boolean isEmpty() {
		return getRowCount() <= 0;
	}

	/**
	 * Returns the amount of rows contained in the {@link #queryResult}.
	 *
	 * @return amount of rows
	 */
	public int getRowCount() {
		if (currentSeries == null) {
			return 0;
		} else {
			return currentSeries.getValues().size();
		}
	}

	/**
	 * Returns the amount of columns contained in the {@link #queryResult}.
	 *
	 * @return amount of columns
	 */
	public int getColumnCount() {
		if (currentSeries == null) {
			return 0;
		} else {
			return currentSeries.getColumns().size();
		}
	}

	/**
	 * Returns the object located in the specified row in the specified column or <code>null</code>
	 * if the series contains no data.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param columnIndex
	 *            the column index
	 * @return the object of at the specified location
	 */
	public Object get(int rowIndex, int columnIndex) {
		if (currentSeries == null) {
			return null;
		} else if ((rowIndex < 0) || (rowIndex > getRowCount())) {
			throw new IndexOutOfBoundsException("The specified row '" + rowIndex + "' is out of bounds.");
		} else if ((columnIndex < 0) || (columnIndex > getColumnCount())) {
			throw new IndexOutOfBoundsException("The specified column '" + columnIndex + "' is out of bounds.");
		} else {
			return currentSeries.getValues().get(rowIndex).get(columnIndex);
		}
	}

	/**
	 * Returns the object located in the specified row in the specified column as a {@link String}.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param columnIndex
	 *            the column index
	 * @return the object of at the specified location as a {@link String}.
	 */
	public String getString(int rowIndex, int columnIndex) {
		return String.valueOf(get(rowIndex, columnIndex));
	}

	/**
	 * Returns the object located in the specified row in the specified column as a {@link Double}.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param columnIndex
	 *            the column index
	 * @return the object of at the specified location as a {@link Double}.
	 */
	public Double getDouble(int rowIndex, int columnIndex) {
		String value = get(rowIndex, columnIndex).toString();
		if (NumberUtils.isNumber(value)) {
			return NumberUtils.toDouble(value);
		} else {
			return Double.NaN;
		}
	}
}
