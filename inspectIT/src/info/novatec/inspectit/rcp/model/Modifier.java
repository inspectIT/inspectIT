package info.novatec.inspectit.rcp.model;

/**
 * The Modifier class provides static methods and constants to decode class and member access
 * modifiers. The constant values are equivalent to the corresponding values in {@link AccessFlag}.
 * <p>
 * All the methods/constants in this class are compatible with ones in
 * <code>java.lang.reflect.Modifier</code>.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="http://jboss-javassist.github.io/javassist/">javassist</a>. Original author is Shigeru
 * Chiba. License info can be found
 * <a href="https://github.com/jboss-javassist/javassist/blob/master/License.html">here</a>.
 */
public final class Modifier {
	public static final int PUBLIC = AccessFlag.PUBLIC; // NOCHK
	public static final int PRIVATE = AccessFlag.PRIVATE; // NOCHK
	public static final int PROTECTED = AccessFlag.PROTECTED;// NOCHK
	public static final int STATIC = AccessFlag.STATIC;// NOCHK
	public static final int FINAL = AccessFlag.FINAL;// NOCHK
	public static final int SYNCHRONIZED = AccessFlag.SYNCHRONIZED;// NOCHK
	public static final int VOLATILE = AccessFlag.VOLATILE;// NOCHK
	public static final int TRANSIENT = AccessFlag.TRANSIENT;// NOCHK
	public static final int NATIVE = AccessFlag.NATIVE;// NOCHK
	public static final int INTERFACE = AccessFlag.INTERFACE;// NOCHK
	public static final int ABSTRACT = AccessFlag.ABSTRACT;// NOCHK
	public static final int STRICT = AccessFlag.STRICT;// NOCHK
	public static final int ANNOTATION = AccessFlag.ANNOTATION;// NOCHK
	public static final int ENUM = AccessFlag.ENUM;// NOCHK

	/**
	 * Private constructor for utility class.
	 */
	private Modifier() {
	}

	/**
	 * Returns true if the modifiers include the <tt>public</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>public</tt> modifier.
	 */
	public static boolean isPublic(int mod) {
		return (mod & PUBLIC) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>private</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>private</tt> modifier.
	 */
	public static boolean isPrivate(int mod) {
		return (mod & PRIVATE) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>protected</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>protected</tt> modifier.
	 */
	public static boolean isProtected(int mod) {
		return (mod & PROTECTED) != 0;
	}

	/**
	 * Returns true if the modifiers do not include either <tt>public</tt>, <tt>protected</tt>, or
	 * <tt>private</tt>.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers do not include either <tt>public</tt>, <tt>protected</tt>, or
	 *         <tt>private</tt>.
	 */
	public static boolean isPackage(int mod) {
		return (mod & (PUBLIC | PRIVATE | PROTECTED)) == 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>static</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>static</tt> modifier.
	 */
	public static boolean isStatic(int mod) {
		return (mod & STATIC) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>final</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>final</tt> modifier.
	 */
	public static boolean isFinal(int mod) {
		return (mod & FINAL) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>synchronized</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>synchronized</tt> modifier.
	 */
	public static boolean isSynchronized(int mod) {
		return (mod & SYNCHRONIZED) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>volatile</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>volatile</tt> modifier.
	 */
	public static boolean isVolatile(int mod) {
		return (mod & VOLATILE) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>transient</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>transient</tt> modifier.
	 */
	public static boolean isTransient(int mod) {
		return (mod & TRANSIENT) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>native</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>native</tt> modifier.
	 */
	public static boolean isNative(int mod) {
		return (mod & NATIVE) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>interface</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>interface</tt> modifier.
	 */
	public static boolean isInterface(int mod) {
		return (mod & INTERFACE) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>annotation</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>annotation</tt> modifier.
	 */
	public static boolean isAnnotation(int mod) {
		return (mod & ANNOTATION) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>enum</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>enum</tt> modifier.
	 */
	public static boolean isEnum(int mod) {
		return (mod & ENUM) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>abstract</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>abstract</tt> modifier.
	 */
	public static boolean isAbstract(int mod) {
		return (mod & ABSTRACT) != 0;
	}

	/**
	 * Returns true if the modifiers include the <tt>strictfp</tt> modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return true if the modifiers include the <tt>strictfp</tt> modifier.
	 */
	public static boolean isStrict(int mod) {
		return (mod & STRICT) != 0;
	}

	/**
	 * Return a string describing the access modifier flags in the specified modifier.
	 * 
	 * @param mod
	 *            modifier flags.
	 * @return a string describing the access modifier flags in the specified modifier.
	 */
	public static String toString(int mod) {
		return java.lang.reflect.Modifier.toString(mod);
	}

}