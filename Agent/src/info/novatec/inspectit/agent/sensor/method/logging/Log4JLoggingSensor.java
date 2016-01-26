package info.novatec.inspectit.agent.sensor.method.logging;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
	private IIdManager idManager;

	/** hook instance. */
	private Log4JLoggingHook hook;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		// read the desired minimum level and pass it to the hook
		String minimumLevelToCapture = (String) parameters.get(CONFIG_KEY_MINIMUM_LEVEL);
		hook = new Log4JLoggingHook(idManager, minimumLevelToCapture);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
