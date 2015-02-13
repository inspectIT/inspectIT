package info.novatec.inspectit.agent.sensor.method.logging.severity;

import java.util.List;

/**
 * Base class for SeverityHelpers that works on a sorted list of severity levels and calculate the
 * capturing decision on the index within the list.
 *
 * @author Stefan Siegl
 */
public abstract class SeverityHelper {

	/**
	 * Index of the minimumLevel within the sorted list.
	 */
	private final int indexOfMinimumLevel;

	/**
	 * The string representation of the minimum level that was given.
	 */
	private final String minimumLevel;

	/**
	 * Constructor taking the minimum level as String.
	 *
	 * @param minimumLevel
	 *            the minimum level as String.
	 */
	public SeverityHelper(String minimumLevel) {
		this.minimumLevel = minimumLevel;
		indexOfMinimumLevel = getIndex(minimumLevel);
	}

	/**
	 * Whether the given configuration is valid.
	 *
	 * @return if the severity helper is valid.
	 */
	public boolean isValid() {
		return indexOfMinimumLevel != -1;
	}

	/**
	 * Returns whether the given logging level should be captured. Invalid logging levels (that is
	 * logging levels that are not supported by the framework) will always return false.
	 *
	 * @param loggingLevel
	 *            the string representation of the logging level that should be checked.
	 * @return if entries with this logging level should be captured.
	 */
	public boolean shouldCapture(String loggingLevel) {
		return getIndex(loggingLevel) <= indexOfMinimumLevel;
	}

	/**
	 * Returns the configured minimum logging level for this severity checker.
	 *
	 * @return the configured minimum logging level for this severity checker.
	 */
	public String getMinimumLevel() {
		return minimumLevel;
	}

	/**
	 * Returns the index of the given level string or -1 if it is not found.
	 *
	 * @param loggingLevel
	 *            the level.
	 * @return the index of the given level string or -1 if it is not found.
	 */
	private int getIndex(String loggingLevel) {
		return getOrderedLevels().indexOf(loggingLevel);
	}

	/**
	 * Should return an (potentially) unmodifiable list that contains all logging severity the
	 * technology provides. The sorting of the level is important. The severity must be decreasing
	 * with the highest severity first to the lowest severity.
	 *
	 * @return the list of available logging severities in a ordered fashion.
	 */
	public abstract List<String> getOrderedLevels();
}
