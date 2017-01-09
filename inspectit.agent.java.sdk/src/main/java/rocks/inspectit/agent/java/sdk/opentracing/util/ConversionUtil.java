package rocks.inspectit.agent.java.sdk.opentracing.util;

/**
 * Conversion utils we need for our tracing.
 *
 * @author Ivan Senic
 *
 */
public final class ConversionUtil {

	/**
	 * Private constructor.
	 */
	private ConversionUtil() {
	};

	/**
	 * Parses a hexadecimal string. If passed string is <code>null</code>, then <code>0</code> will
	 * be returned.
	 *
	 * @param s
	 *            String to parse.
	 * @return parsed long value or <code>0</code> if given string is <code>null</code>
	 * @see Long#parseLong(String, int)
	 */
	public static long parseHexStringSafe(String s) {
		if (null == s) {
			return 0;
		}
		return Long.parseLong(s, 16);
	}

	/**
	 * Converts long to hexadecimal string.
	 *
	 * @param l
	 *            value
	 * @return hexadecimal representation of given long
	 *
	 * @see Long#toHexString(long)
	 */
	public static String toHexString(long l) {
		return Long.toString(l, 16);
	}
}
