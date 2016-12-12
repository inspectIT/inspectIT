package rocks.inspectit.shared.cs.indexing.query.factory.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.indexing.query.factory.AbstractQueryFactory;
import rocks.inspectit.shared.cs.indexing.restriction.impl.IndexQueryRestrictionFactory;

/**
 * Query factory for the spans.
 *
 * @param <E>
 *            Query type.
 * @author Ivan Senic
 *
 */
@Component
public class SpanQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * List of span classes used in the queries.
	 */
	private static final List<Class<?>> CLASSES_LIST = Collections.unmodifiableList(Arrays.<Class<?>> asList(ServerSpan.class, ClientSpan.class));

	/**
	 * Returns query for searching root spans.
	 *
	 * @param fromDate
	 *            From date to search for. Can be <code>null</code> not to set any boundary.
	 * @param toDate
	 *            To date to search for. Can be <code>null</code> not to set any boundary.
	 * @return Query
	 */
	public E getRootSpansQuery(Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		// set classes searched
		query.setObjectClasses(CLASSES_LIST);

		// only root spans
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("spanIdent.root", true));

		// set time bounds
		if (fromDate != null) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (toDate != null) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}

	/**
	 * Returns query for searching spans that belong to the same trace.
	 *
	 * @param traceId
	 *            Trace id.
	 * @return Query
	 */
	public E getSpans(long traceId) {
		E query = getIndexQueryProvider().getIndexQuery();
		// set classes searched
		query.setObjectClasses(CLASSES_LIST);

		// only spans in the trace spans
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("spanIdent.traceId", traceId));
		return query;
	}

	/**
	 * Returns query for getting span with given ident.
	 *
	 * @param spanIdent
	 *            {@link SpanIdent}.
	 * @return Query
	 */
	public E get(SpanIdent spanIdent) {
		E query = getIndexQueryProvider().getIndexQuery();
		// set classes searched
		query.setObjectClasses(CLASSES_LIST);

		// only span with specific id
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("spanIdent.id", spanIdent.getId()));
		return query;
	}

}
