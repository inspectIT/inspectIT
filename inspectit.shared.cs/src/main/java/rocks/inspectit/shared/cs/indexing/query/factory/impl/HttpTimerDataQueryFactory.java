package rocks.inspectit.shared.cs.indexing.query.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.query.factory.AbstractQueryFactory;
import rocks.inspectit.shared.cs.indexing.restriction.impl.IndexQueryRestrictionFactory;

/**
 * Factory for all queries for the {@link HttpTimerData}.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
@Component
public class HttpTimerDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Return query for all <code>HttpTimerData</code> objects.
	 *
	 * @param httpData
	 *            <code>HttpTimerData</code> object used to retrieve the platformId
	 * @param fromDate
	 *            the fromDate or <code>null</code> if not applicable
	 * @param toDate
	 *            the toDate or <code>null</code> if not applicable
	 * @return Query for all <code>HttpTimerData</code> objects in the buffer.
	 */
	public E getFindAllHttpTimersQuery(HttpTimerData httpData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(httpData.getPlatformIdent());
		ArrayList<Class<?>> classesToSearch = new ArrayList<>();
		classesToSearch.add(HttpTimerData.class);
		classesToSearch.add(AggregatedHttpTimerData.class);
		query.setObjectClasses(classesToSearch);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}

	/**
	 * Return query for all <code>HttpTimerData</code> objects that have a inspectIT tag header
	 * value.
	 *
	 * @param httpData
	 *            <code>HttpTimerData</code> object used to retrieve the platformId
	 * @param fromDate
	 *            the fromDate or <code>null</code> if not applicable
	 * @param toDate
	 *            the toDate or <code>null</code> if not applicable
	 * @return Query for all <code>HttpTimerData</code> objects in the buffer.
	 */
	public E getFindAllTaggedHttpTimersQuery(HttpTimerData httpData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(httpData.getPlatformIdent());
		ArrayList<Class<?>> classesToSearch = new ArrayList<>();
		classesToSearch.add(HttpTimerData.class);
		classesToSearch.add(AggregatedHttpTimerData.class);
		query.setObjectClasses(classesToSearch);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isNotNull("httpInfo.inspectItTaggingHeaderValue"));
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}
}
