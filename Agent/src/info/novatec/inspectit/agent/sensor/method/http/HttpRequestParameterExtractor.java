package info.novatec.inspectit.agent.sensor.method.http;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.util.StringConstraint;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe realization to extract information from <code>HttpServletRequests</code>.
 * 
 * @author Stefan Siegl
 */
class HttpRequestParameterExtractor {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(HttpRequestParameterExtractor.class);

	/**
	 * Constraint for String length.
	 */
	private StringConstraint strConstraint;

	/**
	 * Marker method. This method severs for marking the cache key in {@link #methodCache} as
	 * unavailable. Since we can not put <code>null</code> to a {@link ConcurrentHashMap} we need to
	 * put some method to serve as a marker.
	 */
	private Method markerMethod;

	/**
	 * Keeps track of already looked up <code>Method</code> objects for faster access. Get and Put
	 * operations are synchronized by the concurrent hash map.
	 */
	private ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<String, Method>();

	/**
	 * Structure to store all necessary methods that we can invoke to get http information. These
	 * objects are also used to cache the <code>Method</code> object in a cache.
	 * 
	 * @author Stefan Siegl
	 */
	private enum HttpMethods {
		/** Request URI in Servlet. */
		SERVLET_REQUEST_URI("getRequestURI", (Class<?>[]) null),
		/** Parameter map. */
		SERVLET_GET_PARAMETER_MAP("getParameterMap", (Class<?>[]) null),
		/** Gets all attributes names. */
		SERVLET_GET_ATTRIBUTE_NAMES("getAttributeNames", (Class<?>[]) null),
		/** Gets a given attributes name value. */
		SERVLET_GET_ATTRIBUTE("getAttribute", new Class[] { String.class }),
		/** Gets all header names. */
		SERVLET_GET_HEADER_NAMES("getHeaderNames", (Class<?>[]) null),
		/** Gets the value of one given header. */
		SERVLET_GET_HEADER("getHeader", new Class[] { String.class }),
		/** Gets the session. */
		SERVLET_GET_SESSION("getSession", new Class[] { boolean.class }),
		/** Reads the request method. */
		SERVLET_GET_METHOD("getMethod", (Class<?>[]) null),
		/** Gets all attribute names in the session. */
		SESSION_GET_ATTRIBUTE_NAMES("getAttributeNames", (Class<?>[]) null),
		/** Gets the value of a session attribute. */
		SESSION_GET_ATTRIBUTE("getAttribute", new Class[] { String.class });

		/**
		 * Constructor.
		 * 
		 * @param methodName
		 *            method
		 * @param parameters
		 *            parameters
		 */
		private HttpMethods(String methodName, Class<?>[] parameters) { // NOPMD
			this.methodName = methodName;
			this.parameters = parameters;
		}

		/** name of the method. */
		private String methodName;
		/** parameters of the methods. */
		private Class<?>[] parameters;
	}

	/**
	 * Constructor.
	 * 
	 * @param strConstraint
	 *            the string constraints.
	 */
	public HttpRequestParameterExtractor(StringConstraint strConstraint) {
		this.strConstraint = strConstraint;
		try {
			// setting marker method to point to Object.toString()
			// this will represent non existing cache method
			markerMethod = Object.class.getMethod("toString", new Class[0]);
		} catch (Exception e) {
			throw new IllegalStateException("Method toString() can not be found", e);
		}
	}

	/**
	 * Reads the request URI from the given <code>HttpServletRequest</code> object and stores it
	 * with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the request uri
	 */
	public String getRequestUri(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method m = retrieveMethod(HttpMethods.SERVLET_REQUEST_URI, httpServletRequestClass);
		if (null == m) {
			return HttpTimerData.UNDEFINED;
		}

		try {
			String uri = (String) m.invoke(httpServletRequest, (Object[]) null);
			if (null != uri) {
				return uri;
			} else {
				return HttpTimerData.UNDEFINED;
			}
		} catch (Exception e) {
			LOG.error("Invocation on given object failed.", e);
			return HttpTimerData.UNDEFINED;
		}
	}

	/**
	 * Reads the request URI from the given <code>HttpServletRequest</code> object and stores it
	 * with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the request method
	 */
	public String getRequestMethod(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method m = retrieveMethod(HttpMethods.SERVLET_GET_METHOD, httpServletRequestClass);
		if (null == m) {
			return HttpTimerData.UNDEFINED;
		}

		try {
			String requestMethod = (String) m.invoke(httpServletRequest, (Object[]) null);
			if (null != requestMethod) {
				return requestMethod;
			} else {
				return HttpTimerData.UNDEFINED;
			}
		} catch (Exception e) {
			LOG.error("Invocation on given object failed.", e);
			return HttpTimerData.UNDEFINED;
		}
	}

