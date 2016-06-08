package rocks.inspectit.ui.rcp.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rocks.inspectit.shared.all.version.VersionService;
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
import rocks.inspectit.shared.cs.cmr.service.IServerStatusService.ServerStatus;
import rocks.inspectit.shared.cs.cmr.service.ISqlDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IStorageService;
import rocks.inspectit.shared.cs.cmr.service.ITimerDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.service.RefreshEditorsCachedDataService;
import rocks.inspectit.ui.rcp.repository.service.cmr.CmrServiceProvider;

/**
 * The CMR repository definition initializes the services exposed by the CMR.
 *
 * @author Patrice Bouillet
 * @author Dirk Maucher
 * @author Eduard Tudenhoefner
 * @author Matthias Huber
 * @author Alfred Krauss
 *
 */
public class CmrRepositoryDefinition implements RepositoryDefinition, ICmrRepositoryProvider {

	/**
	 * Default CMR name.
	 */
	public static final String DEFAULT_NAME = "Local CMR";

	/**
	 * Default CMR ip address.
	 */
	public static final String DEFAULT_IP = "localhost";

	/**
	 * Default CMR port.
	 */
	public static final int DEFAULT_PORT = 8182;

	/**
	 * Default description.
	 */
	public static final String DEFAULT_DESCRIPTION = "This Central Management Repository (CMR) is automatically added by default when you first start the inspectIT.";

	/**
	 * Enumeration for the online status of {@link CmrRepositoryDefinition}.
	 *
	 * @author Ivan Senic
	 *
	 */
	public enum OnlineStatus {

		/**
		 * Unknown state before the first check.
		 */
		UNKNOWN,

		/**
		 * CMR is off-line.
		 */
		OFFLINE,

		/**
		 * Status is being checked.
		 */
		CHECKING,

		/**
		 * CMR is online.
		 */
		ONLINE;

		/**
		 * Defines if the status can be changed.
		 *
		 * @param newStatus
		 *            New status
		 * @return True if the status change is allowed.
		 */
		public boolean canChangeTo(OnlineStatus newStatus) {
			if (this.equals(newStatus)) {
				return false;
			}
			if (newStatus.equals(UNKNOWN)) {
				return false;
			}
			switch (this) {
			case OFFLINE:
				if (newStatus.equals(ONLINE)) {
					return false;
				}
			case ONLINE:
				if (newStatus.equals(OFFLINE)) {
					return false;
				}
			default:
				return true;
			}
		}
	}

	/**
	 * The ip of the CMR.
	 */
	private final String ip;

	/**
	 * The port used by the CMR.
	 */
	private final int port;

	/**
	 * State of the CMR.
	 */
	private OnlineStatus onlineStatus;

	/**
	 * Key received from the serverStatusService for checking the validation of the registered IDs
	 * on the CMR.
	 */
	private String registrationIdKey;

	/**
	 * CMR name assigned by user.
	 */
	private String name;

	/**
	 * Optional description for the CMR.
	 */
	private String description;

	/**
	 * The cached data service.
	 */
	private final CachedDataService cachedDataService;

	/**
	 * The sql data access service.
	 */
	private final ISqlDataAccessService sqlDataAccessService;

	/**
	 * The invocation data access service.
	 */
	private final IInvocationDataAccessService invocationDataAccessService;

	/**
	 * The exception data access service.
	 */
	private final IExceptionDataAccessService exceptionDataAccessService;

	/**
	 * The server status service exposed by the CMR and initialized by Spring.
	 */
	private final IServerStatusService serverStatusService;

	/**
	 * The buffer data access service.
	 */
	private final ICmrManagementService cmrManagementService;

	/**
	 * The timer data access service.
	 */
	private final ITimerDataAccessService timerDataAccessService;

	/**
	 * The http timer data access service.
	 */
	private final IHttpTimerDataAccessService httpTimerDataAccessService;

	/**
	 * The {@link IRemoteCallDataAccessService}.
	 */
	private final IRemoteCallDataAccessService remoteCallDataAccessService;

	/**
	 * The {@link IGlobalDataAccessService}.
	 */
	private final IGlobalDataAccessService globalDataAccessService;

	/**
	 * The {@link IJmxDataAccessService}.
	 */
	private final IJmxDataAccessService jmxDataAccessService;

	/**
	 * The storage service.
	 */
	private final IStorageService storageService;

	/**
	 * The configuration interface service.
	 */
	private final IConfigurationInterfaceService configurationInterfaceService;

	/**
	 * The business context management service.
	 */
	private final IBusinessContextManagementService businessContextManagementService;

	/**
	 * CMR repository change listeners.
	 */
	private final List<CmrRepositoryChangeListener> cmrRepositoryChangeListeners = new ArrayList<CmrRepositoryChangeListener>(1);

	/**
	 * Calls default constructor with name 'Undefined'.
	 *
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 */
	public CmrRepositoryDefinition(String ip, int port) {
		this(ip, port, "Undefined");
	}

