package rocks.inspectit.agent.java.util;

/**
 * Implementation of a character ringbuffer. <br>
 * Ringbuffers are designed to be very efficient in terms of memory and speed when adding constantly
 * about as much elements to the end of the buffer as to the beginning.
 *
 * Note that operation modifing the buffer ({@link #append(CharSequence)} and {@link #erase(int)})
 * invalidates all previously aquired SubSequences as they use the same internal buffer!
 *
 * @author Jonas Kunz
 *
 */
public class CharacterRingBuffer implements CharSequence {

	/**
	 * The default initial capacity of a char ringbuffer, if nothing els is specified.
	 */
	private static final int DEFAULT_CAPACITY = 1024;

	/**
	 * The internal buffer used for storing the data.
	 */
	private char[] buffer;

	/**
	 * The position in {@link #buffer} where the first valid character is stored.
	 */
	int beginOffset;

	/**
	 * The length of the valid data stored in {@link #buffer}.
	 */
	int length; // NOPMD

	/**
	 * Creates an empty character ringbuffer.
	 */
	public CharacterRingBuffer() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Creates an empty character ringbuffer with the igven capacity.
	 *
	 * @param startingCapacity
	 *            the initial capacity of the buffer
	 */
	public CharacterRingBuffer(int startingCapacity) {
		buffer = new char[startingCapacity];
		beginOffset = 0;
		length = 0;
	}

	/**
	 * Creates a ringbuffer with the given initial contents.
	 *
	 * @param content
	 *            the intial contents of the buffer.
	 */
	public CharacterRingBuffer(CharSequence content) {
		int cap = DEFAULT_CAPACITY;
		while (cap < content.length()) {
			cap *= 2;
		}
		buffer = new char[cap];
		beginOffset = 0;
		length = 0;
		append(content);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int length() {
		return length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char charAt(int index) {
		if ((index < 0) || (index >= length)) {
			throw new IllegalArgumentException("Index out of bounds (" + index + ")");
		}
		int position = (index + beginOffset) % buffer.length;
		return buffer[position];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		if ((start < 0) || (end > length) || (start > end)) {
			throw new IllegalArgumentException("Illegal start / end index(" + start + ", " + end + ")");
		}
		return new SubSequence(start, end - start);
	}

	/**
	 * Appends the given character sequence to the end of this buffer, resizing the buffer if
	 * necessary.<br>
	 * Runtime complexity of this operation is amortised O(n) where n is the length of the String to
	 * append.
	 *
	 * @param sequence
	 *            the character sequence to append to the end of the buffer
	 */
	public final void append(CharSequence sequence) {
		int len = sequence.length();
		ensureCapacity(length + len);
		for (int pos = 0; pos < len; pos++) {
			buffer[(beginOffset + length + pos) % buffer.length] = sequence.charAt(pos);
		}
		length += len;
	}

	/**
	 * Erases the given amount of characters at the beginning of the buffer. <br>
	 * Erasing characters also moved the indices of the remaining characters, e.g. calling
	 * <code>erase(5)</code> results in the character being previously at index 5 to be moved to the
	 * index 0.
	 *
	 * @param charCount
	 *            the number of characters to erase from the beginnign of the buffer
	 */
	public final void erase(int charCount) {
		if (charCount > length) {
			throw new IllegalArgumentException("Cannot erase more chars than sotred in the buffer!");
		}
		length -= charCount;
		if (length == 0) {
			beginOffset = 0;
		} else {
			beginOffset = (beginOffset + charCount) % buffer.length;
		}
	}

	/**
	 * Ensures that the buffer has enough capacity, resizing it if necessary.
	 *
	 * @param requiredCapacity
	 *            the miniumum capacity the buffer has to store
	 */
	private void ensureCapacity(int requiredCapacity) {
		int cap = buffer.length;
		if (cap > requiredCapacity) {
			return;
		}
		while (cap <= requiredCapacity) {
			cap *= 2;
		}
		char[] newBuffer = new char[cap];
		if (length > 0) {
			int charsCountA = Math.min(beginOffset + length, buffer.length) - beginOffset;
			int charsCountB = length - charsCountA;
			System.arraycopy(buffer, beginOffset, newBuffer, 0, charsCountA);
			if (charsCountB > 0) {
				System.arraycopy(buffer, 0, newBuffer, charsCountA, charsCountB);
			}
		}
		beginOffset = 0;
		this.buffer = newBuffer;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getAsString(0, length);
	}

	/**
	 * Builds a string from the buffer contents.
	 *
	 * @param addtionalOffset
	 *            the additional offset within the buffer
	 * @param length
	 *            the number of characters
	 * @return the character data at the given indices as string
	 */
	private String getAsString(int addtionalOffset, int length) {
		int offset = beginOffset + addtionalOffset;
		if ((offset + length) <= buffer.length) {
			return String.valueOf(buffer, offset, length);
		} else {
			char[] data = new char[length];
			int splitPos = buffer.length - offset;
			System.arraycopy(buffer, offset, data, 0, splitPos);
			System.arraycopy(buffer, 0, data, splitPos, length - splitPos);
			return String.valueOf(data);
		}
	}

	/**
	 * @author Jonas Kunz
	 *
	 */
	private class SubSequence implements CharSequence {

		/**
		 * Offset within the original buffer.
		 */
		int offset;

		/**
		 * Length of the sub sequence.
		 */
		int length; // NOPMD

		/**
		 * Constructor.
		 *
		 * @param offset
		 *            the offset within the original buffer
		 * @param length
		 *            the length of the sub sequence
		 */
		SubSequence(int offset, int length) {
			super();
			this.offset = offset;
			this.length = length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int length() {
			return length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public char charAt(int index) {
			if ((index < 0) || (index >= length)) {
				throw new IllegalArgumentException("Index out of bounds (" + index + ")");
			}
			return CharacterRingBuffer.this.charAt(index + offset);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public CharSequence subSequence(int start, int end) {
			if ((start < 0) || (end > length) || (start > end)) {
				throw new IllegalArgumentException("Ilelgal start / end index(" + start + ", " + end + ")");
			}
			return new SubSequence(offset + start, end - start);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return getAsString(offset, length);
		}

	}
}
