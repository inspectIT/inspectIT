package rocks.inspectit.shared.all.pattern;

/**
 * Pattern factory that can be used to retrieve correct {@link IMatchPattern}.
 * 
 * @author Ivan Senic
 * 
 */
public final class PatternFactory {

	/**
	 * Private constructor for factory.
	 */
	private PatternFactory() {
	}

	/**
	 * Returns the correct {@link IMatchPattern} for the given text.
	 * 
	 * @param txt
	 *            Text
	 * @return Correct {@link IMatchPattern} based on the given text.
	 */
	public static IMatchPattern getPattern(String txt) {
		if (WildcardMatchPattern.isPattern(txt)) {
			return new WildcardMatchPattern(txt);
		} else {
			return new EqualsMatchPattern(txt);
		}
	}
}
