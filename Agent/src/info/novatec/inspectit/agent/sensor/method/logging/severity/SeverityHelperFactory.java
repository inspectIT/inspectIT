package info.novatec.inspectit.agent.sensor.method.logging.severity;

/**
 * Factory for building {@link SeverityHelper}s for different logging frameworks.
 * 
 * @author Stefan Siegl
 */
public final class SeverityHelperFactory {

	/**
	 * private.
	 */
	private SeverityHelperFactory() {
	}

	/**
	 * Logging Frameworks.
	 */
	public enum Framework {
		/** Log4J logging Framework. */
		LOG4J
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
		if (Framework.LOG4J.equals(framework)) {
			return new Log4JSeverityHelper(minimumLevel);
		}
		throw new RuntimeException("No logging checker for framework " + framework + " can be found.");
	}
}
