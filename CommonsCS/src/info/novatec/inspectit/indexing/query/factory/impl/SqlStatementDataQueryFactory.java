package info.novatec.inspectit.indexing.query.factory.impl;

import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.AbstractQueryFactory;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Factory for all queries for the {@link SqlStatementData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
@Component
public class SqlStatementDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Returns the query for aggregating the {@link SqlStatementData}. If the template holds the SQL
	 * query string, only objects with this query string will be returned.
	 * 
	 * @param sqlStatementData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return Query object.
	 */
	public E getAggregatedSqlStatementsQuery(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(sqlStatementData.getPlatformIdent());
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(SqlStatementData.class);
		searchedClasses.add(AggregatedSqlStatementData.class);
		query.setObjectClasses(searchedClasses);
		if (null != sqlStatementData.getSql()) {
			query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("sql", sqlStatementData.getSql()));
		}
		if (null != sqlStatementData.getDatabaseProductName()) {
			query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("databaseProductName", sqlStatementData.getDatabaseProductName()));
		}
		if (null != sqlStatementData.getDatabaseProductVersion()) {
			query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("databaseProductVersion", sqlStatementData.getDatabaseProductVersion()));
		}
		if (null != sqlStatementData.getDatabaseUrl()) {
			query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("databaseUrl", sqlStatementData.getDatabaseUrl()));
		}
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}

}
