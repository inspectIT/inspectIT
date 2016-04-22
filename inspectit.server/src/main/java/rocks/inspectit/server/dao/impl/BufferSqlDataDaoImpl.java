package rocks.inspectit.server.dao.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.SqlDataDao;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.AbstractBranch;
import rocks.inspectit.shared.cs.indexing.aggregation.Aggregators;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.SqlStatementDataQueryFactory;

/**
 * Implementation of the {@link SqlDataDao} that searches for the SQL statements in the indexing
 * tree. <br>
 * The query-Method of {@link AbstractBranch} which uses fork&join is executed, because much SQL-
 * data is expected and querying with fork&join will be faster.<br>
 *
 * @author Ivan Senic
 *
 */
@Repository
public class BufferSqlDataDaoImpl extends AbstractBufferDataDao<SqlStatementData> implements SqlDataDao {

	/**
	 * Index query provider.
	 */
	@Autowired
	private SqlStatementDataQueryFactory<IIndexQuery> sqlDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		return this.getAggregatedSqlStatements(sqlStatementData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		IIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		return super.executeQuery(query, Aggregators.SQL_STATEMENT_DATA_AGGREGATOR, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		return this.getParameterAggregatedSqlStatements(sqlStatementData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		IIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		return super.executeQuery(query, Aggregators.SQL_STATEMENT_DATA_PARAMETER_AGGREGATOR, true);
	}

}
