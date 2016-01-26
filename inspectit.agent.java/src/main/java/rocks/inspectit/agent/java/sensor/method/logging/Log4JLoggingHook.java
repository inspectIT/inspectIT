package rocks.inspectit.agent.java.sensor.method.logging;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelper;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelperFactory;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelperFactory.Framework;
import rocks.inspectit.shared.all.communication.data.LoggingData;

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

	/** the platform manager. */
	private final IPlatformManager platformManager;

	/** the level checker. */
	private final SeverityHelper checker;

	/** caches whether the hook has a correct minimum level. */
	// private final boolean correctlyInitialized;

	/**
	 * Creates a new instance of the Log4J Logging hook.
	 *
	 * @param platformManager
	 *            the platformManager.
	 * @param minimumLevelToCapture
	 *            the minimum logging level to capture.
	 */
	public Log4JLoggingHook(IPlatformManager platformManager, String minimumLevelToCapture) {
		this.platformManager = platformManager;

		checker = SeverityHelperFactory.getForFramework(Framework.LOG4J, minimumLevelToCapture);
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
		if (checker.isValid()) {
			// get the information from the parameters. We are expecting the
			// method: Priority.forcedLog (String, Priority, Object, Throwable)
			String level = String.valueOf(parameters[1]);

			if (!checker.shouldCapture(level)) {
				return;
			}

			try {
				long platformId = platformManager.getPlatformId();

				LoggingData data = new LoggingData();
				data.setLevel(level);
				data.setMessage(String.valueOf(parameters[2]));
				data.setPlatformIdent(platformId);
				data.setSensorTypeIdent(sensorTypeId);
				data.setMethodIdent(methodId);
				data.setTimeStamp(new Timestamp(System.currentTimeMillis()));

				// TODO: Note that setting the prefix to null here is only
				// meaningful for the
				// current integration version of the logging sensor where
				// loggings outside of
				// invocation sequences is not yet supported!
				coreService.addMethodSensorData(sensorTypeId, methodId, null, data);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		}
	}
}
