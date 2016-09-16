package rocks.inspectit.agent.java.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Helps with mapping of primtive types to their Boxed class and backwards.
 *
 * @author Jonas Kunz
 *
 */
@SuppressWarnings({ "PMD", "short" })
public final class AutoboxingHelper {

	/**
	 * A map mapping the primtive names e.g. "int" to their classes, e.g. int.class.
	 */
	private static final Map<String, Class<?>> PRIMITIVE_TYPE_CLASSES = new HashMap<String, Class<?>>();
	/**
	 * A map mapping the primtive names e.g. "int" to their boxed classes, e.g. java.lang.Integer.
	 */
	private static final Map<String, Class<?>> PRIMITIVE_TYPE_WRAPPERS = new HashMap<String, Class<?>>();


	static {
		PRIMITIVE_TYPE_CLASSES.put(byte.class.getName(), byte.class);
		PRIMITIVE_TYPE_CLASSES.put(short.class.getName(), short.class);
		PRIMITIVE_TYPE_CLASSES.put(int.class.getName(), int.class);
		PRIMITIVE_TYPE_CLASSES.put(long.class.getName(), long.class);
		PRIMITIVE_TYPE_CLASSES.put(float.class.getName(), float.class);
		PRIMITIVE_TYPE_CLASSES.put(double.class.getName(), double.class);
		PRIMITIVE_TYPE_CLASSES.put(boolean.class.getName(), boolean.class);
		PRIMITIVE_TYPE_CLASSES.put(char.class.getName(), char.class);

		PRIMITIVE_TYPE_WRAPPERS.put(byte.class.getName(), Byte.class);
		PRIMITIVE_TYPE_WRAPPERS.put(short.class.getName(), Short.class);
		PRIMITIVE_TYPE_WRAPPERS.put(int.class.getName(), Integer.class);
		PRIMITIVE_TYPE_WRAPPERS.put(long.class.getName(), Long.class);
		PRIMITIVE_TYPE_WRAPPERS.put(float.class.getName(), Float.class);
		PRIMITIVE_TYPE_WRAPPERS.put(double.class.getName(), Double.class);
		PRIMITIVE_TYPE_WRAPPERS.put(boolean.class.getName(), Boolean.class);
		PRIMITIVE_TYPE_WRAPPERS.put(char.class.getName(), Character.class);
	}

	/**
	 * never called.
	 */
	private AutoboxingHelper() {
	}

	/**
	 * Returns the class object for normal types. Return the primitive type for int,long,etc (the
	 * Integer.TYPE and so on)
	 *
	 * @param name
	 *            fqn of the class or the primitive name
	 * @param initialize
	 *            specifies whether the class shall be initialized when found.
	 * @param classloader
	 *            the classlaoder to start searching from
	 * @throws ClassNotFoundException
	 *             if the class does not exist.
	 *
	 * @return the class instance, if it was found
	 */
	public static Class<?> findClass(String name, boolean initialize, ClassLoader classloader) throws ClassNotFoundException {
		if (isPrimitiveType(name)) {
			return PRIMITIVE_TYPE_CLASSES.get(name);
		} else {
			return Class.forName(name, initialize, classloader);
		}
	}

	/**
	 * Checks if the given class name is a primitive type, e.g "int".
	 *
	 * @param name
	 *            the name of the class or primitive.
	 * @return true, if the name does not reference a class but a primitive
	 */
	public static boolean isPrimitiveType(String name) {
		return PRIMITIVE_TYPE_CLASSES.containsKey(name);
	}

	/**
	 * Returns true if the name equals the name of the primitive "void" return type (NOT THE
	 * Void.class).
	 *
	 * @param name
	 *            the name to check against "void"
	 * @return true, if it is void
	 */
	public static boolean isVoid(String name) {
		return void.class.getName().equals(name);
	}

	/**
	 * @param primName
	 *            the name of the primtive
	 * @return the wrapper class for the given primitive type, e.g. "Integer" for int
	 */
	public static Class<?> getWrapperClass(String primName) {
		return PRIMITIVE_TYPE_WRAPPERS.get(primName);
	}

	/**
	 * @param type
	 *            the type to check
	 * @return the wrapper class if type is a primitive type, null otherwise
	 */
	public static Class<?> getWrapperClass(Class<?> type) {
		return getWrapperClass(type.getName());
	}

}
