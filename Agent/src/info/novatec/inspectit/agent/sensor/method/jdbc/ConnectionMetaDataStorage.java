package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.ReflectionCache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Storage for the meta information of JDBC connection classes.
 * 
 * @author Stefan Siegl
 */
@Component
public class ConnectionMetaDataStorage {

	/**
	 * Empty server as marker that a meta data can not be created for connection.
	 */
	protected static final ConnectionMetaData EMPTY = new ConnectionMetaData();

	/**
	 * This cache keeps track of the meta information for connection objects. The key is the <code>
	 * Connection </code> object. As connections are re-used it makes sense to keep the meta
	 * information cached, as to not request this information over and over again.
	 * 
	 * Also we use weak keys as to allow the garbage collector to remove the entries as soon as
	 * nobody else holds references to the connection (if this happens, the connection itself is
	 * garbage collected and thus we need not hold meta information anymore).
	 * 
	 * Also note that having weakKeys tells the Cache to use identity comparison (==) instead of
	 * equals() comparison. This is just the thing we want as we can ensure that connections
	 * identity stays that same. On top of that == is faster than equals.
	 * 
	 * <b> Note that this data structure provides atomic access like a <code>ConcurrentMap</code>.
	 * </b>.
	 * 
	 * Package access for easier testing.
	 */
	Cache<Object, ConnectionMetaData> storage = CacheBuilder.newBuilder().weakKeys().softValues().build();

	/**
	 * Extractor to read data from the connection instance.
	 * 
	 * Package access for easier testing.
	 */
	ConnectionMetaDataExtractor dataExtractor = new ConnectionMetaDataExtractor();

	/**
	 * Populates the given SQL Statement data with the meta information from the storage if this
	 * data exist.
	 * 
	 * @param sqlData
	 *            the data object to populate.
	 * @param connection
	 *            the connection.
	 */
	public void populate(SqlStatementData sqlData, Object connection) {
		ConnectionMetaData connectionMetaData = get(connection);
		if (null != connectionMetaData && EMPTY != connectionMetaData) { // NOPMD == on purpose
			sqlData.setDatabaseProductName(connectionMetaData.product);
			sqlData.setDatabaseProductVersion(connectionMetaData.version);
			sqlData.setDatabaseUrl(connectionMetaData.url);
		}
	}

	/**
	 * Retrieves the <code>ConnectionMetaData</code> stored with this connection.
	 * 
	 * @param connection
	 *            the connection instance
	 * @return the <code>ConnectionMetaData</code> stored with this connection.
	 */
	private ConnectionMetaData get(final Object connection) {
		if (null == connection) {
			return null;
		}
		try {
			return storage.get(connection, new Callable<ConnectionMetaData>() {
				public ConnectionMetaData call() throws Exception {
					ConnectionMetaData data = dataExtractor.parse(connection);
					return data != null ? data : EMPTY;
				}
			});
		} catch (ExecutionException e) {
			// should not occur as we have no checked exceptions
			return EMPTY;
		}
	}

	/**
	 * Value holder for meta information of connection instances.
	 * 
	 * @author Stefan Siegl
	 */
	public static class ConnectionMetaData {
		/** The connection URL. */
		public String url; // NOCHK
		/** The product name of the database. */
		public String product; // NOCHK
		/** The version of the database. */
		public String version; // NOCHK
	}

	/**
	 * Extractor to retrieve connection meta data information. This class uses reflection to get the
	 * information from the connection. To ensure high performance it caches the reflection
	 * <code>Method</code> objects using the {@link ReflectionCache}.
	 * 
	 * @author Stefan Siegl
	 */
	static class ConnectionMetaDataExtractor {

		/** Method names. */
		private static final String GET_META_DATA = "getMetaData";
		/** Method names. */
		private static final String GET_URL = "getURL";
		/** Method names. */
		private static final String GET_DATABASE_PRODUCT_VERSION = "getDatabaseProductVersion";
		/** Method names. */
		private static final String GET_DATABASE_PRODUCT_NAME = "getDatabaseProductName";
		/** Method names. */
		private static final String IS_CLOSED = "isClosed";

		/** Extractor for the JDBC URL. */
		static JDBCUrlExtractor urlExtractor = new JDBCUrlExtractor();
		/** Cache for the <code> Method </code> elements. */
		static ReflectionCache cache = new ReflectionCache();

		/**
		 * The logger of this class. Initialized manually.
		 */
		static Logger logger = LoggerFactory.getLogger(ConnectionMetaDataExtractor.class);

		/**
		 * Parses a given <code>Connection</code> and retrieves the monitoring-related meta
		 * information.
		 * 
		 * @param connection
		 *            the <code>Connection</code> object.
		 * @return meta information about the connection for monitoring. returns <code>null</code>
		 *         in case connection is <code>null</code> or connection is closed.
		 */
		public ConnectionMetaData parse(Object connection) {
			if (null == connection) {
				logger.warn("Meta Information on database cannot be read for the null connection.");
				return null;
			}

			Class<?> connectionClass = connection.getClass();
			if (isClosed(connectionClass, connection)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Meta Information on database cannot be read because the connection is closed.");
				}
				return null;
			}

