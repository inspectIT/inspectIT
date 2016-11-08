package rocks.inspectit.agent.java.util;

/**
 * Small utility class to help us distinguish our classes from the rest.
 *
 * @author Ivan Senic
 *
 */
public final class ClassLoadingUtil {

	/**
	 * Our classes start with {@value #CLASS_NAME_PREFIX}.
	 */
	private static final String CLASS_NAME_PREFIX = "rocks.inspectit";

	/**
	 * Private constructor.
	 */
	private ClassLoadingUtil() {
	}

	/**
	 * Returns if the class name starts with {@value #CLASS_NAME_PREFIX}.
	 *
	 * @param className
	 *            Class name
	 * @return Returns if the class name starts with {@value #CLASS_NAME_PREFIX}.
	 */
	public static boolean isInspectITClass(String className) {
		return className.startsWith(CLASS_NAME_PREFIX);
	}
}
