package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.ExceptionSensorDataDao;
import info.novatec.inspectit.communication.comparator.DefaultDataComparatorEnum;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.indexing.AbstractBranch;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.Aggregators;
import info.novatec.inspectit.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {@link ExceptionSensorDataDao} that works with the data from the buffer.
 * <br> The query-Method of {@link AbstractBranch} which uses fork&join is executed, because much exception-data is expected and 
 * querying with fork&join will be faster<br>
 * <br> in getExceptionTree(ExceptionSensorData template) the query-Method without fork&join is used, because only one exception tree per invocation will be queried. <br>
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferExceptionSensorDataDaoImpl extends AbstractBufferDataDao<ExceptionSensorData> implements ExceptionSensorDataDao {

	/**
	 * Index query provider.
	 */
	@Autowired
	private ExceptionSensorDataQueryFactory<IIndexQuery> exceptionSensorDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Comparator<? super ExceptionSensorData> comparator) {
		return this.getUngroupedExceptionOverview(template, limit, null, null, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate, Comparator<? super ExceptionSensorData> comparator) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getUngroupedExceptionOverviewQuery(template, limit, fromDate, toDate);
		if (null != comparator) {
			return super.executeQuery(query, comparator, limit, true);
		} else {
			return super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Comparator<? super ExceptionSensorData> comparator) {
		return this.getUngroupedExceptionOverview(template, -1, null, null, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate, Comparator<? super ExceptionSensorData> comparator) {
		return this.getUngroupedExceptionOverview(template, -1, fromDate, toDate, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getExceptionTreeQuery(template);
		List<ExceptionSensorData> results = super.executeQuery(query, false);
		Collections.reverse(results);
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		return this.getDataForGroupedExceptionOverview(template, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getDataForGroupedExceptionOverviewQuery(template, fromDate, toDate);
		List<ExceptionSensorData> results = super.executeQuery(query, Aggregators.GROUP_EXCEPTION_OVERVIEW_AGGREGATOR, true);
		List<AggregatedExceptionSensorData> aggResults = new ArrayList<AggregatedExceptionSensorData>();
		for (ExceptionSensorData exData : results) {
			if (exData instanceof AggregatedExceptionSensorData) {
				aggResults.add((AggregatedExceptionSensorData) exData);
			}
		}
		return aggResults;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		IIndexQuery query = exceptionSensorDataQueryFactory.getStackTraceMessagesForThrowableTypeQuery(template);
		return super.executeQuery(query, Aggregators.DISTINCT_STACK_TRACES_AGGREGATOR, true);
	}

}
