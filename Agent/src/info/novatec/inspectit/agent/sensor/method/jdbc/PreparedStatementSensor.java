package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
	public void init(Map<String, Object> parameter) {
		preparedStatementHook = new PreparedStatementHook(timer, idManager, statementStorage, connectionMetaDataStorage, statementReflectionCache, parameter);
	}

}
