package rocks.inspectit.server.dao.impl;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.SpanQueryFactory;

/**
 * @author Ivan Senic
 *
 */
@Component
public class BufferSpanDaoImpl extends AbstractBufferDataDao<AbstractSpan> {

	/**
	 * Index query provider.
	 */
	@Autowired
	private SpanQueryFactory<IIndexQuery> spanQueryFactory;

	/**
	 * Returns root spans for given time-frame.
	 *
	 * @param limit
	 *            number of results returned by service. Value <code>-1</code> means no limit.
	 * @param fromDate
	 *            From date to search for. Can be <code>null</code> not to set any boundary.
	 * @param toDate
	 *            To date to search for. Can be <code>null</code> not to set any boundary.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case default time-stamp sorting will be done.
	 * @return All root spans.
	 */
	public Collection<AbstractSpan> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator) {
		IIndexQuery query = spanQueryFactory.getRootSpansQuery(fromDate, toDate);
		if (null != resultComparator) {
			return super.executeQuery(query, resultComparator, limit, false);
		} else {
			return super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
		}
	}

	/**
	 * Returns spans that belong to the given trace.
	 *
	 * @param traceId
	 *            Trace id.
	 * @return All spans for the trace.
	 */
	public Collection<AbstractSpan> getSpans(long traceId) {
		IIndexQuery query = spanQueryFactory.getSpans(traceId);
		return getIndexingTree().query(query);
	}

}
