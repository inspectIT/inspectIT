package rocks.inspectit.ui.rcp.provider;

import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Interface for all model components that can provide the information about the storage.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStorageDataProvider {

	/**
	 * Gives the {@link CmrRepositoryDefinition} where Storage is located.
	 * 
	 * @return Gives the {@link CmrRepositoryDefinition} or null if storage is local.
	 */
	CmrRepositoryDefinition getCmrRepositoryDefinition();

	/**
	 * Returns the storage data.
	 * 
	 * @return the storageData {@link StorageData}.
	 */
	StorageData getStorageData();
}
