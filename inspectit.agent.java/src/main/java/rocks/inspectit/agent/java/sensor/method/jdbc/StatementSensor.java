package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IIdManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.util.Timer;

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
	public void init(Map<String, Object> parameter) {
		statementHook = new StatementHook(timer, idManager, connectionMetaDataStorage, statementReflectionCache, parameter);
	}

}
