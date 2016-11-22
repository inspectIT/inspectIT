package rocks.inspectit.agent.java.eum.instrumentation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.agent.java.eum.data.IdGenerator;
import rocks.inspectit.agent.java.eum.reflection.WCookie;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletRequest;
import rocks.inspectit.agent.java.eum.reflection.WHttpServletResponse;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;

/**
 *
 * Detects html content and injects a script tag at the correct point on-the-fly.
 *
 * @author Jonas Kunz
 *
 */
@ProxyFor(superClass = "javax.servlet.http.HttpServletResponseWrapper", constructorParameterTypes = { "javax.servlet.http.HttpServletResponse" })
public class TagInjectionResponseWrapper implements IProxySubject {

	/**
	 * The name of the HTML header specifying the content length.
	 */
	private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";

	/**
	 * if getOutputStream() was called on this class, this variable will hold the generated stream.
	 */
	private OutputStream wrappedStream;

	/**
	 * if getWriter() was called on this class, this variable will hold the generated writer.
	 */
	private PrintWriter wrappedWriter;

	/**
	 * The original, uninstrumented response object which is wrapped by this instance.
	 */
	private WHttpServletResponse wrappedResponse;

	/**
	 * The original, uninstrumented request for this response.
	 */
	private WHttpServletRequest wrappedRequest;

	/**
	 * Generator for generating session IDs.
	 */
	private IdGenerator idGenerator;

	/**
	 * The linker used for building this proxy instance.
	 */
	private IRuntimeLinker linker;

	/**
	 * The tag which will be injected into the HTML code.
	 */
	private String tagToInject;

	/**
	 * Buffer all Calls issued which modify the content-length header. We omit these calls if we
	 * perform an instrumentation to force chunked encoding.
	 */
	private List<Runnable> contentLengthHeaderModifications;

	/**
	 * Flag whether the header data has already been committed.
	 */
	private boolean headerCommitted = false;

	/**
	 * Constructor. After the Construction, a proxy has to be generated using a
	 * {@link IRuntimeLinker}.
	 *
	 * @param requestObject
	 *            the javax.servlet.http.HTTPServletResponse which triggered this response.
	 * @param responseObject
	 *            the javax.servlet.http.HTTPServletResponse to wrap.
	 * @param idGenerator
	 *            the isGenerator used for generating session cookies
	 * @param tagToInject
	 *            the tag to inject
	 */
	public TagInjectionResponseWrapper(Object requestObject, Object responseObject, IdGenerator idGenerator, String tagToInject) {
		this.tagToInject = tagToInject;
		this.idGenerator = idGenerator;
		wrappedResponse = WHttpServletResponse.wrap(responseObject);
		wrappedRequest = WHttpServletRequest.wrap(requestObject);
		contentLengthHeaderModifications = new ArrayList<Runnable>();
	}

