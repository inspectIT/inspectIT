package rocks.inspectit.ui.rcp.repository;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Objects;

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
import rocks.inspectit.shared.cs.cmr.service.IExceptionDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IHttpTimerDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IJmxDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IRemoteCallDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISqlDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ITimerDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.ui.rcp.repository.service.storage.StorageServiceProvider;

/**
 * Storage repository definition. This {@link RepositoryDefinition} has a direct usage of a
 * {@link CmrRepositoryDefinition} where storage is located.
 *
 * @author Ivan Senic
 *
 */
public class StorageRepositoryDefinition implements RepositoryDefinition {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link LocalStorageData}.
	 */
	private LocalStorageData localStorageData;

	/**
	 * {@link IInvocationDataAccessService} service.
	 */
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * {@link IGlobalDataAccessService} service.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * {@link IJmxDataAccessService} service.
	 */
	private IJmxDataAccessService jmxDataAccessService;

	/**
	 * Caching component.
	 */
	private CachedDataService cachedDataService;

	/**
	 * {@link IExceptionDataAccessService}.
	 */
	private IExceptionDataAccessService exceptionDataAccessService;

	/**
	 * {@link ISqlDataAccessService}.
	 */
	private ISqlDataAccessService sqlDataAccessService;

	/**
	 * {@link ITimerDataAccessService}.
	 */
	private ITimerDataAccessService timerDataAccessService;

	/**
	 * {@link IHttpTimerDataAccessService}.
	 */
	private IHttpTimerDataAccessService httpTimerDataAccessService;

	/**
	 * {@link IBusinessContextDefinition}.
	 */
	private IBusinessContextManagementService businessContextService;
	
	/**
	 * {@link IRemoteCallDataAccessService}.
	 */
	private IRemoteCallDataAccessService remoteCallDataAccessService;

	/**
	 * {@link StorageServiceProvider} for instantiating storage services.
	 */
	private StorageServiceProvider storageServiceProvider;

	/**
	 * Indexing tree for storage.
	 */
	private IStorageTreeComponent<? extends DefaultData> indexingTree;

	/**
	 * Involved agents.
	 */
	private List<PlatformIdent> agents;

	/**
	 * Collection of {@link BusinessTransactionData} instances to use for this storage.
	 */
	private Collection<BusinessTransactionData> businessTransactions;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIp() {
		return cmrRepositoryDefinition.getIp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return cmrRepositoryDefinition.getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return localStorageData.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISqlDataAccessService getSqlDataAccessService() {
		return sqlDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IExceptionDataAccessService getExceptionDataAccessService() {
		return exceptionDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IGlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IJmxDataAccessService getJmxDataAccessService() {
		return jmxDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CachedDataService getCachedDataService() {
		return cachedDataService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITimerDataAccessService getTimerDataAccessService() {
		return timerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHttpTimerDataAccessService getHttpTimerDataAccessService() {
		return httpTimerDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBusinessContextManagementService getBusinessContextMangementService() {
		return businessContextService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRemoteCallDataAccessService getRemoteCallDataAccessService() {
		return remoteCallDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void initServices() {
		// init services
		globalDataAccessService = storageServiceProvider.createStorageGlobalDataAccessService(this, localStorageData, (IStorageTreeComponent<DefaultData>) indexingTree, agents);
		exceptionDataAccessService = storageServiceProvider.createStorageExceptionDataAccessService(this, localStorageData, (IStorageTreeComponent<ExceptionSensorData>) indexingTree);
		invocationDataAccessService = storageServiceProvider.createStorageInvocationDataAccessService(this, localStorageData, (IStorageTreeComponent<InvocationSequenceData>) indexingTree);
		sqlDataAccessService = storageServiceProvider.createStorageSqlDataAccessService(this, localStorageData, (IStorageTreeComponent<SqlStatementData>) indexingTree);
		timerDataAccessService = storageServiceProvider.createStorageTimerDataAccessService(this, localStorageData, (IStorageTreeComponent<TimerData>) indexingTree);
		httpTimerDataAccessService = storageServiceProvider.createStorageHttpTimerDataAccessService(this, localStorageData, (IStorageTreeComponent<HttpTimerData>) indexingTree);
		jmxDataAccessService = storageServiceProvider.createStorageJmxDataAccessService(this, localStorageData, (IStorageTreeComponent<JmxSensorValueData>) indexingTree);
		businessContextService = storageServiceProvider.createStorageBusinessContextService(this, localStorageData, (IStorageTreeComponent<DefaultData>) indexingTree, businessTransactions);
		remoteCallDataAccessService = storageServiceProvider.createStorageRemoteDataAccessService(this, localStorageData, (IStorageTreeComponent<RemoteCallData>) indexingTree);
		// for storage we use the regular cached data service because ids can never change
		cachedDataService = new CachedDataService(globalDataAccessService, businessContextService);
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * @param cmrRepositoryDefinition
	 *            the cmrRepositoryDefinition to set
	 */
	public void setCmrRepositoryDefinition(CmrRepositoryDefinition cmrRepositoryDefinition) {
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
	}

	/**
	 * @return the storageData
	 */
	public LocalStorageData getLocalStorageData() {
		return localStorageData;
	}

	/**
	 * @param localStorageData
	 *            the storageData to set
	 */
	public void setLocalStorageData(LocalStorageData localStorageData) {
		this.localStorageData = localStorageData;
	}

	/**
	 * @param storageServiceProvider
	 *            the storageServiceProvider to set
	 */
	public void setStorageServiceProvider(StorageServiceProvider storageServiceProvider) {
		this.storageServiceProvider = storageServiceProvider;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<? extends DefaultData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param agents
	 *            the agents to set
	 */
	public void setAgents(List<PlatformIdent> agents) {
		this.agents = agents;
	}

	/**
	 * Sets {@link #businessTransactions}.
	 *
	 * @param businessTransactions
	 *            a collection of {@link #BusinessTransactionData} instances
	 */
	public void setBusinessTransactions(Collection<BusinessTransactionData> businessTransactions) {
		this.businessTransactions = businessTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(localStorageData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		StorageRepositoryDefinition that = (StorageRepositoryDefinition) object;
		return Objects.equal(this.localStorageData, that.localStorageData);

	}
}
