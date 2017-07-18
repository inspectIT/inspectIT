package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.Map;

/**
 * Class used by the {@link ConfigurationStorage} to store the information of sending strategies or
 * the buffer strategy.
 *
 * @author Patrice Bouillet
 *
 */
public class StrategyConfig {

	/**
	 * The fully qualified class name.
	 */
	private String className;

	/**
	 * Additional settings stored in a map.
	 */
	private Map<String, String> settings;

	/**
	 * No-arg constructor for serialization.
	 */
	public StrategyConfig() {
	}

	/**
	 * Default constructor accepting 2 parameters.
	 *
	 * @param className
	 *            The fully qualified class name.
	 * @param settings
	 *            Additional settings stored in a map.
	 */
	public StrategyConfig(String className, Map<String, String> settings) {
		this.className = className;
		this.settings = settings;
	}

	/**
	 * Returns the fully qualified class name of this configuration.
	 *
	 * @return The fully qualified class name of this configuration.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the settings of this configuration.
	 *
	 * @return The settings of this configuration.
	 */
	public Map<String, String> getSettings() {
		return settings;
	}

}
