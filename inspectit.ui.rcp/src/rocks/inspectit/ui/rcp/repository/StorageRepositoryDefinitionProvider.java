package info.novatec.inspectit.rcp.repository;

/**
 * Spring enhanced class for getting the {@link StorageRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StorageRepositoryDefinitionProvider {

	/**
	 * Returns Spring instantiated {@link StorageRepositoryDefinition}.
	 * 
	 * @return Spring instantiated {@link StorageRepositoryDefinition}.
	 */
	public abstract StorageRepositoryDefinition createStorageRepositoryDefinition();

}
