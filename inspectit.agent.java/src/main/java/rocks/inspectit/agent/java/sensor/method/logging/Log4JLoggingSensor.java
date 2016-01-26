package rocks.inspectit.agent.java.sensor.method.logging;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelper;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelperFactory;
import rocks.inspectit.agent.java.sensor.method.logging.severity.SeverityHelperFactory.Framework;

/**
 * Logging sensor to capture log4j loggings.
 *
 * @author Stefan Siegl
 */
public class Log4JLoggingSensor extends AbstractMethodSensor implements IMethodSensor {

	/** Configuration key for the minimum level that should be captured. */
	public static final String CONFIG_KEY_MINIMUM_LEVEL = "minlevel";

	/**
	 * Used for creating and resolving ids necessary to communicate with the server.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/** hook instance. */
	private Log4JLoggingHook hook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		// read the desired minimum level and pass it to the hook
		String minimumLevelToCapture = (String) parameters.get(CONFIG_KEY_MINIMUM_LEVEL);
		hook = new Log4JLoggingHook(platformManager, minimumLevelToCapture);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