	@Override
	public Object[] getProxyConstructorArguments() {
		return new Object[] { wrappedResponse.getWrappedElement() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
		this.linker = linker;
	}

	/**
	 * Proxy for {@link javax.servlet.ServletResponse#getWriter()}.
	 *
	 * @return the instrumented writer
	 * @throws IOException
	 *             if an exception getting the original writer occurs.
	 */
	@ProxyMethod
	public PrintWriter getWriter() throws IOException {
		commitHeaderData();
		if (wrappedWriter == null) {
			PrintWriter originalWriter = wrappedResponse.getWriter();
			// avoid rewrapping or unnecessary wrapping
			if (isNonHtmlContentTypeSet() || (originalWriter instanceof TagInjectionPrintWriter)) {
				wrappedWriter = originalWriter;
			} else {
				wrappedWriter = new TagInjectionPrintWriter(originalWriter, tagToInject);
			}
		}
		return wrappedWriter;
	}

	/**
	 * Proxy for {@link javax.servlet.ServletResponse#getOutputStream()}.
	 *
	 * @return the instrumented stream
	 * @throws IOException
	 *             if an exception getting the original stream occurs.
	 */
	@ProxyMethod(returnType = "javax.servlet.ServletOutputStream")
	public OutputStream getOutputStream() throws IOException {

		commitHeaderData();
		if (wrappedStream == null) {
			OutputStream originalStream = wrappedResponse.getOutputStream();
			// avoid rewrapping or unnecessary wrapping
			if (isNonHtmlContentTypeSet() || linker.isProxyInstance(originalStream, TagInjectionOutputStream.class)) {
				wrappedStream = originalStream;
			} else {
				TagInjectionOutputStream resultStr = new TagInjectionOutputStream(originalStream, tagToInject);
				resultStr.setEncoding(wrappedResponse.getCharacterEncoding());

				ClassLoader cl = wrappedResponse.getWrappedElement().getClass().getClassLoader();
				wrappedStream = (OutputStream) linker.createProxy(TagInjectionOutputStream.class, resultStr, cl);
				if (wrappedStream == null) {
					// fallback to the normal stream if it can not be linked
					wrappedStream = originalStream;
				}
			}
		}
		return wrappedStream;
	}

	/**
	 * Overrides setContentLength to consume the call and to postpone it. When we inject our script
	 * tag, the content length is not known beforehand. Therefore, in case we perform an injection,
	 * we have to force chunked encoding by not setting any content length.
	 *
	 * @param len
	 *            the parameter from the proxied method.
	 */
	@ProxyMethod
	public void setContentLength(final int len) {
		contentLengthHeaderModifications.add(new Runnable() {
			@Override
			public void run() {
				wrappedResponse.setContentLength(len);
			}
		});
	}

	/**
	 * See {@link #setContentLength(int)} for the reasons for proxying.
	 *
	 * @param len
	 *            parameter of the proxied method
	 */
	@ProxyMethod
	public void setContentLengthLong(final long len) {
		contentLengthHeaderModifications.add(new Runnable() {
			@Override
			public void run() {
				wrappedResponse.setContentLengthLong(len);
			}
		});
	}

	/**
	 * See {@link #setContentLength(int)} for the reasons for proxying.
	 *
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 */
	@ProxyMethod
	public void addHeader(final String name, final String value) {
		if (CONTENT_LENGTH_HEADER_NAME.equalsIgnoreCase(name)) {
			contentLengthHeaderModifications.add(new Runnable() {
				@Override
				public void run() {
					wrappedResponse.addHeader(name, value);
				}
			});
		} else {
			wrappedResponse.addHeader(name, value);
		}
	}

	/**
	 * See {@link #setContentLength(int)} for the reasons for proxying.
	 *
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 */
	@ProxyMethod
	public void addIntHeader(final String name, final int value) {
		if (CONTENT_LENGTH_HEADER_NAME.equalsIgnoreCase(name)) {
			contentLengthHeaderModifications.add(new Runnable() {
				@Override
				public void run() {
					wrappedResponse.addIntHeader(name, value);
				}
			});
		} else {
			wrappedResponse.addIntHeader(name, value);
		}
	}

	/**
	 * See {@link #setContentLength(int)} for the reasons for proxying.
	 *
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 */
	@ProxyMethod
	public void setHeader(final String name, final String value) {
		if (CONTENT_LENGTH_HEADER_NAME.equalsIgnoreCase(name)) {
			contentLengthHeaderModifications.add(new Runnable() {
				@Override
				public void run() {
					wrappedResponse.setHeader(name, value);
				}
			});
		} else {
			wrappedResponse.setHeader(name, value);
		}
	}

	/**
	 * See {@link #setContentLength(int)} for the reasons for proxying.
	 *
	 * @param name
	 *            the name of the header
	 * @param value
	 *            the value of the header
	 */
	@ProxyMethod
	public void setIntHeader(final String name, final int value) {
		if (CONTENT_LENGTH_HEADER_NAME.equalsIgnoreCase(name)) {
			contentLengthHeaderModifications.add(new Runnable() {
				@Override
				public void run() {
					wrappedResponse.setIntHeader(name, value);
				}
			});
		} else {
			wrappedResponse.setIntHeader(name, value);
		}
	}

	/**
	 * Called when the headers are commited. At this point of time we have to decide whether we
	 * force chunked encoding.
	 */
	private void commitHeaderData() {
		if (headerCommitted) {
			return;
		}
		headerCommitted = true;
		if (isNonHtmlContentTypeSet()) {
			// replicate the commands setting the content-length
			for (Runnable cmd : contentLengthHeaderModifications) {
				cmd.run();
			}
		} else {
			Object sessionCookie = generateSessionIDCookie();
			if (sessionCookie != null) {
				wrappedResponse.addCookie(sessionCookie);
			}
		}
	}

	/**
	 *
	 * Generates the cookie for tracking the user session. The returned Cookie is of type
	 * javax.servlet.http.Cookie.
	 *
	 * @return the new session ID cookie, or null if it is already set.
	 */
	private Object generateSessionIDCookie() {
		// check if it already has an id set, if yes don't do anything
		Object[] cookies = wrappedRequest.getCookies();
		if (cookies != null) {
			for (Object cookieObj : cookies) {
				WCookie cookie = WCookie.wrap(cookieObj);
				if (cookie.getName().equals(JSAgentBuilder.SESSION_ID_COOKIE_NAME)) {
					return null; // cookie already present, nothing todo
				}
			}
		}

		String sessionID = String.valueOf(idGenerator.generateSessionID());

		// otherwise generate the cookie
		ClassLoader cl = wrappedResponse.getWrappedElement().getClass().getClassLoader();
		Object cookie = WCookie.newInstance(cl, JSAgentBuilder.SESSION_ID_COOKIE_NAME, sessionID);
		WCookie wrappedCookie = WCookie.wrap(cookie);
		wrappedCookie.setPath("/");
		// We do not set any expiration age - the default age "-1" represents a session cookie which
		// is deleted when the browser is closed
		return cookie;
	}

	/**
	 * @return true, if the content header was set to a type which is not html.
	 */
	private boolean isNonHtmlContentTypeSet() {
		String contentMime = wrappedResponse.getContentType();

		return !((contentMime == null) || contentMime.startsWith("text/html") || contentMime.startsWith("application/xhtml+xml"));

	}

}