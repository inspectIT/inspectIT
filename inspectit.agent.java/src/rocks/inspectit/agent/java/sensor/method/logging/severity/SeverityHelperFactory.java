package info.novatec.inspectit.agent.sensor.method.logging.severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for building {@link SeverityHelper}s for different logging frameworks.
 *
 * @author Stefan Siegl
 */
public final class SeverityHelperFactory {

	/** The logger of this class. Initialized manually. */
	private static final Logger LOG = LoggerFactory.getLogger(SeverityHelperFactory.class);

	/**
	 * Logging Frameworks.
	 */
	public enum Framework {
		/** Log4J logging Framework. */
		LOG4J
	}

	/**
	 * private.
	 */
	private SeverityHelperFactory() {
	}

	/**
	 * Returns a {@link SeverityHelper} for the given Logging-{@link Framework} and initializes it
	 * with the given minimum level that should be captured.
	 *
	 * @param framework
	 *            the logging framework.
	 * @param minimumLevel
	 *            the minimum level to be captured.
	 * @return a {@link SeverityHelper} for the given Logging-{@link Framework} and initializes it
	 *         with the given minimum level that should be captured
	 */
	public static SeverityHelper getForFramework(Framework framework, String minimumLevel) {
		SeverityHelper helper;

		switch (framework) {
		case LOG4J:
			helper = new Log4JSeverityHelper(minimumLevel);
			break;
		default:
			throw new RuntimeException("No logging checker for framework " + framework + " can be found.");
		}

		if (!helper.isValid()) {
			LOG.warn(
					"The configured minimum logging severity " + minimumLevel + " is not found for the technology log4j. No logging will be captured. Defined levels are " + helper.getOrderedLevels());
		}

		return helper;
	}
}
