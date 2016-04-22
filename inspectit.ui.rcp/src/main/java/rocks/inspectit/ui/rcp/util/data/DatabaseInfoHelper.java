package rocks.inspectit.ui.rcp.util.data;

import org.apache.commons.lang.StringUtils;

import rocks.inspectit.shared.all.communication.data.SqlStatementData;

/**
 * Helper class for displaying the database info.
 *
 * @author Ivan Senic
 *
 */
public class DatabaseInfoHelper {

	/**
	 * The URL that the connection uses.
	 */
	private String databaseUrl;

	/**
	 * The name of the database product.
	 */
	private String databaseProductName;

	/**
	 * The version of the database product.
	 */
	private String databaseProductVersion;

	/**
	 * Default constructor.
	 *
	 * @param sqlStatementData
	 *            {@link SqlStatementData} to copy the database information from.
	 */
	public DatabaseInfoHelper(SqlStatementData sqlStatementData) {
		this.databaseProductName = sqlStatementData.getDatabaseProductName();
		this.databaseProductVersion = sqlStatementData.getDatabaseProductVersion();
		this.databaseUrl = sqlStatementData.getDatabaseUrl();
	}

	/**
	 * Gets {@link #databaseUrl}.
	 *
	 * @return {@link #databaseUrl}
	 */
	public String getDatabaseUrl() {
		if (StringUtils.isNotBlank(databaseUrl)) {
			return databaseUrl;
		} else {
			return "Unknown";
		}
	}

	/**
	 * @return Returns long description text or <code>null</code> if {@link #databaseProductName} is
	 *         not specified.
	 */
	public String getLongText() {
		if (StringUtils.isNotEmpty(databaseProductName)) {
			StringBuilder stringBuilder = new StringBuilder(databaseProductName);
			if (StringUtils.isNotEmpty(databaseProductVersion)) {
				stringBuilder.append(" v. ");
				stringBuilder.append(databaseProductVersion);
			}
			if (StringUtils.isNotEmpty(databaseUrl)) {
				stringBuilder.append(" (URL: ");
				stringBuilder.append(databaseUrl);
				stringBuilder.append(')');
			}
			return stringBuilder.toString();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((databaseProductName == null) ? 0 : databaseProductName.hashCode());
		result = (prime * result) + ((databaseProductVersion == null) ? 0 : databaseProductVersion.hashCode());
		result = (prime * result) + ((databaseUrl == null) ? 0 : databaseUrl.hashCode());
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
		DatabaseInfoHelper other = (DatabaseInfoHelper) obj;
		if (databaseProductName == null) {
			if (other.databaseProductName != null) {
				return false;
			}
		} else if (!databaseProductName.equals(other.databaseProductName)) {
			return false;
		}
		if (databaseProductVersion == null) {
			if (other.databaseProductVersion != null) {
				return false;
			}
		} else if (!databaseProductVersion.equals(other.databaseProductVersion)) {
			return false;
		}
		if (databaseUrl == null) {
			if (other.databaseUrl != null) {
				return false;
			}
		} else if (!databaseUrl.equals(other.databaseUrl)) {
			return false;
		}
		return true;
	}

}