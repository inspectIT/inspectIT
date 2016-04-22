package rocks.inspectit.ui.rcp.storage.listener;

import java.util.EventListener;

import rocks.inspectit.shared.cs.storage.IStorageData;

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
