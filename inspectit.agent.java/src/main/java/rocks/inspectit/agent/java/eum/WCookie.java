package rocks.inspectit.agent.java.eum;

/**
 * Reflection wrapper class for {@link javax.servlet.http.Cookie}.
 *
 * @author Jonas Kunz
 *
 */
public final class WCookie {

	/**
	 * see {@link javax.servlet.http.Cookie}.
	 */
	private static final String CLAZZ = "javax.servlet.http.Cookie";

	/**
	 * see {@link javax.servlet.http.Cookie#Cookie(String, String)}.
	 */
	private static final CachedConstructor CONSTRUCTOR = new CachedConstructor(CLAZZ, String.class, String.class);

	/**
	 * see {@link javax.servlet.http.Cookie#getName()}.
	 */
	private static final CachedMethod<String> GET_NAME = new CachedMethod<String>(CLAZZ, "getName");

	/**
	 * see {@link javax.servlet.http.Cookie#getValue()}.
	 */
	private static final CachedMethod<String> GET_VALUE = new CachedMethod<String>(CLAZZ, "getValue");

	/**
	 * see {@link javax.servlet.http.Cookie#setMaxAge()}.
	 */
	private static final CachedMethod<Void> SET_MAX_AGE = new CachedMethod<Void>(CLAZZ, "setMaxAge", int.class);

	/**
	 * see {@link javax.servlet.http.Cookie#setPath()}.
	 */
	private static final CachedMethod<Void> SET_PATH = new CachedMethod<Void>(CLAZZ, "setPath", String.class);

	/**
	 * The {@link javax.servlet.http.Cookie} isntance to call.
	 */
	private Object instance;

	/**
	 * @param inst
	 *            the isntance to call.
	 */
	private WCookie(Object inst) {
		this.instance = inst;
	}

	/**
	 * Wraps the given cookie for easy access.
	 *
	 * @param cookie
	 *            the {@link javax.servlet.http.Cookie} instance
	 * @return the rwapped instance.
	 */
	public static WCookie wrap(Object cookie) {
		return new WCookie(cookie);
	}


	/**
	 * @param instance
	 *            the object to check
	 * @return true, if the given object is an instance of {@link javax.servlet.http.Cookie}
	 */
	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
	}

	/**
	 * Creates a new {@link javax.servlet.http.Cookie} instance.
	 *
	 * @param cl
	 *            the classloader to use for finding the {@link javax.servlet.http.Cookie} class.
	 * @param name
	 *            the name of the cookie
	 * @param value
	 *            the value of the cookie
	 * @return the created {@link javax.servlet.http.Cookie}
	 */
	public static Object newInstance(ClassLoader cl, String name, String value) {
		return CONSTRUCTOR.newInstanceSafe(cl, name, value);
	}

	/**
	 * see {@link javax.servlet.http.Cookie#getName()}.
	 *
	 * @return the name
	 */
	public String getName() {
		return GET_NAME.callSafe(instance);
	}

	/**
	 * see {@link javax.servlet.http.Cookie#getValue()}.
	 *
	 * @return the value
	 */
	public String getValue() {
		return GET_VALUE.callSafe(instance);
	}

	/**
	 * see see {@link javax.servlet.http.Cookie#setMaxAge(int)}.
	 *
	 * @param maxAgeSeconds
	 *            the maxmimum age in seconds
	 */
	public void setMaxAge(int maxAgeSeconds) {
		SET_MAX_AGE.callSafe(instance, maxAgeSeconds);
	}

	/**
	 * see {@link javax.servlet.http.Cookie#setPath(String)}.
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		SET_PATH.callSafe(instance, path);
	}

}
