package info.novatec.inspectit.rcp.storage.listener;

import info.novatec.inspectit.storage.IStorageData;

import java.util.EventListener;

/**
 * Storage change listener.
 * 
 * @author Ivan Senic
 * 
 */
public interface StorageChangeListener extends EventListener {

	/**
	 * Informs the listener that the storage data like name or description have been updated.
	 * 
	 * @param storageData
	 *            {@link IStorageData}.
	 */
	void storageDataUpdated(IStorageData storageData);

	/**
	 * Informs the listener that the repository was deleted on the CMR.
	 * 
	 * @param storageData
	 *            {@link IStorageData}.
	 */
	void storageRemotelyDeleted(IStorageData storageData);

	/**
	 * Informs the listener that the repository was deleted locally.
	 * 
	 * @param storageData
	 *            {@link IStorageData}.
	 */
	void storageLocallyDeleted(IStorageData storageData);
}
