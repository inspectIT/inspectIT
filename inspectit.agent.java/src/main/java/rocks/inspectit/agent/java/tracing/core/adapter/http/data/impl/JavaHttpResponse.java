package rocks.inspectit.agent.java.tracing.core.adapter.http.data.impl;

import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Our implementation of the {@link HttpResponse} class working with
 * {@link javax.servlet.HttpServletResponse}. We only need to extract data from the original
 * javax.servlet.HttpServletResponse.
 *
 * @author Ivan Senic
 *
 */
public class JavaHttpResponse implements HttpResponse {

	/**
	 * FQN of the javax.servlet.HttpServletResponse.
	 */
	private static final String JAVAX_SERVLET_HTTP_SERVLET_RESPONSE_FQN = "javax.servlet.HttpServletResponse";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache;

	/**
	 * Cache for the <code> Method </code> elements. One {@link ReflectionCache} for all the
	 * instances of this class.
	 */
	private final Object httpServletResponse;

	/**
	 * @param httpServletResponse
	 *            response object
	 * @param cache
	 *            reflection cache to use
	 */
	public JavaHttpResponse(Object httpServletResponse, ReflectionCache cache) {
		this.httpServletResponse = httpServletResponse;
		this.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return (Integer) cache.invokeMethod(httpServletResponse.getClass(), "getStatus", new Class<?>[] {}, httpServletResponse, new Object[] {}, 0, JAVAX_SERVLET_HTTP_SERVLET_RESPONSE_FQN);
	}

}