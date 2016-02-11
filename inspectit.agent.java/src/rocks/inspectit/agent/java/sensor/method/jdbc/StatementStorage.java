package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.util.ThreadLocalStack;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Stores the mapping between statements and objects so that these statements are later accessible.
 * 
 * @author Patrice Bouillet
 * @author Stefan Siegl
 */
@Component
public class StatementStorage {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StatementStorage.class);

	/** representation of a null value. */
	private static final String NULL_VALUE = "null";

	/**
	 * This cache keeps track of the prepared statement objects and associates these with the
	 * concrete query string and its the bound parameters. Weak keys ensure that elements will be
	 * cleared regularly. As the key is the PreparedStatement object, as soon as the application
	 * looses the reference to this object (which is usually very quickly after the invocation), the
	 * garbage collector can remove this object and thus the entry in the cache. In order to
	 * safe-guard our cache, elements will also be removed if they are not used for 20 minutes.
	 * 
	 * <b> Note that this data structure provides atomic access like a <code>ConcurrentMap</code>.
	 * </b>.
	 */
	private Cache<Object, QueryInformation> preparedStatements = CacheBuilder.newBuilder().expireAfterAccess(20 * 60, TimeUnit.SECONDS).weakKeys().build();

	/**
	 * Returns the sql thread local stack.
	 */
	private ThreadLocalStack<String> sqlThreadLocalStack = new ThreadLocalStack<String>();

	/**
	 * Adds a prepared statement to this storage for later retrieval.
	 * 
	 * @param object
	 *            The object which will be the key of the mapping.
	 */
	public void addPreparedStatement(Object object) {
		String sql = sqlThreadLocalStack.getLast();
		preparedStatements.put(object, new QueryInformation(sql));

		if (LOG.isDebugEnabled()) {
			LOG.debug("Recorded prepared sql statement: " + sql);
		}
	}

	/**
	 * Returns a stored sql string for this object.
	 * 
	 * @param object
	 *            The object which will be used to look up in the map.
	 * @return The sql string or <code>null</code> if this statement is not available within the
	 *         storage.
	 */
	protected String getPreparedStatement(Object object) {
		QueryInformation queryAndParameters = preparedStatements.getIfPresent(object);

		String query = null;
		if (null != queryAndParameters) {
			query = queryAndParameters.query;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Return prepared sql statement: " + query);
		}

		return query;
	}

	/**
	 * Returns a stored parameters for the object.
	 * 
	 * @param object
	 *            The object which will be used to look up in the map.
	 * @return The list of parameters or <code> null </code> if there is no container for the given
	 *         SQL statement or there are no parameters captured within this SQL statement.
	 */
	protected List<String> getParameters(Object object) {
		QueryInformation queryAndParameters = preparedStatements.getIfPresent(object);
		if (null == queryAndParameters) {
			return null;
		} else {
			return queryAndParameters.getParametersAsList();
		}
	}

	/**
	 * Adds a parameter to a specific prepared statement.
	 * 
	 * @param preparedStatement
	 *            The prepared statement object.
	 * @param index
	 *            The index of the value.
	 * @param value
	 *            The value to be inserted.
	 */
	protected void addParameter(Object preparedStatement, int index, Object value) {
		QueryInformation queryAndParameters = preparedStatements.getIfPresent(preparedStatement);

		if (null == queryAndParameters) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not get the prepared statement from the cache to add a parameter! Prepared Statement:" + preparedStatement + " index:" + index + " value:" + value);
			}
			return;
		}

		String[] parameters = queryAndParameters.getParameters();

		if (0 > index || parameters.length <= index) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Trying to set the parameter with value " + value + " at index " + index + ", but the prepared statement did not have parameter on this index.");
			}
			return;
		}

		if (null != value) {
			if (value instanceof String || value instanceof Date || value instanceof Time || value instanceof Timestamp) {
				parameters[index] = "'" + value.toString() + "'";
			} else {
				parameters[index] = value.toString();
			}
		} else {
			value = NULL_VALUE;
			parameters[index] = (String) value;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Prepared Statement :: Added value:" + value.toString() + " with index:" + index + " to prepared statement:" + preparedStatement);
		}
	}

	/**
	 * Clears all the parameters in the array.
	 * 
	 * @param preparedStatement
	 *            The prepared statement for which all parameters are going to be cleared.
	 */
	protected void clearParameters(Object preparedStatement) {
		QueryInformation queryAndParameters = preparedStatements.getIfPresent(preparedStatement);

		if (null == queryAndParameters) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not get the prepared statement from the cache to clear the parameters! Prepared Statement:" + preparedStatement);
			}
			return;
		}

		queryAndParameters.clearParameters();
	}

	/**
	 * This method adds an SQL String to the current thread local stack. This is needed so that
	 * created prepared statements can be associated to the SQL Strings.
	 * <p>
	 * So if three times the prepared statement method is called with the same string, the stack
	 * contains the string three times. Now the Prepared Statement is created which results in
	 * calling the {@link #addPreparedStatement(Object, String)} method. The last added String is
	 * taken and associated with the object.
	 * 
	 * @param sql
	 *            The SQL String.
	 */
	protected void addSql(String sql) {
		sqlThreadLocalStack.push(sql);
	}

	/**
	 * Removes the last added sql from the thread local stack. We don't need the String object here.
	 */
	protected void removeSql() {
		sqlThreadLocalStack.pop();
	}

	/**
	 * Value container to store the SQL query and its parameters within the cache of prepared
	 * statements. The JDBC sensor in inspectIT allows for two modes. The SQL query can be enhanced
	 * with the values of the bind parameters. This happens if and only if
	 * "SQL Prepared Statement Parameter Replacement" is set. Thus this container ensures that this
	 * calculation is only done if this is needed, that is when the parameters are first accessed.
	 * Thus ensure that you only access the parameters if you really want to fill them!
	 * 
	 * <p>
	 * To access the parameters during the "filling stage", prefer the
	 * <code> public String[] getParameters() </code> method as this allows to access the internal
	 * String[].
	 * 
	 * @author Stefan Siegl
	 */
	private static class QueryInformation {
		/** the SQL query. */
		private String query;

		/**
		 * internal container of the SQL bind values. The size of this array defines the number of
		 * bind values. This field is filled on first access.
		 */
		private String[] parameters = null;

		/**
		 * Creates a new instance of this value container. Please note that creating an instance of
		 * this class does not calculate the number of available bind values (a.k.a. "parameters").
		 * They are creating on first use.
		 * 
		 * @param query
		 *            The SQL query.
		 */
		public QueryInformation(String query) {
			this.query = query;
		}

		/**
		 * Returns the String[] representation of the bind values of this SQL query. This method
		 * should be used over the <code>List<String> getParametersAsList</code> method to fill the
		 * parameters as this method provides access to the backing String[] and is thus more
		 * efficient.
		 * 
		 * <b> please note that the calculation of the number of parameters within the SQL query is
		 * done with the first access to this method. Thus only call this method if you know that
		 * you do have parameters to set, else there will be unnecessary calculations. </b>
		 * 
		 * @return <code>String[]</code> containing the current bind values of this SQL query. The
		 *         size of the array can be used to deduce the number of available bind parameters
		 *         based on the SQL query.
		 */
		public String[] getParameters() {
			if (null == parameters) {
				// Calculate the amount of parameters based on the SQL query. We calculate this
				// value on first request as this is only needed if we have parameter capturing
				// active.
				int count = 0;
				for (int i = query.length() - 1; i > 0; i--) {
					if (query.charAt(i) == '?') {
						count++;
					}
				}
				parameters = new String[count];
			}

			return parameters; // NOPMD: no copy to improve performance
		}

		/**
		 * Resets the bind parameters of this SQL query.
		 * 
		 * <b> please note that the calculation of the number of parameters within the SQL query
		 * will be done as a side-effect of this method (but only if it is not already done before).
		 * Thus only call this method if you know that you do have parameters to set, else there
		 * will be unnecessary calculations. </b>
		 */
		public void clearParameters() {
			if (null == parameters) {
				return;
			}
			parameters = new String[parameters.length];
		}

		/**
		 * Returns the parameter values as <code>List<String></code>. The list will provide a
		 * representation of the parameters. Adding to this list will <b> not </b> change the
		 * parameters. This method is meant to be used from the second after hook to report the
		 * current parameters.
		 * <p>
		 * Please note that to fill the parameters the String[] should be used.
		 * 
		 * @return the parameter values as <code>List<String></code> or <code> null </code> if no
		 *         parameters are captured.
		 */
		public List<String> getParametersAsList() {
			if (null == parameters) {
				return null;
			} else {
				return Arrays.asList(parameters);
			}
		}
	}
}
