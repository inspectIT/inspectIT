package rocks.inspectit.shared.all.tracing.util;

import java.util.Random;

import rocks.inspectit.shared.all.util.UnderlyingSystemInfo;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo.JavaVersion;

/**
 * Our helper class for generating random numbers/strings needed for the tracing. Currently works
 * with {@link java.util.concurrent.ThreadLocalRandom} if java version is 1.7 and higher, as it's
 * expected that there is contention when creating ids. If we are in Java 6 we'll use normal random
 * that is thread-safe but can be slower.
 *
 * @author Ivan Senic
 *
 */
public final class RandomUtils {

	/**
	 * Random to use.
	 */
	private static Random random = new Random();

	static {
		if (UnderlyingSystemInfo.JAVA_VERSION.compareTo(JavaVersion.JAVA_1_6) > 0) {
			// try to load thread local random
			try {
				random = (Random) Class.forName("java.util.concurrent.ThreadLocalRandom").newInstance();
			} catch (Exception e) { // NOPMD
				// ignore
			}
		}
	}

	/**
	 * Hex array.
	 */
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	/**
	 * Private constructor.
	 */
	private RandomUtils() {
	}

	/**
	 * Returns the 64-bit hex {@link String}.
	 *
	 * @return Returns the 64-bit hex {@link String}, based on the {@link Random} returned by the
	 *         {@link #getRandom()}.
	 */
	public static String randomHexString() {
		byte[] randomBytes = new byte[8];
		getRandom().nextBytes(randomBytes);
		char[] hexChars = new char[16];
		for (int j = 0; j < randomBytes.length; j++) {
			int v = randomBytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[(j * 2) + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Returns the random long number. Unlike {@link Random#nextLong()} method, this utility method
	 * may return all possible long values (64-bit spread, compared to 48-bit spread defined by the
	 * {@link Random} interface).
	 *
	 * @return Random long number, based on the {@link Random} returned by the {@link #getRandom()}.
	 */
	public static long randomLong() {
		byte[] randomBytes = new byte[8];
		getRandom().nextBytes(randomBytes);
		return ((randomBytes[0] & 0xffL) << 56)
				| ((randomBytes[1] & 0xffL) << 48)
				| ((randomBytes[2] & 0xffL) << 40)
				| ((randomBytes[3] & 0xffL) << 32)
				| ((randomBytes[4] & 0xffL) << 24)
				| ((randomBytes[5] & 0xffL) << 16)
				| ((randomBytes[6] & 0xffL) << 8)
				| (randomBytes[7] & 0xffL);
	}

	/**
	 * Returns random to use when generating random ids.
	 *
	 * @return Returns random to use when generating random ids.
	 */
	private static Random getRandom() {
		return random;
	}
}
