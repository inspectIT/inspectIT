package rocks.inspectit.ui.rcp.repository.service.cmr;

import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;
import rocks.inspectit.shared.cs.cmr.service.ICmrManagementService;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;
import rocks.inspectit.shared.cs.cmr.service.IExceptionDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IHttpTimerDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IJmxDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IRemoteCallDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IServerStatusService;
import rocks.inspectit.shared.cs.cmr.service.ISqlDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.cmr.service.ITimerDataAccessService;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Provider of the {@link ICmrService}s via Spring.
 *
 * @author Ivan Senic
 * @author Alfred Krauss
 *
 */
public abstract class CmrServiceProvider {

	/**
	 * Returns properly initialized {@link BufferService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link BufferService}.
	 */
	public ICmrManagementService getCmrManagementService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ICmrManagementService cmrManagementService = getCmrManagementService();
		((ICmrService) cmrManagementService).initService(cmrRepositoryDefinition);
		return cmrManagementService;
	}

	/**
	 * Returns Spring created {@link BufferService}.
	 *
	 * @return Returns Spring created {@link BufferService}.
	 */
	protected abstract ICmrManagementService getCmrManagementService();

	/**
	 * Returns properly initialized {@link ExceptionDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link ExceptionDataAccessService}.
	 */
	public IExceptionDataAccessService getExceptionDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IExceptionDataAccessService exceptionDataAccessService = getExceptionDataAccessService();
		((ICmrService) exceptionDataAccessService).initService(cmrRepositoryDefinition);
		return exceptionDataAccessService;
	}

	/**
	 * Returns Spring created {@link ExceptionDataAccessService}.
	 *
	 * @return Returns Spring created {@link ExceptionDataAccessService}.
	 */
	protected abstract IExceptionDataAccessService getExceptionDataAccessService();

	/**
	 * Returns properly initialized {@link GlobalDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link GlobalDataAccessService}.
	 */
	public IGlobalDataAccessService getGlobalDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IGlobalDataAccessService globalDataAccessService = getGlobalDataAccessService();
		((ICmrService) globalDataAccessService).initService(cmrRepositoryDefinition);
		return globalDataAccessService;
	}

	/**
	 * Returns Spring created {@link GlobalDataAccessService}.
	 *
	 * @return Returns Spring created {@link GlobalDataAccessService}.
	 */
	protected abstract IGlobalDataAccessService getGlobalDataAccessService();

	/**
	 * Returns properly initialized {@link JmxDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link JmxDataAccessService}.
	 */
	public IJmxDataAccessService getJmxDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IJmxDataAccessService jmxDataAccessService = getJmxDataAccessService();
		((ICmrService) jmxDataAccessService).initService(cmrRepositoryDefinition);
		return jmxDataAccessService;
	}

	/**
	 * Returns Spring created {@link JmxDataAccessService}.
	 *
	 * @return Returns Spring created {@link JmxDataAccessService}.
	 */
	protected abstract IJmxDataAccessService getJmxDataAccessService();

	/**
	 * Returns properly initialized {@link InvocationDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link InvocationDataAccessService}.
	 */
	public IInvocationDataAccessService getInvocationDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IInvocationDataAccessService invocationDataAccessService = getInvocationDataAccessService();
		((ICmrService) invocationDataAccessService).initService(cmrRepositoryDefinition);
		return invocationDataAccessService;
	}

	/**
	 * Returns Spring created {@link InvocationDataAccessService}.
	 *
	 * @return Returns Spring created {@link InvocationDataAccessService}.
	 */
	protected abstract IInvocationDataAccessService getInvocationDataAccessService();

	/**
	 * Returns properly initialized {@link ServerStatusService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link ServerStatusService}.
	 */
	public IServerStatusService getServerStatusService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IServerStatusService serverStatusService = getServerStatusService();
		((ICmrService) serverStatusService).initService(cmrRepositoryDefinition);
		return serverStatusService;
	}

	/**
	 * Returns Spring created {@link ServerStatusService}.
	 *
	 * @return Returns Spring created {@link ServerStatusService}.
	 */
	protected abstract IServerStatusService getServerStatusService();

	/**
	 * Returns properly initialized {@link SqlDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link SqlDataAccessService}.
	 */
	public ISqlDataAccessService getSqlDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ISqlDataAccessService sqlDataAccessService = getSqlDataAccessService();
		((ICmrService) sqlDataAccessService).initService(cmrRepositoryDefinition);
		return sqlDataAccessService;
	}

	/**
	 * Returns Spring created {@link SqlDataAccessService}.
	 *
	 * @return Returns Spring created {@link SqlDataAccessService}.
	 */
	protected abstract ISqlDataAccessService getSqlDataAccessService();

	/**
	 * Returns properly initialized {@link TimerDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link TimerDataAccessService}.
	 */
	public ITimerDataAccessService getTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		ITimerDataAccessService timerDataAccessService = getTimerDataAccessService();
		((ICmrService) timerDataAccessService).initService(cmrRepositoryDefinition);
		return timerDataAccessService;
	}

	/**
	 * Returns Spring created {@link TimerDataAccessService}.
	 *
	 * @return Returns Spring created {@link TimerDataAccessService}.
	 */
	protected abstract ITimerDataAccessService getTimerDataAccessService();

	/**
	 * Returns properly initialized {@link TimerDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link TimerDataAccessService}.
	 */
	public IHttpTimerDataAccessService getHttpTimerDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IHttpTimerDataAccessService httpTimerDataAccessService = getHttpTimerDataAccessService();
		((ICmrService) httpTimerDataAccessService).initService(cmrRepositoryDefinition);
		return httpTimerDataAccessService;
	}

	/**
	 * Returns Spring created {@link TimerDataAccessService}.
	 *
	 * @return Returns Spring created {@link TimerDataAccessService}.
	 */
	protected abstract IHttpTimerDataAccessService getHttpTimerDataAccessService();

	/**
	 * Returns properly initialized {@link RemoteCallDataAccessService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link RemoteCallDataAccessService}.
	 */
	public IRemoteCallDataAccessService getRemoteDataAccessService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IRemoteCallDataAccessService remoteCallDataAccessService = getRemoteDataAccessService();
		((ICmrService) remoteCallDataAccessService).initService(cmrRepositoryDefinition);
		return remoteCallDataAccessService;
	}

	/**
	 * Returns Spring created {@link RemoteCallDataAccessService}.
	 *
	 * @return Returns Spring created {@link RemoteCallDataAccessService}.
	 */
	protected abstract IRemoteCallDataAccessService getRemoteDataAccessService();

	/**
	 * Returns properly initialized {@link IStorageService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link IStorageService}.
	 */
	public IStorageService getStorageService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IStorageService storageService = getStorageService();
		((ICmrService) storageService).initService(cmrRepositoryDefinition);
		return storageService;
	}

	/**
	 * Returns Spring created {@link IStorageService}.
	 *
	 * @return Returns Spring created {@link IStorageService}.
	 */
	protected abstract IStorageService getStorageService();

	/**
	 * Returns properly initialized {@link IConfigurationInterfaceService}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link IConfigurationInterfaceService}.
	 */
	public IConfigurationInterfaceService getConfigurationInterfaceService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IConfigurationInterfaceService configurationInterfaceService = getConfigurationInterfaceService();
		((ICmrService) configurationInterfaceService).initService(cmrRepositoryDefinition);
		return configurationInterfaceService;
	}

	/**
	 * Returns Spring created {@link IConfigurationInterfaceService}.
	 *
	 * @return Returns Spring created {@link IConfigurationInterfaceService}.
	 */
	protected abstract IConfigurationInterfaceService getConfigurationInterfaceService();
	
	/**
	 * Returns properly initialized {@link IBusinessContextManagement}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link IBusinessContextManagementService}.
	 */
	public IBusinessContextManagementService getBusinessContextManagementService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IBusinessContextManagementService businessCtxMgmtService = getBusinessContextManagementService();
		((ICmrService) businessCtxMgmtService).initService(cmrRepositoryDefinition);
		return businessCtxMgmtService;
	}

	/**
	 * Returns Spring created {@link BusinessContextManagementService}.
	 * 
	 * @return Returns Spring created {@link BusinessContextManagementService}.
	 */
	protected abstract IBusinessContextManagementService getBusinessContextManagementService();


}
