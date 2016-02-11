package info.novatec.inspectit.storage;

import info.novatec.inspectit.communication.DefaultData;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface for the classes that can write data to disk.. Main implementation is storage writer.
 * 
 * @author Ivan Senic
 * 
 */
public interface IWriter {

	/**
	 * Writes one object to the disk.
	 * 
	 * @param defaultData
	 *            Object to be written.
	 * @return Returns the {@link Future} for the writing task of the given data or
	 *         <code>null</code> if writing is currently suspended by the writer. This future
	 *         provides only the information when the writing task is executed, but not when the
	 *         serialized bytes are actually written on disk.
	 */
	Future<Void> write(DefaultData defaultData);

	/**
	 * Writes one object to the disk.
	 * 
	 * @param defaultData
	 *            Object to be written.
	 * @param kryoPreferences
	 *            Map of preferences to be passed to the Kryo serializer.
	 * @return Returns the {@link Future} for the writing task of the given data or
	 *         <code>null</code> if writing is currently suspended by the writer. This future
	 *         provides only the information when the writing task is executed, but not when the
	 *         serialized bytes are actually written on disk.
	 */
	Future<Void> write(DefaultData defaultData, Map<?, ?> kryoPreferences);

}
