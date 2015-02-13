package info.novatec.inspectit.agent.sensor.method.logging;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.LoggingData;
import info.novatec.inspectit.util.ReflectionCache;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logging hook for log4j logging capturing. This hook captures all loggings from log4j if the
 * loggings are with a level "greater" than the provided minimum logging level. "Greater" means
 * higher importance, so providing a minimum level of INFO would capture loggings of INFO, WARN and
 * FATAL but not of message with the level DEBUG.
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
 * 
 */
public class Log4JLoggingHook implements IMethodHook {

	/** The logger of this class. Initialized manually. */
	private static final Logger LOG = LoggerFactory.getLogger(Log4JLoggingHook.class);

	/** All available levels for log4j. */
	private static final String[] POSSIBLE_LEVELS = new String[] { "FATAL", "ERROR", "WARN", "INFO", "TRACE", "DEBUG" };

	/** String for the method. */
	private static final String METHOD_IS_GREATER_OR_EQUAL = "isGreaterOrEqual";

	/** String for the method. */
	private static final String METHOD_TO_STRING = "toString";

	/** String for the method. */
	private static final String METHOD_TO_LEVEL = "toLevel";

	/** the id manager. */
	private IIdManager idManager;

	/** the given minimum level of log messages that should be captured. */
	private String minimumLevelToCapture;

	/**
	 * caches the minimum desired logging level to capture as object of the class
	 * org.apache.log4j.Level.
	 */
	private Object minimumLevelObject;

	/** used to cache reflection calls. */
	private ReflectionCache cache = new ReflectionCache();

	/** marks whether or not the hook is correctly initialized. */
	private boolean correctlyInitialized = true;

	/**
	 * Creates a new instance of the Log4J Logging hook.
	 * 
	 * @param idManager
	 *            the idManager.
	 * @param minimumLevelToCapture
	 *            a string representation of the minimum level that should be captured.
	 */
	public Log4JLoggingHook(IIdManager idManager, String minimumLevelToCapture) {
		this.idManager = idManager;
		this.minimumLevelToCapture = minimumLevelToCapture;

		if (!isPossibleLevel(minimumLevelToCapture)) {
			correctlyInitialized = false;
			LOG.warn("The given minimum level to capture [" + minimumLevelToCapture + "] cannot be found for log4j. The log4j logging sensor is thus DISABLED.");
		}
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
		if (!correctlyInitialized) {
			return;
		}

		// get the information from the parameters. We are expecting the method: forcedLog (String,
		// Priority, Object, Throwable)
		Object levelObject = parameters[1];
		Object messageInformation = parameters[2];

		if (!shouldCapture(levelObject)) {
			return;
		}

		try {
			LoggingData data = new LoggingData();
			
			data.setLevel((String) cache.invokeMethod(levelObject.getClass(), METHOD_TO_STRING, null, levelObject, null, null));
			data.setMessage((String) cache.invokeMethod(messageInformation.getClass(), METHOD_TO_STRING, null, messageInformation, null, null));
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

	/**
	 * checks whether this logging should be captured by the hook.
	 * 
	 * @param levelObject
	 *            The org.apache.log4j.Level object that is logged via the application.
	 * @return whether or not this logging should be captured.
	 */
	private boolean shouldCapture(Object levelObject) {
		Class<?> levelClass = levelObject.getClass();

		// we need to get an object for the minimum level to capture, so that we can compare
		// the given level against this level. We cache this in a field. For this cache
		// it is enough to just use a field. If called in parallel, the lookup will just
		// be done multiple times.
		if (null == minimumLevelObject) {
			minimumLevelObject = getMinimumLevelAsObject(levelClass, minimumLevelToCapture);
		}

		// this is the safe way to get a Class instance of org.apache.log4j.Priority. Using
		// Class.forName() can lead to situations in which a wrong instance of the Class instance is
		// returned.
		Class<?> priorityClass = levelClass.getSuperclass();

		return (Boolean) cache.invokeMethod(levelClass, METHOD_IS_GREATER_OR_EQUAL, new Class[] { priorityClass }, levelObject, new Object[] { minimumLevelObject }, Boolean.FALSE);
	}

	/**
	 * This methods tries to get the given minimum level description as
	 * <code>org.apache.log4j.Level</code> object. If it fails it returns <code>null</code>;
	 * 
	 * Friendly access for easier testing.
	 * 
	 * @param levelClass
	 *            the class instance of org.apache.log4j.Level. Please note that we cannot use
	 *            Class.forName() here as this might give us back a wrong class instance.
	 * @param level
	 *            The string of the level
	 * @return the <code>org.apache.log4j.Level</code> object or <code>null</code> is no match is
	 *         found.
	 */
	Object getMinimumLevelAsObject(Class<?> levelClass, String level) {
		// note that toLevel tries to find a level matching the given string. If it fails
		// it returns DEBUG. The constructor already checks the given minimum level to be a correct
		// level.
		return cache.invokeMethod(levelClass, METHOD_TO_LEVEL, new Class[] { String.class }, null, new Object[] { level }, null);
	}

	/**
	 * Checks if the given String is a possible level for log4j.
	 * 
	 * @param s
	 *            the level as string.
	 * @return if this level is available for log4j.
	 */
	private boolean isPossibleLevel(String s) {
		if (null == s) {
			return false;
		}
		s = s.toUpperCase();
		for (int i = 0; i < POSSIBLE_LEVELS.length; i++) {
			if (POSSIBLE_LEVELS[i].equals(s)) {
				return true;
			}
		}
		return false;
	}
}
