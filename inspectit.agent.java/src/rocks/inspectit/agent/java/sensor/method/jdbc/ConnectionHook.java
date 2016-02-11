package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IMethodHook;

/**
 * This hook records the creation of statements so that they can be later retrieved by other hooks
 * to create valid data objects with query statements.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ConnectionHook implements IMethodHook {

	/**
	 * The statement storage to add the statements to.
	 */
	private StatementStorage statementStorage;

	/**
	 * Default constructor which needs a reference to the statement storage.
	 * 
	 * @param statementStorage
	 *            The statement storage.
	 */
	public ConnectionHook(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		String sql = (String) parameters[0];
		statementStorage.addSql(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		statementStorage.removeSql();
	}

}
