package rocks.inspectit.shared.all.util;

/**
 * This is a utility class that provides the {@link #binarySearch(long[], int, int, long)} method
 * that is not available in Java5.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/collections/index.html">Java
 * Collections Framework</a> {@link java.util.Arrays} class. Original authors are Josh Bloch, Neal
 * Gafter and John Rose. License info can be found
 * <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html">here</a>.
 * 
 * @author Josh Bloch
 * @author Neal Gafter
 * @author John Rose
 * 
 */
public final class ArrayUtil {

	/**
	 * Private constructor.
	 */
	private ArrayUtil() {
	}

	/**
	 * Searches a range of the specified array of longs for the specified value using the binary
	 * search algorithm. The range must be sorted (as by the {@link #sort(long[], int, int)} method)
	 * prior to making this call. If it is not sorted, the results are undefined. If the range
	 * contains multiple elements with the specified value, there is no guarantee which one will be
	 * found.
	 * 
	 * @param a
	 *            the array to be searched
	 * @param fromIndex
	 *            the index of the first element (inclusive) to be searched
	 * @param toIndex
	 *            the index of the last element (exclusive) to be searched
	 * @param key
	 *            the value to be searched for
	 * @return index of the search key, if it is contained in the array within the specified range;
	 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is
	 *         defined as the point at which the key would be inserted into the array: the index of
	 *         the first element in the range greater than the key, or <tt>toIndex</tt> if all
	 *         elements in the range are less than the specified key. Note that this guarantees that
	 *         the return value will be &gt;= 0 if and only if the key is found.
	 */
	public static int binarySearch(long[] a, int fromIndex, int toIndex, long key) {
		rangeCheck(a.length, fromIndex, toIndex);
		return binarySearch0(a, fromIndex, toIndex, key);
	}

	/**
	 * Checks that {@code fromIndex} and {@code toIndex} are in the range and throws an appropriate
	 * exception, if they aren't.
	 * 
	 * @param length
	 *            the length of the array.
	 * @param fromIndex
	 *            the starting index.
	 * @param toIndex
	 *            the end index.
	 */
	private static void rangeCheck(int length, int fromIndex, int toIndex) {
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
		if (fromIndex < 0) {
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if (toIndex > length) {
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
	}

	/**
	 * Like public version, but without range checks.
	 * 
	 * @param a
	 *            the array.
	 * @param fromIndex
	 *            the from index.
	 * @param toIndex
	 *            the end index.
	 * @param key
	 *            the key to search for.
	 * @return the index.
	 */
	private static int binarySearch0(long[] a, int fromIndex, int toIndex, long key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			long midVal = a[mid];

			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -(low + 1); // key not found.
	}
}
