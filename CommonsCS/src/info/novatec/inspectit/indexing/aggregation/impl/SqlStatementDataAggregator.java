package info.novatec.inspectit.indexing.aggregation.impl;

import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * {@link IAggregator} for {@link SqlStatementData}.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlStatementDataAggregator implements IAggregator<SqlStatementData>, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -2226935151962665996L;

	/**
	 * Should the parameters be included in aggregation.
	 */
	private boolean includeParameters;

	/**
	 * No-arg constructor.
	 */
	public SqlStatementDataAggregator() {
	}

	/**
	 * Secondary constructor. Allows to define if parameters should be included in the aggregation.
	 * 
	 * @param includeParameters
	 *            Should the parameters be included in aggregation.
	 */
	public SqlStatementDataAggregator(boolean includeParameters) {
		this.includeParameters = includeParameters;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(IAggregatedData<SqlStatementData> aggregatedObject, SqlStatementData objectToAdd) {
		aggregatedObject.aggregate(objectToAdd);
	}

	/**
	 * {@inheritDoc}
	 */
	public IAggregatedData<SqlStatementData> getClone(SqlStatementData sqlStatementData) {
		AggregatedSqlStatementData clone = new AggregatedSqlStatementData();
		clone.setPlatformIdent(sqlStatementData.getPlatformIdent());
		clone.setSensorTypeIdent(sqlStatementData.getSensorTypeIdent());
		clone.setPreparedStatement(sqlStatementData.isPreparedStatement());
		clone.setSql(sqlStatementData.getSql());
		clone.setDatabaseProductName(sqlStatementData.getDatabaseProductName());
		clone.setDatabaseProductVersion(sqlStatementData.getDatabaseProductVersion());
		clone.setDatabaseUrl(sqlStatementData.getDatabaseUrl());
		if (includeParameters && null != sqlStatementData.getParameterValues()) {
			clone.setParameterValues(new ArrayList<String>(sqlStatementData.getParameterValues()));
		}
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAggregationKey(SqlStatementData sqlStatementData) {
		final int prime = 31;
		int result = 0;
		result = prime * result + (sqlStatementData.isPreparedStatement() ? 1231 : 1237);
		result = prime * result + ((sqlStatementData.getSql() == null) ? 0 : sqlStatementData.getSql().hashCode());
		result = prime * result + ((sqlStatementData.getDatabaseUrl() == null) ? 0 : sqlStatementData.getDatabaseUrl().hashCode());
		if (includeParameters) {
			result = prime * result + ((sqlStatementData.getParameterValues() == null) ? 0 : sqlStatementData.getParameterValues().hashCode());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includeParameters ? 1231 : 1237);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SqlStatementDataAggregator other = (SqlStatementDataAggregator) obj;
		if (includeParameters != other.includeParameters) {
			return false;
		}
		return true;
	}

}
