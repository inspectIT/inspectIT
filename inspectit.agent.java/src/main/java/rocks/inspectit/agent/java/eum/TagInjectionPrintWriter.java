package rocks.inspectit.agent.java.eum;


import java.io.PrintWriter;
import java.util.Locale;

/**
 * A PrintWriter which injects the given tag on the fly into the head (or another appropriate)
 * section of the document. Automatically detects non-html and then falls back to just piping the
 * data through.
 *
 * @author Jonas Kunz
 */
public class TagInjectionPrintWriter extends PrintWriter {

	/**
	 * The HTML parser used ofr injecting the tag.
	 */
	private HTMLScriptInjector parser;

	/**
	 * The writer to pass the data to.
	 */
	private PrintWriter originalWriter;

	/**
	 * New-line character.
	 */
	private static final String NL = System.getProperty("line.separator");



	/**
	 * Creates a new print writer which performs the tag injection.
	 *
	 * @param originalWriter
	 *            The writer which is wrapped.
	 * @param tagToInject
	 *            The tag(s) to insert.
	 */
	public TagInjectionPrintWriter(PrintWriter originalWriter, String tagToInject) {
		super(originalWriter);
		this.originalWriter = originalWriter;
		parser = new HTMLScriptInjector(tagToInject);

	}

	@Override
	public void flush() {
		originalWriter.flush();
	}


	@Override
	public boolean checkError() {
		return originalWriter.checkError();
	}

	@Override
	public void close() {
		originalWriter.close();
	}

	@Override
	public void write(int c) {
		originalWriter.write(parser.performInjection(String.valueOf(((char) c))));
	}

	@Override
	public void write(char[] buf, int off, int len) {
		originalWriter.write(parser.performInjection(String.valueOf(buf, off, len)));
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void write(char[] buf) {
		originalWriter.write(parser.performInjection(String.valueOf(buf)));
	}

	@Override
	public void write(String s, int off, int len) {
		originalWriter.write(parser.performInjection(s.substring(off, off + len)));
	}

	@Override
	public void write(String s) {
		originalWriter.write(parser.performInjection(s));
	}

	@Override
	public void print(boolean b) {
		originalWriter.write(parser.performInjection(String.valueOf(b)));
	}

	@Override
	public void print(char c) {
		originalWriter.write(parser.performInjection(String.valueOf(c)));
	}

	@Override
	public void print(int i) {
		originalWriter.write(parser.performInjection(String.valueOf(i)));
	}

	@Override
	public void print(long l) {
		originalWriter.write(parser.performInjection(String.valueOf(l)));
	}

	@Override
	public void print(float f) {
		originalWriter.write(parser.performInjection(String.valueOf(f)));
	}

	@Override
	public void print(double d) {
		originalWriter.write(parser.performInjection(String.valueOf(d)));
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void print(char[] s) {
		originalWriter.write(parser.performInjection(String.valueOf(s)));
	}

	@Override
	public void print(String s) {
		originalWriter.write(parser.performInjection(s));
	}

	@Override
	public void print(Object obj) {
		originalWriter.write(parser.performInjection(String.valueOf(obj)));
	}

	@Override
	public void println() {
		originalWriter.write(parser.performInjection(NL));
	}

	@Override
	public void println(boolean x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(char x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(int x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(long x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(float x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(double x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void println(char[] x) {
		originalWriter.write(parser.performInjection(new String(x) + NL));
	}

	@Override
	public void println(String x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public void println(Object x) {
		originalWriter.write(parser.performInjection(x + NL));
	}

	@Override
	public PrintWriter printf(String format, Object... args) {
		return this.format(format, args);
	}

	@Override
	public PrintWriter printf(Locale l, String format, Object... args) {
		return this.format(l, format, args);
	}

	@Override
	public PrintWriter format(String format, Object... args) {
		originalWriter.write(parser.performInjection(String.format(format, args)));
		return this;
	}

	@Override
	public PrintWriter format(Locale l, String format, Object... args) {
		originalWriter.write(parser.performInjection(String.format(l, format, args)));
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq) {
		originalWriter.append(parser.performInjection(csq.toString()));
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq, int start, int end) {
		originalWriter.append(parser.performInjection(csq.subSequence(start, end).toString()));
		return this;
	}

	@Override
	public PrintWriter append(char c) {
		originalWriter.append(parser.performInjection(String.valueOf(c)));
		return this;
	}



}