	/**
	 * Reads all request parameters from the given <code>HttpServletRequest</code> object and stores
	 * them with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the parameters
	 */
	public Map<String, String[]> getParameterMap(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method m = retrieveMethod(HttpMethods.SERVLET_GET_PARAMETER_MAP, httpServletRequestClass);
		if (null == m) {
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, String[]> parameterMap = (Map<String, String[]>) m.invoke(httpServletRequest, (Object[]) null);

			if (null == parameterMap || parameterMap.isEmpty()) {
				return null;
			}

			return strConstraint.crop(parameterMap);
		} catch (Exception e) {
			LOG.error("Invocation on given object failed.", e);
			return null;
		}
	}

	/**
	 * Reads all request attributes from the given <code>HttpServletRequest</code> object and stores
	 * them with the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the attributes
	 */
	public Map<String, String> getAttributes(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method attributesMethod = retrieveMethod(HttpMethods.SERVLET_GET_ATTRIBUTE_NAMES, httpServletRequestClass);
		if (null == attributesMethod) {
			return null;
		}

		Method attributeValue = retrieveMethod(HttpMethods.SERVLET_GET_ATTRIBUTE, httpServletRequestClass);
		if (null == attributeValue) {
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> params = (Enumeration<String>) attributesMethod.invoke(httpServletRequest, (Object[]) null);
			Map<String, String> attributes = new HashMap<String, String>();
			if (null == params) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Attribute enumeration was <null>");
				}
				return null;
			}
			while (params.hasMoreElements()) {
				String attrName = params.nextElement();
				Object value = attributeValue.invoke(httpServletRequest, new Object[] { attrName });
				attributes.put(attrName, strConstraint.crop(getAttributeValue(value)));
			}
			return attributes;
		} catch (Exception e) {
			LOG.error("Invocation of " + attributesMethod.getName() + " to get attributes on given object failed.", e);
			return null;
		}
	}

	/**
	 * Reads all headers from the given <code>HttpServletRequest</code> object and stores them with
	 * the given <code>HttpTimerData</code> object.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return the headers
	 */
	public Map<String, String> getHeaders(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method headerNamesMethod = retrieveMethod(HttpMethods.SERVLET_GET_HEADER_NAMES, httpServletRequestClass);
		if (null == headerNamesMethod) {
			return null;
		}

		Method headerValueMethod = retrieveMethod(HttpMethods.SERVLET_GET_HEADER, httpServletRequestClass);
		if (null == headerValueMethod) {
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> headers = (Enumeration<String>) headerNamesMethod.invoke(httpServletRequest, (Object[]) null);
			Map<String, String> headersResult = new HashMap<String, String>();
			if (headers != null) {
				while (headers.hasMoreElements()) {
					String headerName = (String) headers.nextElement();
					String headerValue = (String) headerValueMethod.invoke(httpServletRequest, new Object[] { headerName });
					headersResult.put(headerName, strConstraint.crop(headerValue));
				}
				return headersResult;
			}
		} catch (Exception e) {
			LOG.error("Invocation of to get attributes on given object failed.", e);
		}
		return null;
	}

	/**
	 * Reads all session attributes from the <code>HttpSession</code> of the given
	 * <code>HttpServletRequest</code> object and stores them with the given
	 * <code>HttpTimerData</code> object. This method ensures that no new session will be created.
	 * 
	 * @param httpServletRequestClass
	 *            the <code>Class</code> object representing the class of the given
	 *            <code>HttpServletRequest</code>
	 * @param httpServletRequest
	 *            the object realizing the <code> HttpServletRequest </code> interface.
	 * @return session attributes
	 */
	public Map<String, String> getSessionAttributes(Class<?> httpServletRequestClass, Object httpServletRequest) {
		Method getSessionMethod = retrieveMethod(HttpMethods.SERVLET_GET_SESSION, httpServletRequestClass);

		if (null == getSessionMethod) { // Could not retrieve method
			return null;
		}

		Object httpSession;
		Class<?> httpSessionClass;
		try {
			httpSession = getSessionMethod.invoke(httpServletRequest, new Object[] { Boolean.FALSE });
			if (httpSession == null) {
				// Currently we do not have a session and thus cannot get any session attributes
				if (LOG.isDebugEnabled()) {
					LOG.debug("No session can be found");
				}
				return null;
			}
			httpSessionClass = httpSession.getClass();

		} catch (Exception e) {
			LOG.error("Invocation of to get attributes on given object failed.", e);
			// we cannot go on!
			return null;
		}

		Method getAttributeNamesSession = retrieveMethod(HttpMethods.SESSION_GET_ATTRIBUTE_NAMES, httpSessionClass);
		if (null == getAttributeNamesSession) {
			return null;
		}

		Method getAttributeValueSession = retrieveMethod(HttpMethods.SESSION_GET_ATTRIBUTE, httpSessionClass);
		if (null == getAttributeValueSession) {
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> sessionAttr = (Enumeration<String>) getAttributeNamesSession.invoke(httpSession, (Object[]) null);
			Map<String, String> sessionAttributes = new HashMap<String, String>();

			if (null != sessionAttr) {
				while (sessionAttr.hasMoreElements()) {
					String sessionAtt = sessionAttr.nextElement();
					Object sessionValue = (Object) getAttributeValueSession.invoke(httpSession, sessionAtt);
					sessionAttributes.put(sessionAtt, strConstraint.crop(getAttributeValue(sessionValue)));
				}
				return sessionAttributes;
			}
		} catch (Exception e) {
			LOG.error("Invocation of to get attributes on given object failed.", e);
		}
		return null;
	}

	/**
	 * Tries a lookup in the cache first, then tries to get the <code>Method</code> object via
	 * reflection.
	 * 
	 * @param httpMethod
	 *            the Method to lookup
	 * @param clazzUsedToLookup
	 *            the class to use if reflection lookup is necessary (if it is not already in the
	 *            cache)
	 * @return the <code>Method</code> object or <code>null</code> if the method cannot be found.
	 */
	private Method retrieveMethod(HttpMethods httpMethod, Class<?> clazzUsedToLookup) {
		String cacheLookupName = getCacheLookupName(httpMethod, clazzUsedToLookup);
		Method m = methodCache.get(cacheLookupName);

		if (null == m) {
			// We do not yet have the method in the Cache
			try {
				m = clazzUsedToLookup.getMethod(httpMethod.methodName, httpMethod.parameters);
				m.setAccessible(true);
				Method existing = methodCache.putIfAbsent(cacheLookupName, m);
				if (null != existing) {
					m = existing;
				}
			} catch (Exception e) {
				LOG.error("The provided class " + clazzUsedToLookup.getCanonicalName() + " did not provide the desired method.", e);

				// Do not try to look up every time.
				// Can not place null as value anyway
				methodCache.putIfAbsent(cacheLookupName, markerMethod);
			}
		} else if (markerMethod.equals(m)) {
			return null;
		}

		return m;
	}

	/**
	 * Generates and return a lookup name for the cache.
	 * 
	 * @param httpMethod
	 *            the Method to lookup
	 * @param clazz
	 *            the concrete class to lookup the method upon.
	 * @return the generated lookup name.
	 */
	private String getCacheLookupName(HttpMethods httpMethod, Class<?> clazz) {
		return clazz.getCanonicalName() + '#' + httpMethod.methodName;
	}

	/**
	 * Utility method that checks if the attribute provided is an Array, and if it so, formats the
	 * return String in the human-readable form. If the attribute is not an Array, the
	 * {@link Object#toString()} will be returned. If attribute is <code>null</code>, then
	 * '<notset>' will be returned.
	 * 
	 * @param attribute
	 *            Attribute to get {@link String} value for.
	 * @return Human-readable value of attribute.
	 */
	private String getAttributeValue(Object attribute) {
		if (null == attribute) {
			return "<notset>";
		}
		if (attribute.getClass().isArray()) {
			StringBuilder stringBuilder = new StringBuilder("[");
			boolean isFirst = true;

			int length = Array.getLength(attribute);
			for (int i = 0; i < length; i++) {
				if (isFirst) {
					stringBuilder.append(Array.get(attribute, i));
					isFirst = false;
				} else {
					stringBuilder.append(", ");
					stringBuilder.append(Array.get(attribute, i));
				}
			}
			stringBuilder.append(']');
			return stringBuilder.toString();
		} else {
			return attribute.toString();
		}
	}
}
