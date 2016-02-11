package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * {@link IInputDefinitionExtra} that holds the SQL statement string.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlStatementInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * SQL string.
	 */
	private String sql;

	/**
	 * Gets {@link #sql}.
	 * 
	 * @return {@link #sql}
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets {@link #sql}.
	 * 
	 * @param sql
	 *            New value for {@link #sql}
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		SqlStatementInputDefinitionExtra that = (SqlStatementInputDefinitionExtra) object;
		return Objects.equal(this.sql, that.sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("sql", sql).toString();
	}

}
