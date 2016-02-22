package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.cs.cmr.service.ISqlDataAccessService;
import rocks.inspectit.shared.cs.indexing.aggregation.Aggregators;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.SqlStatementDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * {@link ISqlDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageSqlDataAccessService extends AbstractStorageService<SqlStatementData> implements ISqlDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<SqlStatementData> indexingTree;

	/**
	 * Index query provider.
	 */
	private SqlStatementDataQueryFactory<StorageIndexQuery> sqlDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		return this.getAggregatedSqlStatements(sqlStatementData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		StorageIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		return super.executeQuery(query, Aggregators.SQL_STATEMENT_DATA_AGGREGATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		return this.getParameterAggregatedSqlStatements(sqlStatementData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		StorageIndexQuery query = sqlDataQueryFactory.getAggregatedSqlStatementsQuery(sqlStatementData, fromDate, toDate);
		query.setSql(sqlStatementData.getSql());
		return super.executeQuery(query, Aggregators.SQL_STATEMENT_DATA_PARAMETER_AGGREGATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<SqlStatementData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<SqlStatementData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param sqlDataQueryFactory
	 *            the sqlDataQueryFactory to set
	 */
	public void setSqlDataQueryFactory(SqlStatementDataQueryFactory<StorageIndexQuery> sqlDataQueryFactory) {
		this.sqlDataQueryFactory = sqlDataQueryFactory;
	}

}
