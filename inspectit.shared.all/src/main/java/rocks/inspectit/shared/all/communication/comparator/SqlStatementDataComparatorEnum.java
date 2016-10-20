package rocks.inspectit.shared.all.communication.comparator;

import java.util.Comparator;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.util.ObjectUtils;

/**
 * Comparators for {@link SqlStatementData}.
 *
 * @author Ivan Senic
 *
 */
public enum SqlStatementDataComparatorEnum implements IDataComparator<SqlStatementData>, Comparator<SqlStatementData> {

	/**
	 * Sort by if the statement is prepared or not.
	 */
	IS_PREPARED_STATEMENT,

	/**
	 * Sort by SQL string.
	 */
	SQL,

	/**
	 * Sort by parameter values.
	 */
	PARAMETERS,

	/**
	 * Sort by both SQL string and parameter values.
	 */
	SQL_AND_PARAMETERS,

	/**
	 * Sort by the name of the database.
	 */
	DATABASE_NAME,

	/**
	 * Sort by the database version.
	 */
	DATABASE_VERSION,

	/**
	 * Sort by the database url.
	 */
	DATABASE_URL;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(SqlStatementData o1, SqlStatementData o2, ICachedDataService cachedDataService) {
		return compare(o1, o2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(SqlStatementData o1, SqlStatementData o2) {
		switch (this) {
		case IS_PREPARED_STATEMENT:
			return Boolean.valueOf(o1.isPreparedStatement()).compareTo(Boolean.valueOf(o2.isPreparedStatement()));
		case SQL:
			return ObjectUtils.compare(o1.getSql(), o2.getSql());
		case DATABASE_NAME:
			return ObjectUtils.compare(o1.getDatabaseProductName(), o2.getDatabaseProductName());
		case DATABASE_VERSION:
			return ObjectUtils.compare(o1.getDatabaseProductVersion(), o2.getDatabaseProductVersion());
		case DATABASE_URL:
			return ObjectUtils.compare(o1.getDatabaseUrl(), o2.getDatabaseUrl());
		case PARAMETERS:
			List<String> parameterList1 = o1.getParameterValues();
			List<String> parameterList2 = o2.getParameterValues();
			return ObjectUtils.compare(parameterList1, parameterList2);
		case SQL_AND_PARAMETERS:
			int result = SQL.compare(o1, o2);
			if (0 != result) {
				return result;
			} else {
				return PARAMETERS.compare(o1, o2);
			}
		default:
			return 0;
		}
	}

}
