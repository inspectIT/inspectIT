package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.storage.InspectITStorageManager;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Testing the state of storage. Works if receiver is {@link StorageData} or
 * {@link info.novatec.inspectit.rcp.model.storage.StorageLeaf}.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageStateTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		StorageData storageData = null;
		if (receiver instanceof IStorageDataProvider) {
			storageData = ((IStorageDataProvider) receiver).getStorageData();
		} else if (receiver instanceof StorageData) {
			storageData = (StorageData) receiver;
		} else {
			return false;
		}

		if ("storageState".equals(property)) {
			if ("CREATED".equalsIgnoreCase((String) expectedValue) && storageData.getState() == StorageState.CREATED_NOT_OPENED) {
				return true;
			} else if ("OPENED".equalsIgnoreCase((String) expectedValue) && storageData.getState() == StorageState.OPENED) {
				return true;
			} else if ("RECORDING".equalsIgnoreCase((String) expectedValue) && storageData.getState() == StorageState.RECORDING) {
				return true;
			} else if ("CLOSED".equalsIgnoreCase((String) expectedValue) && storageData.getState() == StorageState.CLOSED) {
				return true;
			}
			return false;
		}

		if ("isStorageMounted".equals(property)) {
			InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
			if (Boolean.TRUE.equals(expectedValue)) {
				return storageManager.isStorageMounted(storageData);
			} else if (Boolean.FALSE.equals(expectedValue)) {
				return !storageManager.isStorageMounted(storageData);
			}
		}

		if ("isStorageDownloaded".equals(property)) {
			InspectITStorageManager storageManager = InspectIT.getDefault().getInspectITStorageManager();
			if (Boolean.TRUE.equals(expectedValue)) {
				return storageManager.isFullyDownloaded(storageData);
			} else if (Boolean.FALSE.equals(expectedValue)) {
				return !storageManager.isFullyDownloaded(storageData);
			}
		}

		return false;
	}
}
