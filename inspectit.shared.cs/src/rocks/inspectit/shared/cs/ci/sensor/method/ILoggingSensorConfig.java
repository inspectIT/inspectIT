/**
 *
 */
package info.novatec.inspectit.ci.sensor.method;

import info.novatec.inspectit.ci.sensor.ISensorConfig;

/**
 * Interface for the logging sensors configurations.
 *
 * @author Ivan Senic
 *
 */
public interface ILoggingSensorConfig extends ISensorConfig {

	/**
	 * Returns the logging technology name (for example: log4j).
	 *
	 * @return Returns the logging technology name.
	 */
	String getTechnologyName();

	/**
	 * Returns names of the available log levels that can be set.
	 *
	 * @return Returns names of the available log levels that can be set.
	 */
	String[] getLogLevels();

	/**
	 * Returns the currently configured minimum level to be captured.
	 *
	 * @return Returns the currently configured minimum level to be captured.
	 */
	String getMinLevel();

	/**
	 * Sets the minimum level to be captured.
	 *
	 * @param minLevel
	 *            log level
	 * @throws IllegalArgumentException
	 *             If the given minLevel is not one of the available {@link #getLogLevels()}.
	 */
	void setMinLevel(String minLevel) throws IllegalArgumentException;
}
