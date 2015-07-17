package info.novatec.inspectit.indexing.query.factory.impl;

import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.AbstractQueryFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Factory for all queries for the {@link TimerData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
@Component
public class TimerDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Returns the query for aggregating the {@link TimerData}.
	 * 
	 * @param timerData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return Query object.
	 */
	public E getAggregatedTimerDataQuery(TimerData timerData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(timerData.getPlatformIdent());
		query.setMethodIdent(timerData.getMethodIdent());
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		// we need to add the subclasses that are timers manually as the search will not include
		// subclasses by default
		// HttpTimerData will not be shown in the timer data view (we also do not show SQL data)
		searchedClasses.add(TimerData.class);
		searchedClasses.add(AggregatedTimerData.class);
		query.setObjectClasses(searchedClasses);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}
}
