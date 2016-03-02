package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implementation for the statement sensor. It uses the {@link ThreadLocalStack} class to
 * know if some execute methods call each other which would result in multiple data objects for only
 * one query. After the complete SQL method was executed, it computes how long the method took to
 * finish and saves the executed SQL Statement String. Afterwards, the measurement is added to the
 * {@link CoreService}.
 * 
 * @author Christian Herzog
 * @author Patrice Bouillet
 * 
 */
public class StatementHook implements IMethodHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StatementHook.class);

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * Storage for connection meta data.
	 */
	private final ConnectionMetaDataStorage connectionMetaDataStorage;

	/**
	 * The ThreadLocal for a boolean value so only the last before and first after hook of an
	 * invocation is measured.
	 */
	private ThreadLocal<Boolean> threadLast = new ThreadLocal<Boolean>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

	/**
	 * Caches the calls to getConnection().
	 */
	private StatementReflectionCache statementReflectionCache;

	/**
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param parameter
	 *            Additional parameters.
	 * @param connectionMetaDataStorage
	 *            the storage containing meta information on the connection.
	 * @param statementReflectionCache
	 *            Caches the calls to getConnection()
	 */
	public StatementHook(Timer timer, IIdManager idManager, ConnectionMetaDataStorage connectionMetaDataStorage, StatementReflectionCache statementReflectionCache, Map<String, Object> parameter) {
		this.timer = timer;
		this.idManager = idManager;
		this.connectionMetaDataStorage = connectionMetaDataStorage;
		this.strConstraint = new StringConstraint(parameter);
		this.statementReflectionCache = statementReflectionCache;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		threadLast.set(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();

		if (threadLast.get().booleanValue()) {
			threadLast.set(Boolean.FALSE);

			double duration = endTime - startTime;
			String sql = parameters[0].toString();
			SqlStatementData sqlData = (SqlStatementData) coreService.getMethodSensorData(sensorTypeId, methodId, sql);

			if (null == sqlData) {
				try {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));
					long platformId = idManager.getPlatformId();
					long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
					long registeredMethodId = idManager.getRegisteredMethodId(methodId);

					sqlData = new SqlStatementData(timestamp, platformId, registeredSensorTypeId, registeredMethodId);
					sqlData.setPreparedStatement(false);
					sqlData.setSql(strConstraint.crop(sql));
					sqlData.setDuration(duration);
					sqlData.calculateMin(duration);
					sqlData.calculateMax(duration);
					sqlData.setCount(1L);

					// populate the connection meta data.
					connectionMetaDataStorage.populate(sqlData, statementReflectionCache.getConnection(object.getClass(), object));
					coreService.addMethodSensorData(sensorTypeId, methodId, sql, sqlData);
				} catch (IdNotAvailableException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Could not save the sql data because of an unavailable id. " + e.getMessage());
					}
				}
			} else {
				sqlData.increaseCount();
				sqlData.addDuration(duration);

				sqlData.calculateMin(duration);
				sqlData.calculateMax(duration);
			}
		}
	}

}
