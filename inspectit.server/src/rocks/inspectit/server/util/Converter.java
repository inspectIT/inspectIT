package info.novatec.inspectit.cmr.util;

/**
 * Converter utility class.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class Converter {

	/**
	 * Nano seconds to milliseconds value.
	 */
	private static final double NANO_TO_MS_VALUE = 1000000.0d;

	/**
	 * Private constructor prevents instantiation. This is just a utility class.
	 */
	private Converter() {
	}

	/**
	 * Converts the nano seconds into milliseconds.
	 * 
	 * @param nanoTime
	 *            The nano time.
	 * @return Returns the milliseconds.
	 */
	public static double nanoToMilliseconds(long nanoTime) {
		return nanoTime / NANO_TO_MS_VALUE;
	}

}
