package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Provides the connection for a given statement.
 *
 * @author Stefan Siegl
 */
@Component
public class StatementReflectionCache extends ReflectionCache {

	/** Caches the method name. */
	private static final String GET_CONNECTION_METHOD_NAME = "getConnection";

	/**
	 * Retrieves the connection.
	 *
	 * @param statementClass
	 *            the class of the statement instance.
	 * @param statementInstance
	 *            the instance of the statement.
	 * @return the associated connection.
	 */
	public Connection getConnection(Class<?> statementClass, Object statementInstance) {
		return (Connection) invokeMethod(statementClass, GET_CONNECTION_METHOD_NAME, null, statementInstance, null, null);
	}
}
