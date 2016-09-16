package rocks.inspectit.agent.java.eum.instrumentation;

import java.io.PrintWriter;
import java.util.Locale;

import rocks.inspectit.agent.java.eum.html.StreamedHTMLScriptInjector;

/**
 * A PrintWriter which injects the given tag on the fly into the head (or another appropriate)
 * section of the document. Automatically detects non-html and then falls back to just piping the
 * data through.
 *
 * @author Jonas Kunz
 */
public class TagInjectionPrintWriter extends PrintWriter {

	/**
	 * New-line character.
	 */
	private static final String NL = System.getProperty("line.separator");

	/**
	 * The HTML parser used for injecting the tag.
	 */
	private StreamedHTMLScriptInjector injector;

	/**
	 * The writer to pass the data to.
	 */
	private PrintWriter originalWriter;

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
		injector = new StreamedHTMLScriptInjector(tagToInject);

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
		String newValue = injector.performInjection(String.valueOf((char) c));
		if (newValue == null) {
			originalWriter.write(c);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void write(char[] buf, int off, int len) {
		String newValue = injector.performInjection(String.valueOf(buf, off, len));
		if (newValue == null) {
			originalWriter.write(buf, off, len);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void write(char[] buf) {
		String newValue = injector.performInjection(String.valueOf(buf));
		if (newValue == null) {
			originalWriter.write(buf);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void write(String s, int off, int len) {
		String newValue = injector.performInjection(s.substring(off, off + len));
		if (newValue == null) {
			originalWriter.write(s, off, len);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void write(String s) {
		String newValue = injector.performInjection(s);
		if (newValue == null) {
			originalWriter.write(s);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(boolean b) {
		String newValue = injector.performInjection(String.valueOf(b));
		if (newValue == null) {
			originalWriter.print(b);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(char c) {
		String newValue = injector.performInjection(String.valueOf(c));
		if (newValue == null) {
			originalWriter.print(c);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(int i) {
		String newValue = injector.performInjection(String.valueOf(i));
		if (newValue == null) {
			originalWriter.print(i);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(long l) {
		String newValue = injector.performInjection(String.valueOf(l));
		if (newValue == null) {
			originalWriter.print(l);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(float f) {
		String newValue = injector.performInjection(String.valueOf(f));
		if (newValue == null) {
			originalWriter.print(f);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(double d) {
		String newValue = injector.performInjection(String.valueOf(d));
		if (newValue == null) {
			originalWriter.print(d);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void print(char[] s) {
		String newValue = injector.performInjection(String.valueOf(s));
		if (newValue == null) {
			originalWriter.print(s);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(String s) {
		String newValue = injector.performInjection(s);
		if (newValue == null) {
			originalWriter.print(s);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void print(Object obj) {
		String newValue = injector.performInjection(String.valueOf(obj));
		if (newValue == null) {
			originalWriter.print(obj);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println() {
		String newValue = injector.performInjection(NL);
		if (newValue == null) {
			originalWriter.println();
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(boolean x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(char x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(int x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(long x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(float x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(double x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	@SuppressWarnings({ "PMD", "UseVarags" })
	public void println(char[] x) {
		String newValue = injector.performInjection(String.valueOf(x) + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(String x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
	}

	@Override
	public void println(Object x) {
		String newValue = injector.performInjection(x + NL);
		if (newValue == null) {
			originalWriter.println(x);
		} else {
			originalWriter.write(newValue);
		}
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
		String newValue = injector.performInjection(String.format(format, args));
		if (newValue == null) {
			originalWriter.format(format, args);
		} else {
			originalWriter.write(newValue);
		}
		return this;
	}

	@Override
	public PrintWriter format(Locale l, String format, Object... args) {
		String newValue = injector.performInjection(String.format(l, format, args));
		if (newValue == null) {
			originalWriter.format(l, format, args);
		} else {
			originalWriter.write(newValue);
		}
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq) {
		String newValue = injector.performInjection(csq);
		if (newValue == null) {
			originalWriter.append(csq);
		} else {
			originalWriter.write(newValue);
		}
		return this;
	}

	@Override
	public PrintWriter append(CharSequence csq, int start, int end) {
		String newValue = injector.performInjection(csq.subSequence(start, end));
		if (newValue == null) {
			originalWriter.append(csq, start, end);
		} else {
			originalWriter.write(newValue);
		}
		return this;
	}

	@Override
	public PrintWriter append(char c) {
		String newValue = injector.performInjection(String.valueOf(c));
		if (newValue == null) {
			originalWriter.append(c);
		} else {
			originalWriter.write(newValue);
		}
		return this;
	}

}
