package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;

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
	 * The Platform manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * Storage for connection meta data.
	 */
	private final ConnectionMetaDataStorage connectionMetaDataStorage;

	/**
	 * The ThreadLocal for a boolean value so only the last before and first after hook of an
	 * invocation is measured.
	 */
	private final ThreadLocal<Boolean> threadLast = new ThreadLocal<Boolean>();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private final StringConstraint strConstraint;

	/**
	 * Caches the calls to getConnection().
	 */
	private final StatementReflectionCache statementReflectionCache;

	/**
	 * The only constructor which needs the {@link Timer}.
	 *
	 * @param timer
	 *            The timer.
	 * @param platformManager
	 *            The Platform manager.
	 * @param parameter
	 *            Additional parameters.
	 * @param connectionMetaDataStorage
	 *            the storage containing meta information on the connection.
	 * @param statementReflectionCache
	 *            Caches the calls to getConnection()
	 */
	public StatementHook(Timer timer, IPlatformManager platformManager, ConnectionMetaDataStorage connectionMetaDataStorage, StatementReflectionCache statementReflectionCache, Map<String, Object> parameter) {
		this.timer = timer;
		this.platformManager = platformManager;
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
					long platformId = platformManager.getPlatformId();

					sqlData = new SqlStatementData(timestamp, platformId, sensorTypeId, methodId);
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
