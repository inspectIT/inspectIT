package rocks.inspectit.agent.java.eum.instrumentation;


import java.io.IOException;
import java.io.OutputStream;

import rocks.inspectit.agent.java.eum.html.DecodingHtmlScriptInjector;
import rocks.inspectit.agent.java.eum.reflection.WServletOutputStream;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;

/**
 * A ServletOutputStream which injects the given tag on the fly into the head (or another appropriate) section of the document.
 * Automatically detects non-html and then falls back to just piping the data through.
 *
 * @author Jonas Kunz
 */
@ProxyFor(superClass = "javax.servlet.ServletOutputStream")
public class TagInjectionOutputStream extends OutputStream implements IProxySubject {

	/**
	 * The default encoding used by the servlet api.
	 */
	private static final String DEFAULT_ENCODING = "ISO-8859-1";

	/**
	 * The actual stream to which the data will be written.
	 */
	private WServletOutputStream originalStream;

	/**
	 * The parser used for inejcting the tag.
	 */
	private DecodingHtmlScriptInjector injector;

	/**
	 * The new-line character.
	 */
	private static final String NL = System.getProperty("line.separator");


	/**
	 * Creates a tag injecting stream.
	 *
	 * @param originalStream
	 *            the wrapped stream, to which the data will be passed through.
	 * @param tagToInject
	 *            the tag to inject.
	 */
	public TagInjectionOutputStream(Object originalStream, String tagToInject) {
		this.originalStream = WServletOutputStream.wrap((OutputStream) originalStream);
		injector = new DecodingHtmlScriptInjector(tagToInject, DEFAULT_ENCODING);
	}

	@Override
	public Object[] getProxyConstructorArguments() {
		return new Object[]{};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
		//nothing to do
	}

	/**
	 * Sets the Character encoding used by the data.
	 * Only this way the stream is able to decode / encode binary data.
	 * @param charsetName the name of the encoding
	 */
	public void setEncoding(String charsetName) {
		injector.setCharacterEncoding(charsetName);
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param arg0
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(boolean arg0) throws IOException {
		String newValue = injector.performInjection(String.valueOf(arg0));
		if (newValue == null) {
			originalStream.print(arg0);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param c
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(char c) throws IOException {
		String newValue = injector.performInjection(String.valueOf(c));
		if (newValue == null) {
			originalStream.print(c);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param d
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(double d) throws IOException {
		String newValue = injector.performInjection(String.valueOf(d));
		if (newValue == null) {
			originalStream.print(d);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param f
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(float f) throws IOException {
		String newValue = injector.performInjection(String.valueOf(f));
		if (newValue == null) {
			originalStream.print(f);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param i
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(int i) throws IOException {
		String newValue = injector.performInjection(String.valueOf(i));
		if (newValue == null) {
			originalStream.print(i);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param l
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(long l) throws IOException {
		String newValue = injector.performInjection(String.valueOf(l));
		if (newValue == null) {
			originalStream.print(l);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for print method of the OutputStream.
	 *
	 * @param arg0
	 *            value which should get printed
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void print(String arg0) throws IOException {
		String newValue = injector.performInjection(String.valueOf(arg0));
		if (newValue == null) {
			originalStream.print(arg0);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream. Terminates the line.
	 *
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println() throws IOException {
		String newValue = injector.performInjection(NL);
		if (newValue == null) {
			originalStream.println();
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param b
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(boolean b) throws IOException {
		String newValue = injector.performInjection(b + NL);
		if (newValue == null) {
			originalStream.println(b);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param c
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(char c) throws IOException {
		String newValue = injector.performInjection(c + NL);
		if (newValue == null) {
			originalStream.println(c);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param d
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(double d) throws IOException {
		String newValue = injector.performInjection(d + NL);
		if (newValue == null) {
			originalStream.println(d);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param f
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(float f) throws IOException {
		String newValue = injector.performInjection(f + NL);
		if (newValue == null) {
			originalStream.println(f);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param i
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(int i) throws IOException {
		String newValue = injector.performInjection(i + NL);
		if (newValue == null) {
			originalStream.println(i);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param l
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(long l) throws IOException {
		String newValue = injector.performInjection(l + NL);
		if (newValue == null) {
			originalStream.println(l);
		} else {
			originalStream.print(newValue);
		}
	}

	/**
	 * Proxy method for println method of the OutputStream.
	 *
	 * @param s
	 *            value which should get printed with an following line termination.
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println(String s) throws IOException {
		String newValue = injector.performInjection(s + NL);
		if (newValue == null) {
			originalStream.println(s);
		} else {
			originalStream.print(newValue);
		}
	}


	@Override
	@ProxyMethod
	public void write(int b) throws IOException {
		byte[] newValue = injector.performInjection(new byte[] { (byte) b });
		if (newValue == null) {
			originalStream.write(b);
		} else {
			originalStream.write(newValue);
		}
	}

	@Override
	@ProxyMethod
	public void write(byte[] b) throws IOException {
		byte[] newValue = injector.performInjection(b);
		if (newValue == null) {
			originalStream.write(b);
		} else {
			originalStream.write(newValue);
		}
	}

	@Override
	@ProxyMethod
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] newValue = injector.performInjection(b, off, len);
		if (newValue == null) {
			originalStream.write(b, off, len);
		} else {
			originalStream.write(newValue);
		}
	}

	@Override
	@ProxyMethod
	public void flush() throws IOException {
		originalStream.flush();
	}

	@Override
	@ProxyMethod
	public void close() throws IOException {
		originalStream.close();
	}


	/**
	 * Proxy method for isReady method of the OutputStream.
	 *
	 * @return the ready state
	 */
	@ProxyMethod(isOptional = true)
	public boolean isReady() {
		return originalStream.isReady();
	}

	/**
	 * Proxy method for setting the write listener.
	 *
	 * @param listener
	 *            write listener which should get set.
	 */
	@ProxyMethod(parameterTypes = { "javax.servlet.WriteListener" }, isOptional = true)
	public void setWriteListener(Object listener) {
		originalStream.setWriteListener(listener);

	}

}
