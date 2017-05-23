package rocks.inspectit.agent.java.eum.reflection;

import java.io.BufferedReader;
import java.util.Map;

/**
 * Reflection wrapper class for {@link javax.servlet.http.HttpServletRequest}.
 *
 * @author Jonas Kunz
 *
 */
public final class WHttpServletRequest {

	/**
	 * See {@link javax.servlet.http.HttpServletRequest}.
	 */
	private static final String CLAZZ = "javax.servlet.http.HttpServletRequest";

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getRequestURI()}.
	 */
	private static final CachedMethod<String> GET_REQUEST_URI = new CachedMethod<String>(CLAZZ, "getRequestURI");

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getParameterMap()}.
	 */
	private static final CachedMethod<Map<java.lang.String, java.lang.String[]>> GET_PARAMETER_MAP = new CachedMethod<Map<java.lang.String, java.lang.String[]>>(CLAZZ, "getParameterMap");

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getReader()}.
	 */
	private static final CachedMethod<BufferedReader> GET_READER = new CachedMethod<BufferedReader>(CLAZZ, "getReader");

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getCookies()}.
	 */
	private static final CachedMethod<Object[]> GET_COOKIES = new CachedMethod<Object[]>(CLAZZ, "getCookies");

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getCookies()}.
	 */
	private static final CachedMethod<String> GET_HEADER = new CachedMethod<String>(CLAZZ, "getHeader", String.class);

	/**
	 * the wrapped {@link javax.servlet.http.HttpServletRequest} instance.
	 */
	private Object instance;

	/**
	 * @param inst
	 *            the instance to wrap
	 */
	private WHttpServletRequest(Object inst) {
		this.instance = inst;
	}

	/**
	 * Wraps the given {@link javax.servlet.http.HttpServletRequest} object.
	 *
	 * @param request
	 *            the {@link javax.servlet.http.HttpServletRequest} instance to wrap.
	 * @return the wrapper
	 */
	public static WHttpServletRequest wrap(Object request) {
		return new WHttpServletRequest(request);
	}

	/**
	 * @param instance
	 *            the object to check
	 * @return true, if the given object is an instance of
	 *         {@link javax.servlet.http.HttpServletRequest}
	 */
	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
	}

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getRequestURI()}.
	 *
	 * @return the request URI
	 */
	public String getRequestURI() {
		return GET_REQUEST_URI.callSafe(instance);
	}

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getParameterMap()}.
	 *
	 * @return the parameter map
	 */
	public Map<java.lang.String, java.lang.String[]> getParameterMap() {
		return GET_PARAMETER_MAP.callSafe(instance);
	}

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getReader()}.
	 *
	 * @return the reader
	 */
	public BufferedReader getReader() {
		return GET_READER.callSafe(instance);
	}

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getCookies()}.
	 *
	 * @return the cookies, or null if no cookies have been set
	 */
	public Object[] getCookies() {
		return GET_COOKIES.callSafe(instance);
	}

	/**
	 * See {@link javax.servlet.http.HttpServletRequest#getHeader()}.
	 * 
	 * @param name
	 *            the name of the header
	 *
	 * @return the header value, or null if it is not present
	 */
	public String getHeader(String name) {
		return GET_HEADER.callSafe(instance, name);
	}
}
