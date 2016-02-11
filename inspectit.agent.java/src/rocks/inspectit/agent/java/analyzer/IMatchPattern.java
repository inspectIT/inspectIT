package info.novatec.inspectit.agent.analyzer;

import info.novatec.inspectit.agent.config.IConfigurationReader;

/**
 * The interface is used by some {@link IConfigurationReader} to try to match a given pattern to a
 * string via {@link #match(String)}. The {@link #getPattern()} just returns the used pattern.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IMatchPattern {

	/**
	 * Comparison test.
	 * 
	 * @param match
	 *            text to be matched against template
	 * @return If the test was successful.
	 */
	boolean match(String match);

	/**
	 * Returns the pattern.
	 * 
	 * @return The pattern
	 */
	String getPattern();

}
