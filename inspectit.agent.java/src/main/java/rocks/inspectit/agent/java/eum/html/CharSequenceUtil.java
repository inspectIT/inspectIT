package rocks.inspectit.agent.java.eum.html;

/**
 * Utility class for working with CHarSequences.
 *
 * @author Jonas Kunz
 *
 */
public final class CharSequenceUtil {

	/**
	 * private constructor of utiltiy class.
	 */
	private CharSequenceUtil() {

	}

	/**
	 * Performs an case insensitive comparison of the substrings within the given CharSequences.
	 *
	 * @param a
	 *            the first character sequence
	 * @param offsetA
	 *            the offset within the charsequence a
	 * @param lenA
	 *            the length of the substring to compare
	 * @param b
	 *            the second character sequence
	 * @param offsetB
	 *            the offset within the charsequence b
	 * @param lenB
	 *            the length of the substring to compare
	 * @return true, if the given subsequences match when ignoring the case
	 */
	public static boolean checkEqualIgnoreCase(CharSequence a, int offsetA, int lenA, CharSequence b, int offsetB, int lenB) {
		if ((lenA != lenB) || ((a.length() - offsetA) < lenA) || ((b.length() - offsetB) < lenB)) {
			return false;
		}
		for (int i = 0; i < lenA; i++) {
			if (Character.toUpperCase(a.charAt(offsetA + i)) != Character.toUpperCase(b.charAt(offsetB + i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs an case sensitive comparison of the substrings within the given CharSequences.
	 *
	 * @param a
	 *            the first character sequence
	 * @param offsetA
	 *            the offset within the charsequence a
	 * @param lenA
	 *            the length of the substring to compare
	 * @param b
	 *            the second character sequence
	 * @param offsetB
	 *            the offset within the charsequence b
	 * @param lenB
	 *            the length of the substring to compare
	 * @return true, if the given subsequences match when checking the case
	 */
	public static boolean checkEqualCheckCase(CharSequence a, int offsetA, int lenA, CharSequence b, int offsetB, int lenB) {
		if ((lenA != lenB) || ((a.length() - offsetA) < lenA) || ((b.length() - offsetB) < lenB)) {
			return false;
		}
		for (int i = 0; i < lenA; i++) {
			if (a.charAt(offsetA + i) != b.charAt(offsetB + i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs a case sensitive comparison of the given substrings.
	 *
	 * @param a
	 *            the first sequence
	 * @param b
	 *            the second sequence
	 * @return true, if both character sequences are exactly equal, including the case
	 */
	public static boolean checkEqualCheckCase(CharSequence a, CharSequence b) {
		return checkEqualCheckCase(a, 0, a.length(), b, 0, b.length());
	}

	/**
	 * Performs a case insensitive comparison of the given substrings.
	 *
	 * @param a
	 *            the first sequence
	 * @param b
	 *            the second sequence
	 * @return true, if both character sequences are equal except for the case
	 */
	public static boolean checkEqualIgnoreCase(CharSequence a, CharSequence b) {
		return checkEqualIgnoreCase(a, 0, a.length(), b, 0, b.length());
	}
}
