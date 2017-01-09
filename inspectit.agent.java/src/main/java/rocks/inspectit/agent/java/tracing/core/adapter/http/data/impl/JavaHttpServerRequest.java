package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Our implementation of the {@link HttpServerRequest} that works with
 * {@link javax.servlet.HttpServletRequest}. We only need to extract data from the original
 * javax.servlet.HttpServletRequest.
 *
 * @author Ivan Senic
 *
 */
public class JavaHttpServerRequest implements HttpRequest {

	/**
	 * FQN constant of the javax.servlet.HttpServletRequest.
	 */
	private static final String JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS = "javax.servlet.HttpServletRequest";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Object representing http servlet request.
	 */
	private final Object httpServletRequest;

	/**
	 * @param httpServletRequest
	 *            request object
	 * @param cache
	 *            reflection cache to use
	 */
	public JavaHttpServerRequest(Object httpServletRequest, ReflectionCache cache) {
		this.httpServletRequest = httpServletRequest;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUrl() {
		Object url = cache.invokeMethod(httpServletRequest.getClass(), "getRequestURL", new Class<?>[] {}, httpServletRequest, new Object[] {}, null, JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
		if (null != url) {
			return url.toString();
		} else {
			// fail back to URI
			return (String) cache.invokeMethod(httpServletRequest.getClass(), "getRequestURI", new Class<?>[] {}, httpServletRequest, new Object[] {}, null, JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHttpMethod() {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getMethod", new Class<?>[] {}, httpServletRequest, new Object[] {}, null, JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Entry<String, String>> iterator() {
		Object headerNames = cache.invokeMethod(httpServletRequest.getClass(), "getHeaderNames", new Class<?>[] {}, httpServletRequest, new Object[] {}, null,
				JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
		if (headerNames instanceof Enumeration<?>) {
			Enumeration<?> enumeration = (Enumeration<?>) headerNames;
			Map<String, String> baggage = new HashMap<String, String>();
			while (enumeration.hasMoreElements()) {
				String headerName = enumeration.nextElement().toString();
				String headerValue = (String) cache.invokeMethod(httpServletRequest.getClass(), "getHeader", new Class<?>[] { String.class }, httpServletRequest, new Object[] { headerName }, null,
						JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
				if (null != headerValue) {
					baggage.put(headerName, headerValue);
				}
			}
			return baggage.entrySet().iterator();
		} else {
			return Collections.<String, String> emptyMap().entrySet().iterator();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String key, String value) {
		throw new UnsupportedOperationException("Server request does not provide option to put baggage.");
	}

}
