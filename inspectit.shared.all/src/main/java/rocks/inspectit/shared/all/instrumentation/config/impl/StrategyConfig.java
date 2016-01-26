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
	private String clazzName;

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
	 * @param clazzName
	 *            The fully qualified class name.
	 * @param settings
	 *            Additional settings stored in a map.
	 */
	public StrategyConfig(String clazzName, Map<String, String> settings) {
		this.clazzName = clazzName;
		this.settings = settings;
	}

	/**
	 * Returns the fully qualified class name of this configuration.
	 *
	 * @return The fully qualified class name of this configuration.
	 */
	public String getClazzName() {
		return clazzName;
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
