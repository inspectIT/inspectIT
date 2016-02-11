package rocks.inspectit.shared.cs.storage.processor.write;

import java.util.Map;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.shared.cs.storage.StorageWriter;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;

/**
 * Special type of processor that performs operations on the elements that have been written to
 * storage.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractWriteDataProcessor {

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param kryoPreferences
	 *            Map of preferences to be passed to the Kryo serializer. Classes can check for if
	 *            some preferences are set or not during the processing.
	 */
	public void process(DefaultData defaultData, Map<?, ?> kryoPreferences) {
		if (canBeProcessed(defaultData)) {
			processData(defaultData, kryoPreferences);
		}
	}

	/**
	 * Concrete method for processing. IMplemented by sub-classeS.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param kryoPreferences
	 *            Map of preferences to be passed to the Kryo serializer. Classes can check for if
	 *            some preferences are set or not during the processing.
	 */
	protected abstract void processData(DefaultData defaultData, Map<?, ?> kryoPreferences);

	/**
	 * Returns if the {@link DefaultData} object can be processed by this
	 * {@link AbstractDataProcessor}.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return True if data can be processed, false otherwise.
	 */
	public abstract boolean canBeProcessed(DefaultData defaultData);

	/**
	 * Called on the preparation of the storage.
	 * <p>
	 * Subclasses may override.
	 * 
	 * @param storageManager
	 *            Storage manager to help in performing tasks.
	 * @param storageWriter
	 *            writer that is being finalized
	 * @param storageData
	 *            {@link StorageData} that represents storage to be finalized.
	 * @throws Exception
	 *             If any exception occurs.
	 */
	public void onPrepare(StorageManager storageManager, StorageWriter storageWriter, StorageData storageData) throws Exception {
	}

	/**
	 * Called on the finalization of the storage.
	 * <p>
	 * Subclasses may override.
	 * 
	 * @param storageManager
	 *            Storage manager to help in performing tasks.
	 * @param storageWriter
	 *            writer that is being finalized
	 * @param storageData
	 *            {@link StorageData} that represents storage to be finalized.
	 * @throws Exception
	 *             If any exception occurs.
	 */
	public void onFinalization(StorageManager storageManager, StorageWriter storageWriter, StorageData storageData) throws Exception {
	}
}
