package rocks.inspectit.shared.cs.indexing.aggregation;

import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.ExceptionDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.SqlStatementDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.ExceptionDataAggregator.ExceptionAggregationType;

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
