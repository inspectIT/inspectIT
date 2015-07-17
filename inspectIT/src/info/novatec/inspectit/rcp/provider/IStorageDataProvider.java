package info.novatec.inspectit.rcp.provider;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;

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
