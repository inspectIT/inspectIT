package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IConstructorHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;

/**
 * This hook is intended to intercept the created prepared statement calls to the database. To not
 * create duplicate calls, a {@link ThreadLocal} attribute is used to check if this specific
 * statement is already 'sent'.
 * <p>
 * Furthermore, a {@link StatementStorage} is used which saves all the created prepared statements
 * and if the parameter hook for the sqls is installed and activated, the parameters are replaced.
 *
 * @author Patrice Bouillet
 *
 */
public class PreparedStatementHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	Logger log = LoggerFactory.getLogger(PreparedStatementHook.class);

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
	 * The statement storage.
	 */
	private final StatementStorage statementStorage;

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
	 * Contains all method idents of all prepared statements that had a problem finding the stored
	 * SQL statement. Using this structure we can ensure that we do not throw the exception always
	 * again.
	 */
	private static List<Long> preparedStatementsWithExceptions = new ArrayList<Long>(0);

	/**
	 * The only constructor which needs the {@link Timer}.
	 *
	 * @param timer
	 *            The timer.
	 * @param platformManager
	 *            The Platform manager.
	 * @param statementStorage
	 *            The statement storage.
	 * @param parameter
	 *            Additional parameters.
	 * @param connectionMetaDataStorage
	 *            the meta information storage for connections.
	 * @param statementReflectionCache
	 *            Caches the calls to getConnection().
	 */
	public PreparedStatementHook(Timer timer, IPlatformManager platformManager, StatementStorage statementStorage, ConnectionMetaDataStorage connectionMetaDataStorage,
			StatementReflectionCache statementReflectionCache, Map<String, Object> parameter) {
		this.timer = timer;
		this.platformManager = platformManager;
		this.statementStorage = statementStorage;
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

			String sql = statementStorage.getPreparedStatement(object);
			if (null != sql) {
				double duration = endTime - startTime;
				SqlStatementData sqlData = (SqlStatementData) coreService.getMethodSensorData(sensorTypeId, methodId, sql);
				if (null == sqlData) {
					try {
						Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));
						List<String> params = statementStorage.getParameters(object);
						long platformId = platformManager.getPlatformId();

						sqlData = new SqlStatementData(timestamp, platformId, sensorTypeId, methodId);
						sqlData.setPreparedStatement(true);
						sqlData.setSql(strConstraint.crop(sql));
						sqlData.setDuration(duration);
						sqlData.calculateMin(duration);
						sqlData.calculateMax(duration);
						sqlData.setCount(1L);
						sqlData.setParameterValues(params);

						// populate the connection meta data.
						connectionMetaDataStorage.populate(sqlData, statementReflectionCache.getConnection(object.getClass(), object));

						coreService.addMethodSensorData(sensorTypeId, methodId, sql, sqlData);
					} catch (IdNotAvailableException e) {
						if (log.isDebugEnabled()) {
							log.debug("Could not save the sql data because of an unavailable id. " + e.getMessage());
						}
					}
				} else {
					sqlData.increaseCount();
					sqlData.addDuration(duration);

					sqlData.calculateMin(duration);
					sqlData.calculateMax(duration);
				}
			} else {
				// the sql was not found, we'll try again
				threadLast.set(Boolean.TRUE);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		try {
			statementStorage.addPreparedStatement(object);
		} catch (NoSuchElementException e) {
			// Ensure that a problem with this statement is only thrown once to not spam the log
			// file. It is possible that we hide exceptions.
			Long methodIdLong = Long.valueOf(methodId);
			if (preparedStatementsWithExceptions.contains(methodIdLong)) {
				// we already logged this exception...
				return;
			}

			// it is possible that this exception is thrown in a 'normal' way,
			// as everyone could instantiate a prepared statement object without
			// calling first a method on the connection (prepareStatement...)
			log.info("Could not add prepared statement, no sql available! Method ID(local): " + methodId);
			log.info(
					"This is not an inspectIT issue, but you forgot to integrate the Connection creating the SQL statement in the configuration, please consult the management of inspectIT and send the following stacktrace!",
					e);

			// we need to ensure thread safety for the list and do not care for lost updates, so
			// we simply create a new list based on the old list and change references after we
			// finished building it.
			List<Long> clonedList = new ArrayList<Long>(preparedStatementsWithExceptions);
			clonedList.add(methodIdLong);
			preparedStatementsWithExceptions = clonedList;
		}
	}
}
