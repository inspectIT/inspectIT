package rocks.inspectit.agent.java.eum;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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
	 * The linker used for building this proxy instance.
	 */
	private IRuntimeLinker linker;

	/**
	 * The tag which will be injected into the HTML code.
	 */
	private String tagToInject;

	/**
	 * If non-null, a set-cookie header will be added with this cookie.
	 */
	private Object cookieToSet;

	/**
	 * Buffer for the set content length, if setContentLength was called. for HTML documents this
	 * will not be passed to the original repsonse, as we need chunked encoding for our injection to
	 * work.
	 */
	private Long contentLengthSet = null;

	/**
	 * Constructor. After the Construction, a proxy has to be generated using a
	 * {@link IRuntimeLinker}.
	 *
	 * @param responseObject
	 *            the javax.servlet.http.HTTPServletResponse to wrap.
	 * @param cookieToSet
	 *            the javax.servlet.http.Cookie which shall be set. Null, if no cookie should be
	 *            set.
	 * @param tagToInject
	 *            the tag to inject
	 */
	public TagInjectionResponseWrapper(Object responseObject, Object cookieToSet, String tagToInject) {
		this.tagToInject = tagToInject;
		this.cookieToSet = cookieToSet;
		wrappedResponse = WHttpServletResponse.wrap(responseObject);
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
		if (wrappedWriter == null) {
			commitHeaderData();
			PrintWriter originalWriter = wrappedResponse.getWriter();
			// avoid rewrapping or unnecessary wrapping
			if (isNonHTMLContentTypeSet() || (originalWriter instanceof TagInjectionPrintWriter)) {
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

		if (wrappedStream == null) {
			commitHeaderData();
			OutputStream originalStream = wrappedResponse.getOutputStream();
			// avoid rewrapping or unnecessary wrapping
			if (isNonHTMLContentTypeSet() || linker.isProxyInstance(originalStream, TagInjectionOutputStream.class)) {
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

	@ProxyMethod
	public void setContentLength(int len) {
		// we do not delegate this call at this moment- maybe we have to used chunked encoding for
		// the request
		this.contentLengthSet = Long.valueOf(len);
	}

	@ProxyMethod
	public void setContentLengthLong(long len) {
		// we do not delegate this call at this moment- maybe we have to used chunked encoding for
		// the request
		this.contentLengthSet = len;
	}

	/**
	 * Called when the headers are commited. At this point of time we have to decide whether we
	 * force chunked encoding.
	 */
	private void commitHeaderData() {
		if (isNonHTMLContentTypeSet()) {
			if (contentLengthSet != null) {
				wrappedResponse.setContentLengthLong(contentLengthSet);
			}
		} else {
			if (cookieToSet != null) {
				wrappedResponse.addCookie(cookieToSet);
			}
		}
	}

	/**
	 * @return true, if the content header was set to a type which is not html.
	 */
	private boolean isNonHTMLContentTypeSet() {
		String contentMime = wrappedResponse.getContentType();

		return !((contentMime == null) || contentMime.startsWith("text/html") || contentMime.startsWith("application/xhtml+xml"));

	}

}