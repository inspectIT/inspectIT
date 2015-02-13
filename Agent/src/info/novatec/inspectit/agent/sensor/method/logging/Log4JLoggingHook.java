package info.novatec.inspectit.agent.sensor.method.logging;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.logging.severity.SeverityHelper;
import info.novatec.inspectit.communication.data.LoggingData;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logging hook for log4j logging capturing. This hook captures all loggings from log4j if the
 * loggings are with a level "greater" than the provided minimum logging level (see
 * {@link SeverityHelper}).
 * 
 * If the minimum logging level is not provided or cannot be found in log4j default levels, the
 * logging hook will not capture any loggings.
 * 
 * This hook is expected to be placed on the method
 * <code>protected void forcedLog(String fqcn, Priority level, Object message, Throwable
 * t)</code> of the class <code>org.apache.log4j.Priority</code>. Putting this hook to other
 * classes/methods can lead to errors.
 * 
 * @author Stefan Siegl
 */
public class Log4JLoggingHook implements IMethodHook {

	/** The logger of this class. Initialized manually. */
	private static final Logger LOG = LoggerFactory.getLogger(Log4JLoggingHook.class);

	/** the id manager. */
	private IIdManager idManager;

	/** the level checker. */
	private SeverityHelper checker;

	/**
	 * Creates a new instance of the Log4J Logging hook.
	 * 
	 * @param idManager
	 *            the idManager.
	 * @param checker
	 *            the level checker.
	 */
	public Log4JLoggingHook(IIdManager idManager, SeverityHelper checker) {
		this.idManager = idManager;
		this.checker = checker;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		// not needed for this hook
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// not needed for this hook
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// get the information from the parameters. We are expecting the method: Priority.forcedLog
		// (String, Priority, Object, Throwable)
		String level = parameters[1].toString();

		if (!checker.shouldCapture(level)) {
			return;
		}

		try {
			LoggingData data = new LoggingData();

			data.setLevel(level);
			data.setMessage(parameters[2].toString());
			data.setPlatformIdent(idManager.getPlatformId());
			data.setSensorTypeIdent(idManager.getRegisteredSensorTypeId(sensorTypeId));
			data.setMethodIdent(idManager.getRegisteredMethodId(methodId));
			data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

			coreService.addMethodSensorData(sensorTypeId, methodId, null, data);
		} catch (IdNotAvailableException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
			}
		}
	}
}