	/**
	 * The default constructor of this class. The ip and port is mandatory to create the connection.
	 *
	 * @param ip
	 *            The ip of the CMR.
	 * @param port
	 *            The port used by the CMR.
	 * @param name
	 *            The name of the CMR assigned by user.
	 */
	public CmrRepositoryDefinition(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.onlineStatus = OnlineStatus.UNKNOWN;
		this.name = name;

		CmrServiceProvider cmrServiceProvider = InspectIT.getService(CmrServiceProvider.class);

		sqlDataAccessService = cmrServiceProvider.getSqlDataAccessService(this);
		serverStatusService = cmrServiceProvider.getServerStatusService(this);
		invocationDataAccessService = cmrServiceProvider.getInvocationDataAccessService(this);
		exceptionDataAccessService = cmrServiceProvider.getExceptionDataAccessService(this);
		httpTimerDataAccessService = cmrServiceProvider.getHttpTimerDataAccessService(this);
		cmrManagementService = cmrServiceProvider.getCmrManagementService(this);
		timerDataAccessService = cmrServiceProvider.getTimerDataAccessService(this);
		globalDataAccessService = cmrServiceProvider.getGlobalDataAccessService(this);
		storageService = cmrServiceProvider.getStorageService(this);
		configurationInterfaceService = cmrServiceProvider.getConfigurationInterfaceService(this);
		jmxDataAccessService = cmrServiceProvider.getJmxDataAccessService(this);
		businessContextManagementService = cmrServiceProvider.getBusinessContextManagementService(this);
		remoteCallDataAccessService = cmrServiceProvider.getRemoteDataAccessService(this);

		cachedDataService = new RefreshEditorsCachedDataService(globalDataAccessService, businessContextManagementService, this);
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
	public IExceptionDataAccessService getExceptionDataAccessService() {
		return exceptionDataAccessService;
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
	public IInvocationDataAccessService getInvocationDataAccessService() {
		return invocationDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	public ICmrManagementService getCmrManagementService() {
		return cmrManagementService;
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
	public IStorageService getStorageService() {
		return storageService;
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
	public IGlobalDataAccessService getGlobalDataAccessService() {
		return globalDataAccessService;
	}

	/**
	 * Gets {@link #configurationInterfaceService}.
	 *
	 * @return {@link #configurationInterfaceService}
	 */
	public IConfigurationInterfaceService getConfigurationInterfaceService() {
		return configurationInterfaceService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBusinessContextManagementService getBusinessContextMangementService() {
		return businessContextManagementService;
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
	public IRemoteCallDataAccessService getRemoteCallDataAccessService() {
		return remoteCallDataAccessService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIp() {
		return ip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * Registers a CMR repository change listener to this CMR if it was not already.
	 *
	 * @param cmrRepositoryChangeListener
	 *            {@link CmrRepositoryChangeListener}.
	 */
	public void addCmrRepositoryChangeListener(CmrRepositoryChangeListener cmrRepositoryChangeListener) {
		synchronized (cmrRepositoryChangeListeners) {
			if (!cmrRepositoryChangeListeners.contains(cmrRepositoryChangeListener)) {
				cmrRepositoryChangeListeners.add(cmrRepositoryChangeListener);
			}
		}
	}

	/**
	 * Removes a CMR repository change listener to this CMR.
	 *
	 * @param cmrRepositoryChangeListener
	 *            {@link CmrRepositoryChangeListener}.
	 */
	public void removeCmrRepositoryChangeListener(CmrRepositoryChangeListener cmrRepositoryChangeListener) {
		synchronized (cmrRepositoryChangeListeners) {
			cmrRepositoryChangeListeners.remove(cmrRepositoryChangeListener);
		}
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the onlineStatus
	 */
	public OnlineStatus getOnlineStatus() {
		return onlineStatus;
	}

	/**
	 * If the CMR is online invokes the {@link IServerStatusService} to get the version. Otherwise
	 * returns 'N/A'.
	 *
	 * @return Version of this CMR.
	 */
	public String getVersion() {
		if (onlineStatus != OnlineStatus.OFFLINE) {
			try {
				return serverStatusService.getVersion();
			} catch (Exception e) {
				return VersionService.UNKNOWN_VERSION;
			}
		} else {
			return VersionService.UNKNOWN_VERSION;
		}
	}

	/**
	 * Updates the status of the CMR if possible.
	 *
	 * @param newStatus
	 *            New status.
	 * @return True if change was successful, false if the change is not allowed.
	 */
	public boolean changeOnlineStatus(OnlineStatus newStatus) {
		if (onlineStatus.canChangeTo(newStatus)) {
			OnlineStatus oldStatus = onlineStatus;
			onlineStatus = newStatus;
			synchronized (cmrRepositoryChangeListeners) {
				for (CmrRepositoryChangeListener changeListener : cmrRepositoryChangeListeners) {
					changeListener.repositoryOnlineStatusUpdated(this, oldStatus, newStatus);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Refreshes the online status.
	 */
	public void refreshOnlineStatus() {
		this.changeOnlineStatus(OnlineStatus.CHECKING);
		boolean isOnline = isOnline();
		if (isOnline) {
			this.changeOnlineStatus(OnlineStatus.ONLINE);
		} else {
			this.changeOnlineStatus(OnlineStatus.OFFLINE);
		}
	}

	/**
	 * Returns if the server is online by checking the {@link IServerStatusService}.
	 *
	 * @return Returns if the server is online by checking the {@link IServerStatusService}.
	 */
	private boolean isOnline() {
		try {
			ServerStatus status = serverStatusService.getServerStatus();
			if (ServerStatus.SERVER_ONLINE == status) {
				checkKey(status.getRegistrationIdsValidationKey());
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return this;
	}

	/**
	 * Check if key has changed and fire the refresh idents if necessary.
	 *
	 * @param newKey
	 *            New key received from status.
	 */
	private void checkKey(String newKey) {
		boolean isRefreshIdents = false;
		if (null == registrationIdKey) {
			registrationIdKey = newKey;
		} else {
			isRefreshIdents = !Objects.equals(registrationIdKey, newKey); // NOPMD
			registrationIdKey = newKey;
		}

		if (isRefreshIdents) {
			cachedDataService.triggerRefreshIdents();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((ip == null) ? 0 : ip.hashCode());
		result = (prime * result) + port;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CmrRepositoryDefinition other = (CmrRepositoryDefinition) obj;
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Repository definition :: Name=" + name + " IP=" + ip + " Port=" + port;
	}
}
