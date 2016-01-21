package info.novatec.inspectit.rcp.model;

/**
 * A support class providing static methods and constants for access modifiers such as public,
 * private, ...
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="http://jboss-javassist.github.io/javassist/">javassist</a>. Original author is Shigeru
 * Chiba. License info can be found
 * <a href="https://github.com/jboss-javassist/javassist/blob/master/License.html">here</a>.
 * 
 */
public final class AccessFlag {
	public static final int PUBLIC = 0x0001; // NOCHK
	public static final int PRIVATE = 0x0002; // NOCHK
	public static final int PROTECTED = 0x0004; // NOCHK
	public static final int STATIC = 0x0008; // NOCHK
	public static final int FINAL = 0x0010; // NOCHK
	public static final int SYNCHRONIZED = 0x0020; // NOCHK
	public static final int VOLATILE = 0x0040; // NOCHK
	public static final int BRIDGE = 0x0040; // for method_info NOCHK
	public static final int TRANSIENT = 0x0080; // NOCHK
	public static final int VARARGS = 0x0080; // for method_info NOCHK
	public static final int NATIVE = 0x0100; // NOCHK
	public static final int INTERFACE = 0x0200; // NOCHK
	public static final int ABSTRACT = 0x0400; // NOCHK
	public static final int STRICT = 0x0800; // NOCHK
	public static final int SYNTHETIC = 0x1000; // NOCHK
	public static final int ANNOTATION = 0x2000; // NOCHK
	public static final int ENUM = 0x4000; // NOCHK

	public static final int SUPER = 0x0020; // NOCHK

	/**
	 * private constructor.
	 */
	private AccessFlag() {
	}

	// Note: 0x0020 is assigned to both ACC_SUPER and ACC_SYNCHRONIZED
	// although java.lang.reflect.Modifier does not recognize ACC_SUPER.
}