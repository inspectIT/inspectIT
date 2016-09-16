package rocks.inspectit.agent.java.eum.instrumentation;


import java.io.IOException;
import java.io.OutputStream;

import rocks.inspectit.agent.java.eum.html.HTMLScriptInjector;
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
	 * The actual stream to which the data will be written.
	 */
	private WServletOutputStream originalStream;

	/**
	 * The parser used for inejcting the tag.
	 */
	private HTMLScriptInjector parser;

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
		this.originalStream = WServletOutputStream.wrap(originalStream);
		parser = new HTMLScriptInjector(tagToInject);
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
		parser.setEncoding(charsetName);
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
		originalStream.print(parser.performInjection(String.valueOf(arg0)));
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
		originalStream.print(parser.performInjection(String.valueOf(c)));
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
		originalStream.print(parser.performInjection(String.valueOf(d)));
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
		originalStream.print(parser.performInjection(String.valueOf(f)));
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
		originalStream.print(parser.performInjection(String.valueOf(i)));
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
		originalStream.print(parser.performInjection(String.valueOf(l)));
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
		originalStream.print(parser.performInjection(arg0));
	}

	/**
	 * Proxy method for println method of the OutputStream. Terminates the line.
	 *
	 * @throws IOException
	 *             signals that an I/O exception in some sort happened
	 */
	@ProxyMethod
	public void println() throws IOException {
		originalStream.print(parser.performInjection(NL));
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
		originalStream.print(parser.performInjection(b + NL));
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
		originalStream.print(parser.performInjection(c + NL));
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
		originalStream.print(parser.performInjection(d + NL));
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
		originalStream.print(parser.performInjection(f + NL));
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
		originalStream.print(parser.performInjection(i + NL));
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
		originalStream.print(parser.performInjection(l + NL));
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
		originalStream.print(parser.performInjection(s + NL));
	}


	@Override
	@ProxyMethod
	public void write(int b) throws IOException {
		originalStream.write(parser.performInjection(new byte[] { (byte) b }));
	}

	@Override
	@ProxyMethod
	public void write(byte[] b) throws IOException {
		originalStream.write(parser.performInjection(b));
	}

	@Override
	@ProxyMethod
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] copiedData = new byte[len];
		System.arraycopy(b, off, copiedData, 0, len);
		originalStream.write(parser.performInjection(copiedData));
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
