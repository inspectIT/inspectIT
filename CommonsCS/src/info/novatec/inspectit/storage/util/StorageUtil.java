package info.novatec.inspectit.storage.util;

import java.util.UUID;

/**
 * General storage utility class.
 * 
 * @author Ivan Senic
 * 
 */
public final class StorageUtil {

	/**
	 * Private constructor.
	 */
	private StorageUtil() {
	}

	/**
	 * Returns the random int by invoking the hash code of the random {@link UUID} object.
	 * 
	 * @return Returns the random int by invoking the hash code of the random {@link UUID} object.
	 * @see UUID
	 */
	public static int getRandomInt() {
		return UUID.randomUUID().hashCode();
	}
}
