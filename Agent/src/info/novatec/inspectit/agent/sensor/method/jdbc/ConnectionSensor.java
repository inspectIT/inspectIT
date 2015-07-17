package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;

import java.sql.PreparedStatement;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This sensor initializes the {@link ConnectionHook} to intercept the creation of
 * {@link PreparedStatement} classes.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ConnectionSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The statement storage.
	 */
	@Autowired
	private StatementStorage statementStorage;

	/**
	 * The used prepared statement hook.
	 */
	private ConnectionHook connectionHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public ConnectionSensor() {
	}

	/**
	 * The default constructor which needs one parameter for initialization.
	 * 
	 * @param statementStorage
	 *            The statement storage.
	 */
	public ConnectionSensor(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return connectionHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		connectionHook = new ConnectionHook(statementStorage);
	}

}
