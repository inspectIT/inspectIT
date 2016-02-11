package info.novatec.inspectit.ci.strategy;

import java.util.Map;

/**
 * Interface for all sending and buffering strategies.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStrategyConfig {

	/**
	 * Returns the fully qualified class name of this configuration.
	 * 
	 * @return The fully qualified class name of this configuration.
	 */
	String getClassName();

	/**
	 * Returns the settings of this configuration.
	 * <p>
	 * Note that this method is only utility method to be aligned with the agent based
	 * configuration.
	 * 
	 * @return The settings of this configuration.
	 */
	Map<String, String> getSettings();

}
