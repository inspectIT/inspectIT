package info.novatec.inspectit.agent.sensor.method.logging;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.logging.severity.SeverityHelper;
import info.novatec.inspectit.agent.sensor.method.logging.severity.SeverityHelperFactory;
import info.novatec.inspectit.agent.sensor.method.logging.severity.SeverityHelperFactory.Framework;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Logging sensor to capture log4j loggings.
 * 
 * @author Stefan Siegl
 */
public class Log4JLoggingSensor extends AbstractMethodSensor implements IMethodSensor {

	/** Configuration key for the minimum level that should be captured. */
	public static final String CONFIG_KEY_MINIMUM_LEVEL = "minlevel";

	/** The logger of this class. Initialized manually. */
	private static final Logger LOG = LoggerFactory.getLogger(Log4JLoggingHook.class);

	/**
	 * Used for creating and resolving ids necessary to communicate with the server.
	 */
	@Autowired
	private IIdManager idManager;

	/** hook instance. */
	private Log4JLoggingHook hook;

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		// read the desired minimum level and pass it to the hook
		String minimumLevelToCapture = (String) parameter.get(CONFIG_KEY_MINIMUM_LEVEL);

		SeverityHelper checker = SeverityHelperFactory.getForFramework(Framework.LOG4J, minimumLevelToCapture);
		if (!checker.isValid()) {
			LOG.warn("The configured minimum logging severity " + minimumLevelToCapture + " is not found for the technology log4j. No logging will be captured.");
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("minimum level is " + minimumLevelToCapture);
			}

			hook = new Log4JLoggingHook(idManager, checker);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}
}
