package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * @author Patrice Bouillet
 *
 */
public class PreparedStatementParameterSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The statement storage.
	 */
	@Autowired
	private StatementStorage statementStorage;

	/**
	 * The used prepared statement hook.
	 */
	private PreparedStatementParameterHook preparedStatementParameterHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public PreparedStatementParameterSensor() {
	}

	/**
	 * The default constructor which needs one parameter for initialization.
	 *
	 * @param statementStorage
	 *            The statement storage.
	 */
	public PreparedStatementParameterSensor(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return preparedStatementParameterHook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		preparedStatementParameterHook = new PreparedStatementParameterHook(statementStorage);
	}

}
