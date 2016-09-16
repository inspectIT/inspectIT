package rocks.inspectit.agent.java.eum.reflection;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Reflection wrapper for {@link javax.servlet.http.HttpServletResponse}.
 *
 * @author Jonas Kunz
 *
 */
public final class WHttpServletResponse {

	/**
	 * See {@link javax.servlet.http.HttpServletResponse}.
	 */
	private static final String CLAZZ = "javax.servlet.http.HttpServletResponse";

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setStatus(int)}.
	 */
	private static final CachedMethod<Void> SET_STATUS = new CachedMethod<Void>(CLAZZ, "setStatus", int.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setContentType(String)}.
	 */
	private static final CachedMethod<Void> SET_CONTENT_TYPE = new CachedMethod<Void>(CLAZZ, "setContentType", String.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#getContentType()}.
	 */
	private static final CachedMethod<String> GET_CONTENT_TYPE = new CachedMethod<String>(CLAZZ, "getContentType");

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setContentLength(int)}.
	 */
	private static final CachedMethod<Void> SET_CONTENT_LENGTH = new CachedMethod<Void>(CLAZZ, "setContentLength", int.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setContentLength(long)}.
	 */
	private static final CachedMethod<Void> SET_CONTENT_LENGTH_LONG = new CachedMethod<Void>(CLAZZ, "setContentLengthLong", long.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setCharacterEncoding(String)}.
	 */
	private static final CachedMethod<Void> SET_CHARACTER_ENCODING = new CachedMethod<Void>(CLAZZ, "setCharacterEncoding", String.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#getCharacterEncoding()}.
	 */
	private static final CachedMethod<String> GET_CHRACTER_ENCODING = new CachedMethod<String>(CLAZZ, "getCharacterEncoding");

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setLocale(Locale)}.
	 */
	private static final CachedMethod<Void> SET_LOCALE = new CachedMethod<Void>(CLAZZ, "setLocale", Locale.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#getWriter()}.
	 */
	private static final CachedMethod<PrintWriter> GET_WRITER = new CachedMethod<PrintWriter>(CLAZZ, "getWriter");

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#getOutputStream()}.
	 */
	private static final CachedMethod<OutputStream> GET_OUTPUT_STREAM = new CachedMethod<OutputStream>(CLAZZ, "getOutputStream");

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)}.
	 */
	private static final CachedMethod<Void> ADD_COOKIE = new CachedMethod<Void>(CLAZZ, "addCookie", "javax.servlet.http.Cookie");

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#addHeader(String, String)}.
	 */
	private static final CachedMethod<Void> ADD_HEADER = new CachedMethod<Void>(CLAZZ, "addHeader", String.class, String.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#addIntHeader(String, int)}.
	 */
	private static final CachedMethod<Void> ADD_INT_HEADER = new CachedMethod<Void>(CLAZZ, "addHeader", String.class, int.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setHeader(String, String)}.
	 */
	private static final CachedMethod<Void> SET_HEADER = new CachedMethod<Void>(CLAZZ, "setHeader", String.class, String.class);

	/**
	 * See {@link javax.servlet.http.HttpServletResponse#setIntHeader(String, int)}.
	 */
	private static final CachedMethod<Void> SET_INT_HEADER = new CachedMethod<Void>(CLAZZ, "setHeader", String.class, int.class);

	/**
	 * The wrapped {@link javax.servlet.http.HttpServletResponse} instance.
	 */
	private Object instance;

	/**
	 * @param inst
	 *            the instance to wrap
	 */
	private WHttpServletResponse(Object inst) {
		this.instance = inst;
	}

	/**
	 * @param instance
	 *            the object to check
	 * @return true, if the given object is an instance of
	 *         {@link javax.servlet.http.HttpServletResponse}
	 */
	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
	}

	/**
	 * @param request
	 *            the {@link javax.servlet.http.HttpServletResponse} instance to wrap
	 * @return the wrapped instance.
	 */
	public static WHttpServletResponse wrap(Object request) {
		return new WHttpServletResponse(request);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setStatus(int)}.
	 *
	 * @param status
	 *            the status
	 */
	public void setStatus(int status) {
		SET_STATUS.callSafe(instance, status);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setContentType(String)}.
	 *
	 * @param conentType
	 *            the content type
	 */
	public void setContentType(String conentType) {
		SET_CONTENT_TYPE.callSafe(instance, conentType);

	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#getContentType()}.
	 *
	 * @return the content type
	 */
	public String getContentType() {
		return GET_CONTENT_TYPE.callSafe(instance);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setContentLength(int)}.
	 *
	 * @param length
	 *            the content length
	 */
	public void setContentLength(int length) {
		SET_CONTENT_LENGTH.callSafe(instance, length);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setContentLengthLong(long)}.
	 *
	 * @param length
	 *            the content length
	 */
	public void setContentLengthLong(long length) {
		SET_CONTENT_LENGTH_LONG.callSafe(instance, length);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setCharacterEncoding(String)}.
	 *
	 * @param encoding
	 *            the encoding
	 */
	public void setCharacterEncoding(String encoding) {
		SET_CHARACTER_ENCODING.callSafe(instance, encoding);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#getWriter()}.
	 *
	 * This method does not catch any occuring runtime exceptions!
	 *
	 * @return the print writer
	 */
	public PrintWriter getWriter() {
		return GET_WRITER.callSafeExceptions(RuntimeException.class, instance);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#getOutputStream()}.
	 *
	 * This method does not catch any occuring runtime exceptions!
	 *
	 * @return the servlet output stream
	 */
	public OutputStream getOutputStream() {
		return GET_OUTPUT_STREAM.callSafeExceptions(RuntimeException.class, instance);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setLocale(int)}.
	 *
	 * @param locale
	 *            the locale
	 */
	public void setLocale(Locale locale) {
		SET_LOCALE.callSafe(instance, locale);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#getCharacterEncoding()}.
	 *
	 * @return the character encoding
	 */
	public String getCharacterEncoding() {
		return GET_CHRACTER_ENCODING.callSafe(instance);
	}

	/**
	 * @return the wrapped HTTPServletResponse instance
	 */
	public Object getWrappedElement() {
		return instance;
	}

	/**
	 *
	 * see {@link javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)}.
	 *
	 * @param cookieToSet
	 *            {@link javax.servlet.http.Cookie} instance.
	 */
	public void addCookie(Object cookieToSet) {
		ADD_COOKIE.callSafe(instance, cookieToSet);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#addHeader(String, String)}.
	 *
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 */
	public void addHeader(String headerName, String headerValue) {
		ADD_HEADER.callSafe(instance, headerName, headerValue);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#addIntHeader(String, int)}.
	 *
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 */
	public void addIntHeader(String headerName, int headerValue) {
		ADD_INT_HEADER.callSafe(instance, headerName, headerValue);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setHeader(String, String)}.
	 *
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 */
	public void setHeader(String headerName, String headerValue) {
		SET_HEADER.callSafe(instance, headerName, headerValue);
	}

	/**
	 * see {@link javax.servlet.http.HttpServletResponse#setIntHeader(String, int)}.
	 *
	 * @param headerName
	 *            the header name
	 * @param headerValue
	 *            the header value
	 */
	public void setIntHeader(String headerName, int headerValue) {
		SET_INT_HEADER.callSafe(instance, headerName, headerValue);
	}

}