			ConnectionMetaData data = new ConnectionMetaData();
			Object metaData = getMetaData(connectionClass, connection);
			if (null == metaData) {
				logger.warn("Meta information on database cannot be read for connection " + connection.toString() + ". No database details like URL or Vendor will be displayed.");
				return data;
			}

			Class<?> metaDataClass = metaData.getClass();

			data.version = parseVersion(metaDataClass, metaData);
			data.url = parseTarget(metaDataClass, metaData);
			data.product = parseProduct(metaDataClass, metaData);

			return data;
		}

		/**
		 * Checks if the connection is closed.
		 * 
		 * @param connectionClass
		 *            the connection class.
		 * @param connection
		 *            the connection instance.
		 * @return the result of calling isClosed on the connection object or <code>true</code> any
		 *         exception occurs during method invocation
		 */
		private boolean isClosed(Class<?> connectionClass, Object connection) {
			return (Boolean) cache.invokeMethod(connectionClass, IS_CLOSED, null, connection, null, true);
		}

		/**
		 * Retrieves the meta information object from the connection.
		 * 
		 * @param connectionClass
		 *            the connection class.
		 * @param connection
		 *            the connection instance.
		 * @return the meta information object from the connection or <code>null</code> in case of
		 *         problems.
		 */
		private Object getMetaData(Class<?> connectionClass, Object connection) {
			return cache.invokeMethod(connectionClass, GET_META_DATA, null, connection, null, null);
		}

		/**
		 * Retrieves the target/url from the jdbc connection string.
		 * 
		 * @param databaseMetaDataClass
		 *            the meta information class.
		 * @param databaseMetaData
		 *            the meta information object of the connection.
		 * @return the target/url from the jdbc connection string.
		 */
		private String parseTarget(Class<?> databaseMetaDataClass, Object databaseMetaData) {
			String url = (String) cache.invokeMethod(databaseMetaDataClass, GET_URL, null, databaseMetaData, null, null);
			return urlExtractor.extractURLfromJDBCURL(url);
		}

		/**
		 * Retrieves the version of the database.
		 * 
		 * @param databaseMetaDataClass
		 *            the meta information class.
		 * @param databaseMetaData
		 *            the meta information instance of the connection.
		 * @return the version of the database.
		 */
		private String parseVersion(Class<?> databaseMetaDataClass, Object databaseMetaData) {
			return (String) cache.invokeMethod(databaseMetaDataClass, GET_DATABASE_PRODUCT_VERSION, null, databaseMetaData, null, null);
		}

		/**
		 * Retrieves the product name of the database.
		 * 
		 * @param databaseMetaDataClass
		 *            the meta information class.
		 * @param databaseMetaData
		 *            the meta information instance of the connection.
		 * @return the product name of the database.
		 */
		private String parseProduct(Class<?> databaseMetaDataClass, Object databaseMetaData) {
			return (String) cache.invokeMethod(databaseMetaDataClass, GET_DATABASE_PRODUCT_NAME, null, databaseMetaData, null, null);
		}
	}

	/**
	 * Extractor to retrieve the concrete URL from the JDBC connection string.
	 * 
	 * @author Stefan Siegl
	 */
	static class JDBCUrlExtractor {
		/**
		 * URL pattern to read jdbc URL from jdbc connection string.
		 * jdbc:sqlserver://[serverName[\instanceName
		 * ][:portNumber]][;property=value[;property=value]]
		 * jdbc:db2://<HOST>:<PORT>/<DATABASE_NAME> --> remove the //
		 * jdbc:h2:../../database/database/dvdstore22
		 * 
		 * Oracle is once again different: http://www.orafaq.com/wiki/JDBC
		 * "jdbc:oracle:thin:@//myhost:1521/orcl"; "jdbc:oracle:thin:@myhost:1521:orcl";
		 * "jdbc:oracle:oci:@myhost:1521:orcl";
		 * 
		 * use: http://www.regexr.com/ to play around with regex. See
		 * http://www.regular-expressions.info/named.html as great reference.
		 */
		private final Pattern urlPattern = Pattern.compile("^jdbc:(?:oracle:.*?|.*?):(?:[@/]*)?(.*?)([;?].*)?$");

		/**
		 * Extracts the url from the connection string.
		 * 
		 * @param url
		 *            the connection string
		 * @return the url.
		 */
		public String extractURLfromJDBCURL(String url) {
			try {
				final Matcher matcher = urlPattern.matcher(url);
				matcher.find();
				return matcher.group(1);
			} catch (IllegalStateException i) {
				return url;
			}
		}
	}
}
