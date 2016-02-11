package info.novatec.inspectit.indexing.aggregation;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator.ExceptionAggregationType;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;

/**
 * This class provides commonly used aggregators.
 * 
 * @author Ivan Senic
 * 
 */
public final class Aggregators {

	/**
	 * {@link IAggregator} for {@link ExceptionSensorData} for the grouped overview.
	 */
	public static final ExceptionDataAggregator GROUP_EXCEPTION_OVERVIEW_AGGREGATOR = new ExceptionDataAggregator(ExceptionAggregationType.GROUP_EXCEPTION_OVERVIEW);

	/**
	 * {@link IAggregator} for {@link ExceptionSensorData} for the distinct stack traces.
	 */
	public static final ExceptionDataAggregator DISTINCT_STACK_TRACES_AGGREGATOR = new ExceptionDataAggregator(ExceptionAggregationType.DISTINCT_STACK_TRACES);

	/**
	 * {@link IAggregator} used for {@link TimerData}.
	 */
	public static final TimerDataAggregator TIMER_DATA_AGGREGATOR = new TimerDataAggregator();

	/**
	 * {@link IAggregator} used for {@link SqlStatementData} general aggregation.
	 */
	public static final SqlStatementDataAggregator SQL_STATEMENT_DATA_AGGREGATOR = new SqlStatementDataAggregator();

	/**
	 * {@link IAggregator} used for {@link SqlStatementData} when parameters are included in
	 * aggregation.
	 */
	public static final SqlStatementDataAggregator SQL_STATEMENT_DATA_PARAMETER_AGGREGATOR = new SqlStatementDataAggregator(true);

	/**
	 * Private constructor.
	 */
	private Aggregators() {
	}
}
