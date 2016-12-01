package rocks.inspectit.shared.all.pattern;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.util.CollectionUtils;

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

	/**
	 * Returns the pattern with exceptions.
	 *
	 * @param txt
	 *            Text to match
	 * @param exceptions
	 *            Exception text not to match
	 * @return Correct {@link IMatchPattern} based on the given input.
	 * @see ExceptionalMatchPattern
	 */
	public static IMatchPattern getPattern(String txt, Collection<String> exceptions) {
		IMatchPattern mainPattern = getPattern(txt);
		if (CollectionUtils.isEmpty(exceptions)) {
			return mainPattern;
		} else {
			Collection<IMatchPattern> exceptionPatterns = new ArrayList<IMatchPattern>(exceptions.size());
			for (String exception : exceptions) {
				exceptionPatterns.add(getPattern(exception));
			}
			return new ExceptionalMatchPattern(mainPattern, exceptionPatterns);
		}
	}
}
