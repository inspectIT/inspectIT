package rocks.inspectit.agent.java.sdk.opentracing.internal.util;

import java.lang.reflect.Field;
import java.util.Random;

import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerLoggerWrapper;

/**
 * Helper class for generating random numbers needed for the tracing. Currently works with
 * {@link java.util.concurrent.ThreadLocalRandom} if java version is 1.7 and higher, as it's
 * expected that there is contention when creating ids. If we are in Java 6 we'll use normal random
 * that is thread-safe but can be slower.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("unchecked")
public final class RandomUtils {

	/**
	 * {@link TracerLogger} of this class.
	 */
	private final static TracerLogger LOGGER = TracerLoggerWrapper.getTraceLogger(RandomUtils.class);

	/**
	 * Random to use.
	 */
	private static final Random RANDOM = new Random();

	/**
	 * LocalRandom field in the java.util.concurrent.ThreadLocalRandom class.
	 */
	private static ThreadLocal<Random> threadLocalRandomLocal;

	static {
		// try to load thread local random
		try {
			Class<?> clazz = Class.forName("java.util.concurrent.ThreadLocalRandom");
			Field field = clazz.getDeclaredField("localRandom");
			field.setAccessible(true);
			threadLocalRandomLocal = (ThreadLocal<Random>) field.get(null);
		} catch (Exception e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("ThreadLocalRandom is not available. Using " + RANDOM.getClass().getSimpleName() + " for generating random numbers.");
			}
		}
	}

	/**
	 * Private constructor.
	 */
	private RandomUtils() {
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
		//@formatter:off
		return ((randomBytes[0] & 0xffL) << 56)
				| ((randomBytes[1] & 0xffL) << 48)
				| ((randomBytes[2] & 0xffL) << 40)
				| ((randomBytes[3] & 0xffL) << 32)
				| ((randomBytes[4] & 0xffL) << 24)
				| ((randomBytes[5] & 0xffL) << 16)
				| ((randomBytes[6] & 0xffL) << 8)
				| (randomBytes[7] & 0xffL);
		//@formatter:on
	}

	/**
	 * Returns random to use when generating random ids.
	 *
	 * @return Returns random to use when generating random ids.
	 */
	private static Random getRandom() {
		if (null != threadLocalRandomLocal) {
			return threadLocalRandomLocal.get();
		}
		return RANDOM;
	}
}
