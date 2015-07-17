package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.Aggregators;
import info.novatec.inspectit.indexing.query.factory.impl.TimerDataQueryFactory;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link TimerData} that searches for timer data in buffer.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferTimerDataDaoImpl extends AbstractBufferDataDao<TimerData> implements TimerDataDao {

	/**
	 * Index query factory.
	 */
	@Autowired
	private TimerDataQueryFactory<IIndexQuery> timerDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		return this.getAggregatedTimerData(timerData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		IIndexQuery query = timerDataQueryFactory.getAggregatedTimerDataQuery(timerData, fromDate, toDate);
		return super.executeQuery(query, Aggregators.TIMER_DATA_AGGREGATOR);
	}
}
