package rocks.inspectit.agent.java.eum.html;

/**
 *
 * Utiltiy method for performing some text searching on a given text. A caret just represents a
 * position within this text and can be moved around.
 *
 * @author Jonas Kunz
 */
public class Caret {

	/**
	 * The text this caret opperates on.
	 */
	private CharSequence src;

	/**
	 * The current position of the caret, as an offset from the text beginning.
	 */
	private int offset;

	/**
	 * Creates a new caret.
	 *
	 * @param src
	 *            the sourcetext
	 * @param offset
	 *            the caret position
	 */
	public Caret(CharSequence src, int offset) {
		this.src = src;
		this.offset = offset;
	}

	/**
	 * Creates a caret pointing to the beginning of the given text.
	 *
	 * @param src
	 *            the text
	 */
	public Caret(CharSequence src) {
		this(src, 0);
	}

	/**
	 * Copy-Constructor.
	 *
	 * @param copyOf
	 *            the caret to copy.
	 */
	public Caret(Caret copyOf) {
		this.src = copyOf.src;
		this.offset = copyOf.offset;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * Moves the caret n characters.
	 *
	 * @param n
	 *            the number of characters to move (negative values for walking backwards)
	 */
	public void goN(int n) {
		offset = Math.min(src.length(), Math.max(0, offset + n));

	}

	/**
	 * returns the character at the given offset from this caret.
	 *
	 * @param additionOff
	 *            the additional offset
	 * @return the character at (caret-position + additionOff)
	 */
	public char get(int additionOff) {
		return src.charAt(offset + additionOff);
	}

	/**
	 * @return the number of "steps" the caret has to walk until it points behind the last
	 *         character.
	 */
	public int wayToEnd() {
		return src.length() - offset;
	}

	/**
	 * @return an identical copy of this caret, identical to calling the copy constructor.
	 */
	public Caret copy() {
		return new Caret(this);
	}

	/**
	 * Walks forward with the caret until it points at the given character. Case-Sensitive
	 * comparison is used.
	 *
	 * @param c
	 *            the character to walk to
	 * @return true if the character was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToCharCheckCase(char c) {
		while (!endReached() && (src.charAt(offset) != c)) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the caret until it points at the given character. Then advances the
	 * position by one. Case-Sensitive comparison is used.
	 *
	 * @param c
	 *            the character to walk to
	 * @return true if the character was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterCharCheckCase(char c) {
		boolean found = walkToCharCheckCase(c);
		if (found) {
			offset++;
		}
		return found;
	}

	/**
	 * Walks forward with the caret until it points at a position which matches the given string.
	 * Case-Insensitive comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToMatchIgnoreCase(String strToMatch) {
		while (!endReached() && !startsWithIgnoreCase(strToMatch)) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the caret until it points at a position which matches the given string.
	 * Case-sensitive comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToMatchCheckCase(String strToMatch) {
		while (!endReached() && !startsWithCheckCase(strToMatch)) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the caret until it points at a position which matches the given string.
	 * Then advances the position after this string. Case-Insensitive comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterMatchIgnoreCase(String strToMatch) {
		boolean found = walkToMatchIgnoreCase(strToMatch);
		if (found) {
			offset += strToMatch.length();
		}
		return found;
	}

	/**
	 * Walks forward with the caret until it points at a position which matches the given string.
	 * Then advances the position after this string. Case-Sensitive comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterMatchCheckCase(String strToMatch) {
		boolean found = walkToMatchCheckCase(strToMatch);
		if (found) {
			offset += strToMatch.length();
		}
		return found;
	}

	/**
	 * Checks if the substring of the source text starting at the carets position starts with the
	 * give String. Case-Insensitive Comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if matched
	 */
	public boolean startsWithIgnoreCase(String strToMatch) {
		int len = strToMatch.length();
		if (len > wayToEnd()) {
			return false;
		}
		return CharSequenceUtils.checkEqualIgnoreCase(src, offset, len, strToMatch, 0, len);
	}

	/**
	 * Checks if the substring of the source text starting at the carets position starts with the
	 * give String. Case-Sensitive Comparison is used.
	 *
	 * @param strToMatch
	 *            the string to match
	 * @return true if matched
	 */
	public boolean startsWithCheckCase(String strToMatch) {
		int len = strToMatch.length();
		if (len > wayToEnd()) {
			return false;
		}
		return CharSequenceUtils.checkEqualCheckCase(src, offset, len, strToMatch, 0, len);
	}

	/**
	 * Walks backwards until the caret points at a non-whitespace character.
	 */
	public void walkBackBeforeWhitespaces() {
		while ((offset > 0) && Character.isWhitespace(src.charAt(offset))) {
			offset--;
		}
	}

	/**
	 * Walks forward until the caret points at a non-whitespace character.
	 */
	public void walkAfterWhitespaces() {
		while (!endReached() && Character.isWhitespace(src.charAt(offset))) {
			offset++;
		}
	}

	/**
	 * Walks forward until the caret points at a whitespace character.
	 *
	 * @return true if a whitespace character was found
	 */
	public boolean walkToWhitespace() {
		while (!endReached() && !Character.isWhitespace(src.charAt(offset))) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * @return true if the end was reached (the caret points after the last character)
	 *
	 */
	public boolean endReached() {
		return offset == src.length();
	}

	/**
	 * Moves the caret to the given position.
	 *
	 * @param offset2
	 *            the offset from the beginning of the text
	 */
	public void goTo(int offset2) {
		goN(offset2 - offset);
	}

	@Override
	public String toString() {
		return "Caret [..." + src.subSequence(Math.max(offset - 100, 0), offset) + "~C~" + src.subSequence(offset, Math.min(offset + 100, src.length())) + "]";
	}

}
