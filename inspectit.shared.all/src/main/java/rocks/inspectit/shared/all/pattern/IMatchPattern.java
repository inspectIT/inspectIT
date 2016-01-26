package rocks.inspectit.shared.all.pattern;


/**
 * The interface is used to try to match a given pattern to a string via {@link #match(String)}. The
 * {@link #getPattern()} just returns the used pattern.
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
	 * Returns the string representation of this pattern.
	 *
	 * @return Returns the string representation of this pattern.
	 */
	String getPattern();

}
