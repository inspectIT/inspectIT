package rocks.inspectit.ui.rcp.tester;

import org.eclipse.core.expressions.PropertyTester;

import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageData.StorageState;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.provider.IStorageDataProvider;
import rocks.inspectit.ui.rcp.storage.InspectITStorageManager;

/**
 * Testing the state of storage. Works if receiver is {@link StorageData} or
 * {@link rocks.inspectit.ui.rcp.model.storage.StorageLeaf}.
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
