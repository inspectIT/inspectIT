package info.novatec.inspectit.storage.serializer.util;

import java.io.IOException;

import com.esotericsoftware.kryo.io.Input;

/**
 * Kryo utilities.
 * 
 * @author Ivan Senic
 * 
 */
public final class KryoUtil {

	/**
	 * Private constructor.
	 */
	private KryoUtil() {
	}

	/**
	 * Returns if the input has more bytes that Kryo can read. This method will first check if any
	 * bytes are left to read in the Input's internal buffer. If no bytes are left there, will check
	 * the underlying input stream if the amount of available bytes is more than zero.
	 * 
	 * @param input
	 *            {@link Input}
	 * @return True if there is at least 1 bytes to read, false otherwise.
	 * @throws IOException
	 *             If {@link IOException} occurs on the Input's input stream.
	 */
	public static boolean hasMoreBytes(Input input) throws IOException {
		if (input.limit() > input.position()) {
			return true;
		} else {
			return null != input.getInputStream() && input.getInputStream().available() > 0;
		}
	}
}
