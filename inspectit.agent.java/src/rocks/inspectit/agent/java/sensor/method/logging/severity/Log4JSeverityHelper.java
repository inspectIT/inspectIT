package info.novatec.inspectit.agent.sensor.method.logging.severity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link SeverityHelper} for the logging framework log4j.
 *
 * @author Stefan Siegl
 */
public class Log4JSeverityHelper extends SeverityHelper {

	/** all logging levels of log4j sorted ascending. */
	private static final List<String> LEVELS = Collections.unmodifiableList(Arrays.asList("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"));

	/**
	 * Constructor.
	 *
	 * @param minimumLevel
	 *            the minimum level to capture.
	 */
	public Log4JSeverityHelper(String minimumLevel) {
		super(minimumLevel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getOrderedLevels() {
		return LEVELS;
	}
}
