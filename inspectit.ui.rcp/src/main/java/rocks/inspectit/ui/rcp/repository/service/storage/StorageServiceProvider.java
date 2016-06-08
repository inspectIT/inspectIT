package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.Collection;
import java.util.List;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;

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

	/**
	 * @return Spring created {@link StorageJmxDataAccessService}.
	 */
	protected abstract StorageJmxDataAccessService createStorageJmxDataAccessService();

	/**
	 * Properly initialized {@link StorageJmxDataAccessService}.
	 *
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageJmxDataAccessService}.
	 */
	public StorageJmxDataAccessService createStorageJmxDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<JmxSensorValueData> storageTreeComponent) {
		StorageJmxDataAccessService storageJmxDataAccessService = createStorageJmxDataAccessService();
		storageJmxDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageJmxDataAccessService.setLocalStorageData(localStorageData);
		storageJmxDataAccessService.setIndexingTree(storageTreeComponent);
		return storageJmxDataAccessService;
	}

	/**
	 * @return Spring created {@link StorageBusinessContextService}.
	 */
	protected abstract StorageBusinessContextService createStorageBusinessContextService();

	/**
	 * Properly initialized {@link StorageBusinessContextService}.
	 *
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param indexingTree
	 *            Indexing tree.
	 * @param businessTransactions
	 *            a collection of {@link BusinessTransactionData} instances.
	 * @return The storage implementation of the {@link IBusinessContextManagementService}
	 */
	public IBusinessContextManagementService createStorageBusinessContextService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<DefaultData> indexingTree, Collection<BusinessTransactionData> businessTransactions) {
		StorageBusinessContextService storageBusinessContextService = createStorageBusinessContextService();
		storageBusinessContextService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageBusinessContextService.setLocalStorageData(localStorageData);
		storageBusinessContextService.setIndexingTree(indexingTree);
		storageBusinessContextService.setBusinessTransactions(businessTransactions);
		return storageBusinessContextService;
	}

	/**
	 * Properly initialized {@link StorageRemoteDataAccessService}.
	 *
	 * @param storageRepositoryDefinition
	 *            {@link StorageRepositoryDefinition}.
	 * @param localStorageData
	 *            {@link LocalStorageData}.
	 * @param storageTreeComponent
	 *            Indexing tree.
	 * @return Properly initialized {@link StorageRemoteDataAccessService}.
	 */
	public StorageRemoteDataAccessService createStorageRemoteDataAccessService(StorageRepositoryDefinition storageRepositoryDefinition, LocalStorageData localStorageData,
			IStorageTreeComponent<RemoteCallData> storageTreeComponent) {
		StorageRemoteDataAccessService storageRemoteDataAccessService = createStorageRemoteDataAccessService();
		storageRemoteDataAccessService.setStorageRepositoryDefinition(storageRepositoryDefinition);
		storageRemoteDataAccessService.setLocalStorageData(localStorageData);
		storageRemoteDataAccessService.setIndexingTree(storageTreeComponent);
		return storageRemoteDataAccessService;

	}

	/**
	 * @return Spring created {@link StorageRemoteDataAccessService}.
	 */
	protected abstract StorageRemoteDataAccessService createStorageRemoteDataAccessService();
}
