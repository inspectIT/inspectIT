package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
	public void init(Map<String, Object> parameter) {
		preparedStatementParameterHook = new PreparedStatementParameterHook(statementStorage);
	}

}
