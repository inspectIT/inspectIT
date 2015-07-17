package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.cmr.service.ICmrManagementService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.cmr.service.IInvocationDataAccessService;
import info.novatec.inspectit.cmr.service.IServerStatusService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

/**
 * Provider of the {@link ICmrService}s via Spring.
 * 
 * @author Ivan Senic
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
	 * Returns properly initialized {@link StorageService}.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to bound service to.
	 * @return Returns {@link StorageService}.
	 */
	public IStorageService getStorageService(CmrRepositoryDefinition cmrRepositoryDefinition) {
		IStorageService storageService = getStorageService();
		((ICmrService) storageService).initService(cmrRepositoryDefinition);
		return storageService;
	}

	/**
	 * Returns Spring created {@link StorageService}.
	 * 
	 * @return Returns Spring created {@link StorageService}.
	 */
	protected abstract IStorageService getStorageService();

}
