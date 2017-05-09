package rocks.inspectit.agent.java.sensor.method.jdbc;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;

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
	@Override
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		String sql = (String) parameters[0];
		statementStorage.addSql(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, boolean exception, RegisteredSensorConfig rsc) { // NOCHK:8-params
		statementStorage.removeSql();
	}

}
