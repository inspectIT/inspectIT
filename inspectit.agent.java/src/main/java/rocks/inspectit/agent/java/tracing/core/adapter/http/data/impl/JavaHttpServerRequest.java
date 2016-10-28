package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpServerRequest;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Our implementation of the {@link HttpServerRequest} that works with
 * {@link javax.servlet.HttpServletRequest}. We only need to extract data from the original
 * javax.servlet.HttpServletRequest.
 *
 * @author Ivan Senic
 *
 */
public class JavaHttpServerRequest implements HttpServerRequest {

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
	public String getUri() {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getRequestURI", new Class<?>[] {}, httpServletRequest, new Object[] {}, null,
				JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaggageItem(String headerName) {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getHeader", new Class<?>[] { String.class }, httpServletRequest, new Object[] { headerName }, null,
				JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not used currently.
	 */
	public String getHttpMethod() {
		return (String) cache.invokeMethod(httpServletRequest.getClass(), "getMethod", new Class<?>[] {}, httpServletRequest, new Object[] {}, null, JAVAX_SERVLET_HTTP_SERVLET_REQUEST_CLASS);
	}


}
