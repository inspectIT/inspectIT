package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The SQL timer sensor which initializes and returns the {@link StatementHook} class.
 *
 * @author Christian Herzog
 *
 */
public class StatementSensor extends AbstractMethodSensor implements IMethodSensor {

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
	 * The used statement hook.
	 */
	private StatementHook statementHook = null;

	/**
	 * The default constructor which needs 2 parameter for initialization. Caches the calls to
	 * getConnection().
	 */
	@Autowired
	private StatementReflectionCache statementReflectionCache;

	/**
	 * Storage for connection meta data.
	 */
	@Autowired
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	/**
	 * Returns the method hook.
	 *
	 * @return The method hook.
	 */
	public IHook getHook() {
		return statementHook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		statementHook = new StatementHook(timer, idManager, connectionMetaDataStorage, statementReflectionCache, parameters);
	}

}
