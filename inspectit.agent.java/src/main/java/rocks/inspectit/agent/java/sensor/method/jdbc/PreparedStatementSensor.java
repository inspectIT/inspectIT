package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IIdManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

/**
 * @author Patrice Bouillet
 *
 */
public class PreparedStatementSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The timer used for accurate measuring.
	 */
	@Autowired
	private Timer timer;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The statement storage.
	 */
	@Autowired
	private StatementStorage statementStorage;

	/**
	 * Caches the calls to getConnection().
	 */
	@Autowired
	private StatementReflectionCache statementReflectionCache;

	/**
	 * Storage for connection meta data.
	 */
	@Autowired
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	/**
	 * The used prepared statement hook.
	 */
	private PreparedStatementHook preparedStatementHook = null;

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return preparedStatementHook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		preparedStatementHook = new PreparedStatementHook(timer, idManager, statementStorage, connectionMetaDataStorage, statementReflectionCache, parameters);
	}

}
