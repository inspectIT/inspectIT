package rocks.inspectit.agent.java.eum;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Reflection wrapper for {@link javax.servlet.ServletOutputStream}.
 *
 * @author Jonas Kunz
 *
 */
public final class WServletOutputStream {

	/**
	 * See {@link javax.servlet.ServletOutputStream}.
	 */
	private static final String CLAZZ = "javax.servlet.ServletOutputStream";

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(boolean)}.
	 */
	private static final CachedMethod<Void> PRINT_BOOL = new CachedMethod<Void>(CLAZZ, "print", boolean.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(char)}.
	 */
	private static final CachedMethod<Void> PRINT_CHAR = new CachedMethod<Void>(CLAZZ, "print", char.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(double)}.
	 */
	private static final CachedMethod<Void> PRINT_DOUBLE = new CachedMethod<Void>(CLAZZ, "print", double.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(float)}.
	 */
	private static final CachedMethod<Void> PRINT_FLOAT = new CachedMethod<Void>(CLAZZ, "print", float.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(int)}.
	 */
	private static final CachedMethod<Void> PRINT_INT = new CachedMethod<Void>(CLAZZ, "print", int.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(long)}.
	 */
	private static final CachedMethod<Void> PRINT_LONG = new CachedMethod<Void>(CLAZZ, "print", long.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#print(String)}.
	 */
	private static final CachedMethod<Void> PRINT_STRING = new CachedMethod<Void>(CLAZZ, "print", String.class);


	/**
	 * See {@link javax.servlet.ServletOutputStream#println()}.
	 */
	private static final CachedMethod<Void> PRINTLN_NOARGS = new CachedMethod<Void>(CLAZZ, "println");

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(boolean)}.
	 */
	private static final CachedMethod<Void> PRINTLN_BOOL = new CachedMethod<Void>(CLAZZ, "println", boolean.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(char)}.
	 */
	private static final CachedMethod<Void> PRINTLN_CHAR = new CachedMethod<Void>(CLAZZ, "println", char.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(double)}.
	 */
	private static final CachedMethod<Void> PRINTLN_DOUBLE = new CachedMethod<Void>(CLAZZ, "println", double.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(float)}.
	 */
	private static final CachedMethod<Void> PRINTLN_FLOAT = new CachedMethod<Void>(CLAZZ, "println", float.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(int)}.
	 */
	private static final CachedMethod<Void> PRINTLN_INT = new CachedMethod<Void>(CLAZZ, "println", int.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(long)}.
	 */
	private static final CachedMethod<Void> PRINTLN_LONG = new CachedMethod<Void>(CLAZZ, "println", long.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#println(String)}.
	 */
	private static final CachedMethod<Void> PRINTLN_STRING = new CachedMethod<Void>(CLAZZ, "println", String.class);

	/**
	 * See {@link javax.servlet.ServletOutputStream#isReady()}.
	 */
	private static final CachedMethod<Boolean> IS_READY = new CachedMethod<Boolean>(CLAZZ, "isReady");

	/**
	 * See {@link javax.servlet.ServletOutputStream#setWriteListener()}.
	 */
	private static final CachedMethod<Void> SET_WRITE_LISTENER = new CachedMethod<Void>(CLAZZ, "setWriteListener", "javax.servlet.WriteListener");

	/**
	 * The wrapped {@link javax.servlet.ServletOutputStream} instance.
	 */
	private Object instance;

	/**
	 * @param inst
	 *            the instance to wrap
	 */
	private WServletOutputStream(Object inst) {
		this.instance = inst;
	}

	/**
	 * Wraps the given {@link javax.servlet.ServletOutputStream} instance.
	 *
	 * @param stream
	 *            the {@link javax.servlet.ServletOutputStream} instance to wrap
	 * @return the wrapper.
	 *
	 */
	public static WServletOutputStream wrap(Object stream) {
		return new WServletOutputStream(stream);
	}

	/**
	 * @param obj
	 *            the object to check
	 * @return true, if obj is an instance of {@link javax.servlet.ServletOutputStream}.
	 */
	public static boolean isInstance(Object obj) {
		return ClassLoaderAwareClassCache.isInstance(obj, CLAZZ);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(boolean)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(boolean val) {
		PRINT_BOOL.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(char)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(char val) {
		PRINT_CHAR.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(double)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(double val) {
		PRINT_DOUBLE.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(float)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(float val) {
		PRINT_FLOAT.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(int)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(int val) {
		PRINT_INT.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(long)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(long val) {
		PRINT_LONG.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#print(String)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void print(String val) {
		PRINT_STRING.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println()}.
	 */
	public void println() {
		PRINTLN_NOARGS.callSafeExceptions(IOException.class, instance);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(boolean)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(boolean val) {
		PRINTLN_BOOL.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(char)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(char val) {
		PRINTLN_CHAR.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(double)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(double val) {
		PRINTLN_DOUBLE.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(float)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(float val) {
		PRINTLN_FLOAT.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(int)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(int val) {
		PRINTLN_INT.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(val)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(long val) {
		PRINTLN_LONG.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#println(String)}.
	 *
	 * @param val
	 *            the value to print
	 */
	public void println(String val) {
		PRINTLN_STRING.callSafeExceptions(IOException.class, instance, val);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#close()}.
	 *
	 * @throws IOException
	 *             if something unexpected happens
	 */
	public void close() throws IOException {
		((OutputStream) instance).close();
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#flush()}.
	 *
	 * @throws IOException
	 *             if something unexpected happens
	 */
	public void flush() throws IOException {
		((OutputStream) instance).flush();
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#write(byte[])}.
	 *
	 * @param b
	 *            the bytes to write
	 * @throws IOException
	 *             if something unexpected happens
	 */
	public void write(byte[] b) throws IOException {
		((OutputStream) instance).write(b);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#write(byte[], int, int)}.
	 *
	 * @param b
	 *            the bytes to write
	 * @param off
	 *            the offset
	 * @param len
	 *            the amount of bytes
	 * @throws IOException
	 *             if something unexpected happens
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		((OutputStream) instance).write(b, off, len);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#write(int)}.
	 *
	 * @param b
	 *            the byte to write
	 * @throws IOException
	 *             if something unexpected happens
	 *
	 */
	public void write(int b) throws IOException {
		((OutputStream) instance).write(b);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#isReady()}.
	 *
	 * @return true if ready
	 *
	 */
	public boolean isReady() {
		return IS_READY.callSafeExceptions(IOException.class, instance);
	}

	/**
	 * see {@link javax.servlet.ServletOutputStream#setWriteListener()}.
	 *
	 * @param listener
	 *            the {@link javax.servlet.WriteListener} instance.
	 *
	 */
	public void setWriteListener(Object listener) {
		SET_WRITE_LISTENER.callSafeExceptions(IOException.class, instance, listener);
	}

}
