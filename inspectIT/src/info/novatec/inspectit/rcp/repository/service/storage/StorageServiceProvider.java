package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.storage.LocalStorageData;

import java.util.List;

/**
 * Provider of all storage related services. This classes correctly initialize the service with help
 * of Spring.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StorageServiceProvider {

	/**
	 * @return Spring created {@link StorageTimerDataAccessService}.
	 */
	protected abstract StorageTimerDataAccessService createStorageTimerDataAccessService();

	/**
	 * Properly initialized {@link StorageTimerDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageTimerDataAccessService}.
	 */
	public StorageTimerDataAccessService createStorageTimerDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<TimerData> storageTreeComponent) {
		StorageTimerDataAccessService storageTimerDataService = createStorageTimerDataAccessService();
		storageTimerDataService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageTimerDataService.setLocalStorageData(localStorageData);
		storageTimerDataService.setIndexingTree(storageTreeComponent);
		return storageTimerDataService;
	}

	/**
	 * @return Spring created {@link StorageHttpTimerDataAccessService}.
	 */
	protected abstract StorageHttpTimerDataAccessService createStorageHttpTimerDataAccessService();

	/**
	 * Properly initialized {@link StorageHttpTimerDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageHttpTimerDataAccessService}.
	 */
	public StorageHttpTimerDataAccessService createStorageHttpTimerDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<HttpTimerData> storageTreeComponent) {
		StorageHttpTimerDataAccessService storageHttpTimerDataService = createStorageHttpTimerDataAccessService();
		storageHttpTimerDataService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageHttpTimerDataService.setLocalStorageData(localStorageData);
		storageHttpTimerDataService.setIndexingTree(storageTreeComponent);
		return storageHttpTimerDataService;
	}

	/**
	 * @return Spring created {@link StorageSqlDataAccessService}.
	 */
	protected abstract StorageSqlDataAccessService createStorageSqlDataAccessService();

	/**
	 * Properly initialized {@link StorageSqlDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageSqlDataAccessService}.
	 */
	public StorageSqlDataAccessService createStorageSqlDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<SqlStatementData> storageTreeComponent) {
		StorageSqlDataAccessService storageSqlDataAccessService = createStorageSqlDataAccessService();
		storageSqlDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageSqlDataAccessService.setLocalStorageData(localStorageData);
		storageSqlDataAccessService.setIndexingTree(storageTreeComponent);
		return storageSqlDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageExceptionDataAccessService}.
	 */
	protected abstract StorageExceptionDataAccessService createStorageExceptionDataAccessService();

	/**
	 * Properly initialized {@link StorageExceptionDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageExceptionDataAccessService}.
	 */
	public StorageExceptionDataAccessService createStorageExceptionDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<ExceptionSensorData> storageTreeComponent) {
		StorageExceptionDataAccessService storageExceptionDataAccessService = createStorageExceptionDataAccessService();
		storageExceptionDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageExceptionDataAccessService.setLocalStorageData(localStorageData);
		storageExceptionDataAccessService.setIndexingTree(storageTreeComponent);
		return storageExceptionDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageInvocationDataAccessService}.
	 */
	protected abstract StorageInvocationDataAccessService createStorageInvocationDataAccessService();

	/**
	 * Properly initialized {@link StorageInvocationDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @param cachedDataSer
	 * @return Properly initialized {@link StorageInvocationDataAccessService}.
	 */
	public StorageInvocationDataAccessService createStorageInvocationDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<InvocationSequenceData> storageTreeComponent) {
		StorageInvocationDataAccessService storageInvocationDataAccessService = createStorageInvocationDataAccessService();
		storageInvocationDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageInvocationDataAccessService.setLocalStorageData(localStorageData);
		storageInvocationDataAccessService.setIndexingTree(storageTreeComponent);
		return storageInvocationDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageGlobalDataAccessService}.
	 */
	protected abstract StorageGlobalDataAccessService createStorageGlobalDataAccessService();

	/**
	 * Properly initialized {@link StorageGlobalDataAccessService}.
	 * 
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @param platformIdents
	 *            Agents related to storage.
	 * @return Properly initialized {@link StorageGlobalDataAccessService}.
	 */
	public StorageGlobalDataAccessService createStorageGlobalDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<DefaultData> storageTreeComponent, List<PlatformIdent> platformIdents) {
		StorageGlobalDataAccessService storageGlobalDataAccessService = createStorageGlobalDataAccessService();
		storageGlobalDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageGlobalDataAccessService.setLocalStorageData(localStorageData);
		storageGlobalDataAccessService.setIndexingTree(storageTreeComponent);
		storageGlobalDataAccessService.setAgents(platformIdents);
		return storageGlobalDataAccessService;
	}
}
